# 智能启动测试环境脚本
# 自动检测网关服务状态，决定是复用还是独立启动

param(
    [switch]$Debug,
    [switch]$Stop,
    [switch]$Restart,
    [switch]$Status,
    [switch]$ForceIndependent  # 强制独立启动，不复用网关服务
)

$ComposeFile = "docker-compose.test.yml"
$ProjectName = "charge-station-test"

function Write-Info {
    param($Message)
    Write-Host "🔵 $Message" -ForegroundColor Blue
}

function Write-Success {
    param($Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Error {
    param($Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Warning {
    param($Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Test-DockerRunning {
    try {
        docker version | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Test-ServiceRunning {
    param($ServiceName)
    try {
        $container = docker ps --filter "name=$ServiceName" --filter "status=running" --format "{{.Names}}" 2>$null
        return ($container -eq $ServiceName)
    }
    catch {
        return $false
    }
}

function Test-ServiceHealthy {
    param($ServiceName)
    try {
        $health = docker inspect --format='{{.State.Health.Status}}' $ServiceName 2>$null
        return ($health -eq "healthy")
    }
    catch {
        return $false
    }
}

function Get-GatewayServiceStatus {
    $gatewayServices = @{
        "redis-test" = $false
        "kafka-test" = $false
        "zookeeper-test" = $false
    }
    
    foreach ($service in $gatewayServices.Keys) {
        $gatewayServices[$service] = Test-ServiceRunning $service
    }
    
    return $gatewayServices
}

function Start-IndependentServices {
    Write-Warning "网关服务未完全运行，启动独立的测试环境"
    Write-Info "这将使用不同的网络段避免冲突"
    
    # 临时修改网络配置
    $tempComposeContent = Get-Content $ComposeFile -Raw
    $tempComposeContent = $tempComposeContent -replace "external: true", "driver: bridge"
    $tempComposeContent = $tempComposeContent -replace "name: test_test-network", "ipam:`n      config:`n        - subnet: 172.26.0.0/16"
    
    # 添加独立的中间件服务
    $independentServices = @"

  # Redis服务（独立模式）
  redis-test:
    image: redis:7-alpine
    container_name: redis-test-independent
    ports:
      - "6380:6379"  # 使用不同端口避免冲突
    command: redis-server --appendonly yes
    volumes:
      - redis-test-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5
    networks:
      - test-network

  # Zookeeper服务（独立模式）
  zookeeper-test:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper-test-independent
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2183:2181"  # 使用不同端口避免冲突
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "2181"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - test-network

  # Kafka服务（独立模式）
  kafka-test:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka-test-independent
    depends_on:
      zookeeper-test:
        condition: service_healthy
    ports:
      - "9093:9092"  # 使用不同端口避免冲突
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper-test:2181
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka-test:29092,EXTERNAL://localhost:9093
      KAFKA_LISTENERS: INTERNAL://0.0.0.0:29092,EXTERNAL://0.0.0.0:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      KAFKA_DELETE_TOPIC_ENABLE: "true"
      KAFKA_LOG_RETENTION_HOURS: 1
      KAFKA_LOG_SEGMENT_BYTES: 1048576
      KAFKA_PROCESS_ROLES: ""
    healthcheck:
      test: ["CMD", "kafka-topics", "--bootstrap-server", "kafka-test:9092", "--list"]
      interval: 10s
      timeout: 10s
      retries: 5
    networks:
      - test-network
"@
    
    $tempComposeContent = $tempComposeContent -replace "(?s)(services:.*?)(volumes:)", "`$1$independentServices`n`$2"
    
    # 写入临时文件
    $tempComposeFile = "docker-compose.temp.yml"
    $tempComposeContent | Out-File -FilePath $tempComposeFile -Encoding UTF8
    
    try {
        # 使用临时文件启动服务
        docker-compose -f $tempComposeFile -p "$ProjectName-independent" up -d
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "独立测试环境启动成功"
            Write-Info "服务访问地址："
            Write-Host "  PostgreSQL: localhost:5432" -ForegroundColor Cyan
            Write-Host "  Redis: localhost:6380" -ForegroundColor Cyan
            Write-Host "  Kafka: localhost:9093" -ForegroundColor Cyan
            
            # 更新应用配置以使用独立端口
            Write-Warning "请手动更新 application-integration.yml 中的端口配置："
            Write-Host "  Redis port: 6380" -ForegroundColor Yellow
            Write-Host "  Kafka bootstrap-servers: localhost:9093" -ForegroundColor Yellow
        }
    }
    finally {
        # 清理临时文件
        if (Test-Path $tempComposeFile) {
            Remove-Item $tempComposeFile
        }
    }
}

function Start-SharedServices {
    param($GatewayStatus)
    
    $runningServices = $GatewayStatus.Keys | Where-Object { $GatewayStatus[$_] }
    Write-Success "检测到网关服务运行中: $($runningServices -join ', ')"
    Write-Info "将复用网关的基础中间件服务"
    
    # 只启动 PostgreSQL
    docker-compose -f $ComposeFile -p $ProjectName up -d postgres-test
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Station 测试环境启动成功（共享模式）"
        Write-Info "服务访问地址："
        Write-Host "  PostgreSQL: localhost:5432 (Station专用)" -ForegroundColor Cyan
        Write-Host "  Redis: localhost:6379 (共享网关)" -ForegroundColor Cyan
        Write-Host "  Kafka: localhost:9092 (共享网关)" -ForegroundColor Cyan
    }
}

# 主逻辑
if ($Status) {
    Write-Info "检查服务状态..."
    $gatewayStatus = Get-GatewayServiceStatus
    
    Write-Info "网关服务状态："
    foreach ($service in $gatewayStatus.Keys) {
        $status = if ($gatewayStatus[$service]) { "✅ 运行中" } else { "❌ 未运行" }
        Write-Host "  $service`: $status"
    }
    
    Write-Info "Station 服务状态："
    $stationPostgres = Test-ServiceRunning "postgres-test"
    $status = if ($stationPostgres) { "✅ 运行中" } else { "❌ 未运行" }
    Write-Host "  postgres-test: $status"
    
    return
}

if ($Stop) {
    Write-Info "停止测试环境..."
    docker-compose -f $ComposeFile -p $ProjectName down
    docker-compose -f $ComposeFile -p "$ProjectName-independent" down 2>$null
    Write-Success "测试环境已停止"
    return
}

if (-not (Test-DockerRunning)) {
    Write-Error "Docker 未运行，请启动 Docker Desktop"
    return
}

# 检查网关服务状态
$gatewayStatus = Get-GatewayServiceStatus
$gatewayServicesRunning = ($gatewayStatus.Values | Where-Object { $_ }).Count

if ($ForceIndependent -or $gatewayServicesRunning -eq 0) {
    Start-IndependentServices
} elseif ($gatewayServicesRunning -eq 3) {
    Start-SharedServices $gatewayStatus
} else {
    Write-Warning "网关服务部分运行，建议选择："
    Write-Host "1. 停止所有网关服务，使用独立模式" -ForegroundColor Yellow
    Write-Host "2. 启动完整网关服务，使用共享模式" -ForegroundColor Yellow
    Write-Host "3. 强制使用独立模式: .\scripts\start-test-env-smart.ps1 -ForceIndependent" -ForegroundColor Yellow
}
