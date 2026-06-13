# AnimeShop 🛒

二次元动漫电商平台 —— 每个年轻程序员学习 Redis 的必不可少的一环。

## 项目简介

AnimeShop 是一个以动漫周边商品为主题的电商平台，采用前后端分离架构。后端基于 Spring Boot 4 + Java 21，前端基于 Vue 3 + TypeScript + Vite。项目深度整合了 **Redis 缓存**、**RabbitMQ 消息队列** 和 **JWT 认证**，覆盖电商核心场景：商品浏览、购物车、订单、秒杀、用户管理等。

> 该项目源于学习 Redis 实战的典型电商项目，在此基础上扩展了完整的秒杀系统、消息驱动缓存刷新、分布式 ID 生成等生产级特性。

## 技术栈

### 后端

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 4.0.0 |
| 语言 | Java | 21 |
| ORM | MyBatis-Plus | 3.5.15 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis + Spring Data Redis | - |
| 消息队列 | RabbitMQ | - |
| 安全 | Spring Security + JWT | 0.13.0 (jjwt) |
| 分布式 ID | 雪花算法 (SnowflakeIdWorker) | - |
| 工具 | Hutool, FastJSON, Lombok | - |

### 前端

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 | ^3.5.32 |
| 构建工具 | Vite | ^8.0.8 |
| 语言 | TypeScript | ~6.0.0 |
| UI 组件库 | Element Plus | ^2.13.7 |
| 状态管理 | Pinia + pinia-plugin-persistedstate | ^3.0.4 |
| HTTP 客户端 | Axios | ^1.16.0 |
| CSS 预处理器 | Sass | ^1.99.0 |
| 路由 | Vue Router | ^4.6.4 |
| 代码规范 | ESLint + Prettier | - |

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────┐  │
│  │ 用户端   │  │ 管理端   │  │ 购物车   │  │ 登录/注册 │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └─────┬─────┘  │
│       └──────────────┴─────────────┴───────────────┘       │
│                         │ Axios + JWT                       │
└─────────────────────────┼───────────────────────────────────┘
                          │ /api (Vite 代理 → localhost:8080)
┌─────────────────────────┼───────────────────────────────────┐
│                Spring Boot 后端                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Controller 层 (REST API)                │   │
│  │  Home / Product / Cart / Order / Seckill / User / ..│   │
│  └──────────────────────┬───────────────────────────────┘   │
│  ┌──────────────────────┴───────────────────────────────┐   │
│  │                Service 层 (业务逻辑)                  │   │
│  │   缓存穿透/击穿保护 · 库存扣减 · 订单状态流转         │   │
│  └──┬──────────────┬──────────────┬────────────────────┘   │
│  ┌──┴──────────┐ ┌─┴──────────┐ ┌─┴──────────────────┐    │
│  │   Mapper    │ │   Redis    │ │    RabbitMQ         │    │
│  │ (MyBatis-   │ │ (缓存 ·    │ │ (消息驱动缓存刷新 · │    │
│  │  Plus)      │ │  分布式锁)  │ │  异步订单处理)      │    │
│  └──────┬──────┘ └────────────┘ └────────────────────┘    │
│  ┌──────┴──────┐                                           │
│  │   MySQL     │                                           │
│  └─────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```

## 功能模块

### 用户端
- **首页** — 轮播 Banner、热销商品、新品上市、秒杀商品展示
- **商品** — 分类浏览、商品详情（多图、HTML 描述）
- **购物车** — 添加/删除/修改数量、选中计算、登录后合并本地购物车
- **订单** — 创建订单、订单列表、取消订单、订单状态追踪
- **秒杀** — 秒杀倒计时、秒杀下单（库存原子扣减）
- **地址管理** — 增删改查、设置默认地址
- **用户中心** — 个人信息、修改密码

### 管理端
- 商品管理（上架/下架/编辑）
- 订单管理
- 分类管理

### 后端特色
- **消息驱动缓存刷新** — RabbitMQ 广播缓存变更，各节点消费后刷新本地缓存，防止缓存击穿
- **缓存穿透保护** — 空值缓存，防止恶意查询穿透到数据库
- **逻辑过期** — 缓存项不设物理 TTL，由消息驱动刷新，避免缓存雪崩
- **分布式 ID** — 雪花算法生成订单号，支持多节点部署
- **秒杀预热** — 定时任务每分钟预热即将开始的秒杀库存到 Redis
- **定时统计** — 每日凌晨统计热销商品和新品

## 快速开始

### 前置条件

- MySQL 8.x（端口 3306）
- Redis（端口 6379）
- RabbitMQ（端口 5672）
- JDK 21+
- Node.js >= 20.19.0 || >= 22.12.0
- pnpm

### 数据库初始化

```sql
CREATE DATABASE animeshop DEFAULT CHARACTER SET utf8mb4;
```

项目不包含 Flyway/Liquibase 迁移脚本，表结构由 MyBatis-Plus 实体类映射。可使用 `AnimeShopBackend/src/main/resources/mapper/` 下的 XML 文件和实体类作为建表参考。

### 启动后端

```bash
cd AnimeShopBackend

# 确保 MySQL、Redis、RabbitMQ 已启动

# 编译并启动
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 启动前端

```bash
cd AnimeShop_front

# 安装依赖
pnpm install

# 开发服务器（热重载）
pnpm dev
```

前端默认运行在 `http://localhost:5173`，Vite 将 `/api` 代理到后端 `localhost:8080`。

## 项目结构

```
AnimeiShop/
├── AnimeShopBackend/              # 后端 (Spring Boot)
│   ├── src/main/java/com/dong/
│   │   ├── common/                 # 公用组件
│   │   │   ├── Result.java         # 统一响应包装
│   │   │   ├── SnowflakeIdWorker   # 雪花 ID 生成器
│   │   │   ├── RedisCatch*.java    # 缓存穿透/击穿保护
│   │   │   └── RabbitMQ*.java      # 通用消息生产者/消费者
│   │   ├── config/                 # 配置类
│   │   │   ├── SecurityConfig.java # Spring Security + JWT
│   │   │   ├── RedisConfig.java    # Redis 配置
│   │   │   ├── RabbitMQConfig.java # 交换机/队列/绑定
│   │   │   └── MybatisPlusConfig   # 分页插件
│   │   ├── controller/             # REST API 入口
│   │   ├── service/                # 业务逻辑接口 + 实现
│   │   ├── mapper/                 # MyBatis-Plus 数据访问
│   │   ├── entity/                 # 数据库实体
│   │   ├── dto/                    # 请求参数
│   │   ├── vo/                     # 响应对象
│   │   ├── security/               # JWT TokenFilter
│   │   ├── producer/               # RabbitMQ 消息生产者
│   │   ├── consumer/               # RabbitMQ 消息消费者
│   │   ├── exception/              # 全局异常处理
│   │   ├── schedTask/              # 定时任务
│   │   └── utils/                  # 工具类
│   └── src/main/resources/
│       ├── application.yml         # 配置文件
│       └── mapper/                 # MyBatis XML 映射
│
├── AnimeShop_front/                # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/                    # API 调用封装
│   │   ├── assets/                 # 静态资源 + 全局样式
│   │   ├── components/             # 公共组件
│   │   ├── layouts/                # 布局（用户/管理/空）
│   │   ├── router/                 # 路由 + 导航守卫
│   │   ├── stores/                 # Pinia 状态管理
│   │   │   ├── userStore.ts        # 用户状态（持久化）
│   │   │   └── cartStores.ts       # 购物车状态（乐观更新）
│   │   ├── utils/                  # 工具（Axios 封装等）
│   │   └── views/                  # 页面组件
│   ├── vite.config.ts              # Vite 配置
│   ├── package.json
│   └── tsconfig*.json
│
├── .claude/                        # Claude AI 配置
└── README.md
```

## 关键设计

### 缓存策略

采用三级防护应对缓存问题：
1. **缓存穿透** — 查询不存在的数据时，将空值写入 Redis（短 TTL），防止重复查库
2. **缓存击穿** — 热点数据通过 RabbitMQ 发送缓存刷新消息，消费者异步重建缓存
3. **逻辑过期** — 缓存项不设物理 TTL，由定时任务或写操作触发刷新，避免大量缓存同时过期

### JWT 认证

- 登录成功返回 JWT token（1 小时过期）
- `TokenFilter` 从请求头提取 token 并验证
- `SecurityContextHolder` 持有当前用户信息
- 密码编码器配置为 `BCryptPasswordEncoder`

### 购物车状态同步

- 本地购物车（未登录）存储在 Pinia + localStorage
- 登录后自动合并本地购物车到服务端
- 乐观更新：修改立即反映在 UI，请求失败时回滚
- RabbitMQ 广播购物车变更，多节点消费后刷新缓存

### 秒杀系统

- 定时任务每分钟预热即将开始的秒杀库存到 Redis
- 秒杀下单使用 Redis 原子扣减库存
- 通过 RabbitMQ 异步处理秒杀订单，削峰填谷
- 订单过期时间：秒杀订单 10 分钟，普通订单 30 分钟

## 常用命令

### 后端

```bash
cd AnimeShopBackend
./mvnw spring-boot:run        # 启动后端
./mvnw clean compile           # 编译
./mvnw test                    # 运行测试
./mvnw clean package           # 打包 JAR
```

### 前端

```bash
cd AnimeShop_front
pnpm install                   # 安装依赖
pnpm dev                       # 启动开发服务器
pnpm build                     # 类型检查 + 生产构建
pnpm lint                      # ESLint 修复
pnpm format                    # Prettier 格式化
pnpm type-check                # 仅类型检查
pnpm preview                   # 预览生产构建
```

## 配置说明

主要配置位于 `AnimeShopBackend/src/main/resources/application.yml`：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 后端端口 | 8080 |
| `spring.datasource.url` | MySQL 连接 | `jdbc:mysql://localhost:3306/animeshop` |
| `spring.data.redis.host` | Redis 地址 | 127.0.0.1 |
| `spring.rabbitmq.host` | RabbitMQ 地址 | localhost |
| `jwt.secret` | JWT 签名密钥 | (需修改为安全密钥) |
| `jwt.expiration` | JWT 过期时间 | 3600000 (1h) |
| `snowflake.workerId` | 雪花算法 Worker ID | 1 |
| `snowflake.datacenterId` | 雪花算法数据中心 ID | 1 |

## License

MIT
