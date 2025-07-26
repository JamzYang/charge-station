# 充电站运营平台 (Charge Station Service)

## 项目概述

充电站运营平台是一个基于DDD（领域驱动设计）架构的Spring Boot应用，用于管理充电站和充电桩的静态信息与动态状态，为上层业务提供统一的资产查询和设备控制能力。

## 技术栈

- **Java 21** - 核心开发语言
- **Spring Boot 3.3.x** - 应用框架
- **Spring Data JPA** - ORM框架
- **PostgreSQL 16+** - 主数据库
- **PostGIS** - 地理空间数据扩展
- **Redis 7.x** - 缓存和会话存储
- **Redisson** - Redis客户端和分布式锁
- **Kafka 3.6.x** - 消息队列
- **Flyway** - 数据库版本管理
- **Gradle** - 构建工具

## 项目结构

```
src/main/java/com/charge/station/
├── StationServiceApplication.java          # 启动类
├── config/                                 # 配置类
├── interfaces/                            # 接口层
│   ├── rest/                              # REST控制器
│   ├── dto/                               # 数据传输对象
│   └── assembler/                         # DTO转换器
├── application/                           # 应用层
│   ├── service/                           # 应用服务
│   ├── command/                           # 命令对象
│   └── handler/                           # 事件处理器
├── domain/                                # 领域层
│   ├── model/                             # 领域模型
│   ├── event/                             # 领域事件
│   ├── repository/                        # 仓储接口
│   └── service/                           # 领域服务
├── infrastructure/                        # 基础设施层
│   ├── persistence/                       # 持久化实现
│   ├── messaging/                         # 消息处理
│   └── external/                          # 外部服务集成
└── shared/                                # 共享组件
    ├── exception/                         # 异常定义
    ├── util/                              # 工具类
    └── constant/                          # 常量定义
```

## 快速开始

### 前置条件

1. Java 21
2. PostgreSQL 16+ (带PostGIS扩展)
3. Redis 7.x
4. Kafka 3.6.x

### 数据库准备

```sql
-- 创建数据库
CREATE DATABASE charge_station;

-- 创建用户（可选）
CREATE USER charge_station_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE charge_station TO charge_station_user;
```

### 配置文件

复制 `src/main/resources/application.yml` 并根据你的环境修改数据库、Redis、Kafka连接信息。

### 启动应用

```bash
# 编译项目
./gradlew build

# 启动应用
./gradlew bootRun
```

### 验证启动

访问健康检查端点：
```bash
curl http://localhost:8080/health
```

访问Actuator健康检查：
```bash
curl http://localhost:8080/actuator/health
```

## 开发指南

### DDD分层架构

本项目采用DDD分层架构：

1. **接口层 (interfaces)**: 处理HTTP请求，参数校验，DTO转换
2. **应用层 (application)**: 编排业务流程，事务管理，调用领域服务
3. **领域层 (domain)**: 核心业务逻辑，领域规则，领域事件
4. **基础设施层 (infrastructure)**: 技术实现，数据持久化，外部集成

### 代码规范

- 使用Java 21的新特性（如Record、Pattern Matching等）
- 遵循DDD设计原则，保持领域层的纯净性
- 使用事务性发件箱模式处理事件发布
- 统一异常处理和API响应格式

### 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests "com.charge.station.*"
```

## API文档

启动应用后，访问 Swagger UI：
http://localhost:8080/swagger-ui.html

## 监控

应用集成了Spring Boot Actuator，提供以下监控端点：

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 指标信息
- `/actuator/prometheus` - Prometheus指标

## 部署

### Docker部署

```bash
# 构建Docker镜像
./gradlew bootBuildImage

# 运行容器
docker run -p 8080:8080 charge-station:1.0.0
```

### Kubernetes部署

参考 `k8s/` 目录下的部署文件。

## 开发计划

当前完成状态：**阶段一 - 项目初始化与核心框架搭建** ✅

- [x] 1.1 项目脚手架创建
- [x] 1.2 搭建DDD分层结构  
- [x] 1.3 配置与集成 (DB, Redis, Kafka)
- [x] 1.4 实现全局通用组件
- [x] 1.5 数据库初始化

下一步：**阶段二 - 充电资产管理 (CRUD)**

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
