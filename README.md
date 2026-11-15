# 碧海希 · 水果助农销售平台

面向果蔬助农场景的前后端分离电商项目，包含 **用户端、商家端、平台管理端** 三套角色，覆盖商品浏览与下单、商家入驻与店铺运营、库存管理、助农公益活动、在线客服与反馈处理等完整业务闭环。

> 绵阳城市学院第三学期课程实践项目。

---

## 技术栈

### 后端

| 分类 | 选型 |
| --- | --- |
| 框架 | Spring Boot 3.3.1 / Java 21 |
| 持久层 | MyBatis 3.0.3 + PageHelper 1.4.6 分页 |
| 数据库 | MySQL 8.x（`fruit` 库） |
| 缓存 | Redis（`spring-boot-starter-data-redis`） |
| 认证 | JWT（jjwt 0.11.5）+ 自定义登录拦截器 |
| 工具 | Hutool 5.8.40、Lombok、统一 `Result` 返回体 + 全局异常处理 |
| 构建 | Maven（maven-compiler-plugin 3.13.0） |

### 前端

| 分类 | 选型 |
| --- | --- |
| 框架 | Vue 3（`<script setup>`） |
| 构建 | Vite 4 |
| UI | Element Plus 2.x（按需自动引入 + Sass 主题定制） |
| 图表 | ECharts 6 |
| 请求 | Axios（统一封装于 `src/utils`） |
| 路由 | Vue Router 4 |
| 样式 | Sass / SCSS |

---

## 功能模块

| 端 | 功能 |
| --- | --- |
| 用户端 | 注册 / 登录、商品浏览与详情、购物车、下单与订单查询、收货地址、公告、意见反馈、在线客服、助农活动浏览与参与 |
| 商家端 | 商家入驻申请、店铺创建与管理、店铺商品管理、库存维护、销售数据查看 |
| 管理端 | 管理员登录、用户管理、商品与分类管理、库存管理、订单管理、商家入驻审核、公告发布、反馈处理、客服会话、助农活动管理、销售统计 |

---

## 目录结构

```
fruit/
├── fruit.sql                        # 建表语句 + 示例数据（25 张表）
├── springboot/                      # 后端工程
│   ├── pom.xml
│   ├── files/                       # 运行时上传目录（商品图、头像等示例图片）
│   └── src/main/
│       ├── java/com/example/
│       │   ├── SpringbootApplication.java
│       │   ├── common/              # Result 统一返回、CORS / Web 配置
│       │   ├── controller/          # Admin / Goods / Orders / Cart / Store / Charity 等 11 个控制器
│       │   ├── entity/              # 数据库实体
│       │   ├── exception/           # 全局异常处理
│       │   ├── interceptor/         # JWT 登录拦截
│       │   ├── mapper/              # MyBatis Mapper 接口
│       │   ├── service/ + impl/     # 业务层
│       │   └── utils/JwtUtil.java
│       └── resources/
│           ├── application.yml      # 端口、数据源、上传大小等配置
│           └── mapper/*.xml         # SQL 映射
└── vue/                             # 前端工程
    ├── package.json
    ├── vite.config.js               # Element Plus 按需引入、@ 别名、Sass 主题注入
    ├── .env.development             # 开发环境后端地址
    ├── .env.production
    ├── index.html
    └── src/
        ├── api/                     # 接口封装
        ├── assets/                  # css 主题、imgs 图片
        ├── components/              # HeaderNav、ChatWindow
        ├── router/index.js
        ├── utils/                   # axios.js、request.js
        └── views/
            ├── Login.vue / Register.vue / Manager.vue
            ├── manager/             # 管理端页面（商品、分类、库存、订单、用户、公告、反馈、客服、助农活动）
            ├── merchant/            # 商家端页面（入驻、店铺、商品）
            └── charity/             # 助农活动列表与详情
```

---

## 快速开始

### 环境要求

- JDK 21、Maven 3.9+
- MySQL 8.x
- Redis（默认 `localhost:6379`，后端启动依赖）
- Node.js 16+（建议 18/20 LTS）

### 1. 初始化数据库

```sql
CREATE DATABASE fruit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后导入根目录下的 `fruit.sql`：

```bash
mysql -u root -p fruit < fruit.sql
```

或用 Navicat / DataGrip 直接运行该 SQL 文件。

### 2. 启动后端

1. 修改 `springboot/src/main/resources/application.yml`，把 `username` / `password` 换成你本地 MySQL 的账号密码：

```yaml
spring:
  datasource:
    username: "root"
    password: "你的数据库密码"
    url: jdbc:mysql://localhost:3306/fruit?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true
```

2. 启动 Redis（Windows 可执行 `redis-server`，或用 Docker `docker run -d -p 6379:6379 redis`）。
3. 运行 `com.example.SpringbootApplication`，或命令行：

```bash
cd springboot
mvn spring-boot:run
```

服务端口 **9090**，文件资源前缀 `http://localhost:9090`。

### 3. 启动前端

```bash
cd vue
npm install
npm run dev
```

浏览器访问 **http://localhost:5173**（默认端口）。前端通过 `vue/.env.development` 中的 `VITE_BASE_URL=http://localhost:9090` 直连后端。

### 4. 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 平台管理员 | `admin` | `admin` |

用户端与商家端账号可在首页注册，或使用 `fruit.sql` 中的示例数据。

---

## 已知问题与待优化

- ~~`vue/.env.production` 中的 `VITE_BASE_URL` 写成了 `http://:9090`~~ 已修正为 `http://localhost:9090`；正式部署到服务器时再替换为实际域名或 IP。
- 数据库账号密码目前明文写在 `application.yml` 中，生产环境建议改为环境变量注入。
- `springboot/files` 为运行时上传目录，因示例数据依赖其中图片，故随仓库一并提交；后续可改为独立对象存储并加入 `.gitignore`。

---

## License

本项目为课程学习实践作品，仅用于学习交流。