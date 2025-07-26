# Station 服务集成测试指南

## 概述

本指南介绍如何使用 Docker 容器环境运行 Station 服务的集成测试，替代之前不稳定的 Testcontainers 方案。

## 架构说明

### 测试环境组件

- **PostgreSQL (PostGIS)**: 主数据库，支持地理空间数据
- **Redis**: 缓存和分布式锁
- **Kafka + Zookeeper**: 消息队列
- **调试工具** (可选): Kafka UI, Redis Commander, pgAdmin

### 与网关服务的关系

Station 服务的测试环境可以与网关服务共享基础中间件：

- **网络**: 使用相同的 `test-network` (172.25.0.0/16)
- **服务名**: 保持一致的命名规范 (`*-test`)
- **端口**: 避免冲突的端口映射

## 快速开始

### 1. 启动测试环境

```powershell
# 启动基础服务
.\scripts\start-test-env.ps1

# 启动包含调试工具的完整环境
.\scripts\start-test-env.ps1 -Debug
```

### 2. 运行集成测试

```powershell
# 运行所有集成测试
./gradlew test --tests '*IntegrationTest'

# 运行特定的集成测试
./gradlew test --tests 'UpstreamDataFlowIntegrationTest'
```

### 3. 停止测试环境

```powershell
# 停止服务（保留数据）
.\scripts\stop-test-env.ps1

# 停止服务并清理数据
.\scripts\stop-test-env.ps1 -CleanVolumes
```

## 服务访问信息

### 基础服务

| 服务 | 地址 | 凭据 |
|------|------|------|
| PostgreSQL | localhost:5432 | 用户: test, 密码: test, 数据库: station_test |
| Redis | localhost:6379 | 无密码 |
| Kafka | localhost:9092 | 无认证 |

### 调试工具 (使用 -Debug 参数时)

| 工具 | 地址 | 凭据 |
|------|------|------|
| Kafka UI | http://localhost:8082 | 无需登录 |
| Redis Commander | http://localhost:8084 | 无需登录 |
| pgAdmin | http://localhost:8085 | 用户: admin@test.com, 密码: admin123 |

## 测试配置

### 配置文件

- `application-integration.yml`: 集成测试专用配置
- `application-test.yml`: 单元测试配置 (使用 H2 内存数据库)

### 测试 Profile

```java
@SpringBootTest(classes = StationServiceApplication.class)
@ActiveProfiles("integration")  // 使用 integration profile
class MyIntegrationTest {
    // 测试代码
}
```

## 常见问题

### Q: 容器启动失败怎么办？

1. 检查 Docker Desktop 是否运行
2. 检查端口是否被占用
3. 查看容器日志：`docker-compose -f docker-compose.test.yml logs [service-name]`

### Q: 测试连接数据库失败？

1. 确认 PostgreSQL 容器健康状态：`docker-compose -f docker-compose.test.yml ps`
2. 检查数据库连接配置是否正确
3. 尝试手动连接测试：`psql -h localhost -p 5432 -U test -d station_test`

### Q: Kafka 消息发送失败？

1. 确认 Kafka 和 Zookeeper 容器都已启动
2. 检查 Kafka 主题是否自动创建
3. 使用 Kafka UI 查看集群状态

### Q: 如何与网关服务共享环境？

如果网关服务已经启动了基础中间件，可以：

1. 停止 Station 的测试环境
2. 修改 `application-integration.yml` 中的连接地址
3. 确保网络连通性

## 最佳实践

### 1. 测试隔离

- 每个测试类使用 `@DirtiesContext` 确保上下文隔离
- 在 `@BeforeEach` 中清理测试数据
- 使用事务回滚避免数据污染

### 2. 性能优化

- 复用容器实例，避免频繁启停
- 使用健康检查确保服务就绪
- 合理设置连接池大小

### 3. 调试技巧

- 使用调试工具查看中间件状态
- 启用 SQL 日志查看数据库操作
- 使用 Kafka UI 监控消息流

## 故障排除

### 容器状态检查

```powershell
# 查看所有容器状态
.\scripts\start-test-env.ps1 -Status

# 查看特定服务日志
docker-compose -f docker-compose.test.yml logs postgres-test
docker-compose -f docker-compose.test.yml logs kafka-test
```

### 网络连接测试

```powershell
# 测试 PostgreSQL 连接
docker exec postgres-test pg_isready -U test -d station_test

# 测试 Redis 连接
docker exec redis-test redis-cli ping

# 测试 Kafka 连接
docker exec kafka-test kafka-topics --bootstrap-server localhost:9092 --list
```

### 数据清理

```powershell
# 清理所有测试数据
.\scripts\stop-test-env.ps1 -CleanVolumes

# 重新启动干净环境
.\scripts\start-test-env.ps1
```
