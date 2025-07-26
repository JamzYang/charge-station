# 运行集成测试脚本
# 自动启动测试环境并运行集成测试

param(
    [string]$TestClass = "*IntegrationTest",
    [switch]$SkipEnvStart,
    [switch]$Debug,
    [switch]$StopAfter
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

function Test-DockerRunning {
    try {
        docker version | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Test-ServicesRunning {
    $runningServices = docker-compose -f $ComposeFile -p $ProjectName ps --filter "health=healthy" --format "table {{.Service}}" | Select-Object -Skip 1
    $requiredServices = @("postgres-test", "redis-test", "kafka-test")
    
    foreach ($service in $requiredServices) {
        if ($runningServices -notcontains $service) {
            return $false
        }
    }
    return $true
}

# 主逻辑
Write-Info "准备运行集成测试: $TestClass"

# 检查 Docker
if (-not (Test-DockerRunning)) {
    Write-Error "Docker 未运行，请启动 Docker Desktop"
    exit 1
}

# 启动测试环境（如果需要）
if (-not $SkipEnvStart) {
    if (-not (Test-ServicesRunning)) {
        Write-Info "启动测试环境..."
        
        if ($Debug) {
            & ".\scripts\start-test-env.ps1" -Debug
        } else {
            & ".\scripts\start-test-env.ps1"
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-Error "启动测试环境失败"
            exit 1
        }
    } else {
        Write-Success "测试环境已运行"
    }
}

# 运行测试
Write-Info "运行集成测试..."
./gradlew test --tests $TestClass --info

$testResult = $LASTEXITCODE

# 显示结果
if ($testResult -eq 0) {
    Write-Success "集成测试通过！"
} else {
    Write-Error "集成测试失败"
}

# 停止环境（如果需要）
if ($StopAfter) {
    Write-Info "停止测试环境..."
    & ".\scripts\stop-test-env.ps1"
}

# 显示测试报告位置
Write-Info "测试报告位置: build/reports/tests/test/index.html"

exit $testResult
