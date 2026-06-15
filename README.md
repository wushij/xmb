# 小卖部（XMB）—— 微信小程序商城项目分析

## 一、项目概述

**项目名称**：小卖部（xmb）

**项目定位**：一个面向校园/社区场景的轻量级电商小程序，支持商品浏览、购物车、下单、配送/自提等完整购物流程，并配备 Web 后台管理系统。

**线上域名**：`wushij.online`（后端 API 部署地址）

**微信小程序 AppID**：`wx53794b90690ca624`

---

## 二、系统架构

项目采用**前后端分离 + 三端架构**：

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   微信小程序      │    │   Admin 后台      │    │   后端 API       │
│  (WXML+WXSS+JS) │    │  (Vue3 + Vite)   │    │ (Spring Boot)   │
│  用户端          │    │  管理端           │    │  服务端          │
└────────┬────────┘    └────────┬────────┘    └────────┬────────┘
         │                      │                      │
         └──────────────────────┼──────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │     MySQL + Redis     │
                    │      数据层            │
                    └───────────────────────┘
```

| 端 | 技术栈 | 说明 |
|---|---|---|
| **小程序端（用户端）** | 微信原生开发（WXML/WXSS/JS） | 用户购物入口 |
| **Admin 后台（管理端）** | Vue 3 + Vite 8 + Element Plus + ECharts + Pinia | 商家管理后台 |
| **后端服务** | Spring Boot 2.7 + MyBatis-Plus + Sa-Token + Knife4j | RESTful API 服务 |
| **数据库** | MySQL 8.0 + Redis | 持久化 + 缓存/Session |

---

## 三、目录结构详解

```
xiaomaibutml/
├── pages/                  # 小程序页面（16个页面）
│   ├── index/              # 首页（分类导航 + 热销商品）
│   ├── category/           # 分类页（左右联动分类浏览）
│   ├── cart/               # 购物车
│   ├── my/                 # 我的（个人中心）
│   ├── goods/              # 商品详情页
│   ├── login/              # 登录页（微信授权登录）
│   ├── about/              # 关于页面
│   ├── address/            # 收货地址管理
│   ├── profile/            # 个人资料编辑
│   ├── settings/           # 设置页
│   └── order/              # 订单相关（6个子页面）
│       ├── createOrder/    # 确认下单页
│       ├── allOrder/       # 全部订单
│       ├── waitPay/        # 待付款
│       ├── waitDelivery/   # 待收货/待取货
│       ├── refunded/       # 已退款
│       └── orderDetail/    # 订单详情
├── components/             # 小程序自定义组件
│   └── goods-item/         # 商品卡片组件（全局注册）
├── utils/                  # 小程序工具类
│   ├── request.js          # 网络请求封装（wx.request）
│   └── storage.js          # 本地缓存工具
├── images/                 # 小程序本地图标/图片资源
├── static/css/             # 全局样式（iconfont）
├── app.js                  # 小程序入口（全局配置、工具函数）
├── app.json                # 小程序全局配置（页面、TabBar）
│
├── backend/                # 后端 Spring Boot 项目
│   ├── src/main/java/com/xmb/
│   │   ├── XmbApplication.java     # 启动类
│   │   ├── common/                 # 公共类（Result、OrderStatus、PageResult）
│   │   ├── config/                 # 配置类（Redis、MyBatis-Plus、Sa-Token、Knife4j、Web）
│   │   ├── controller/             # 控制器层（10个Controller）
│   │   ├── dto/                    # 数据传输对象
│   │   ├── entity/                 # 实体类（8张表）
│   │   ├── exception/              # 全局异常处理
│   │   ├── mapper/                 # MyBatis Mapper 接口
│   │   ├── service/                # 业务逻辑层（接口 + 实现）
│   │   ├── task/                   # 定时任务（订单超时取消）
│   │   └── vo/                     # 视图对象
│   ├── src/main/resources/
│   │   └── application.yml         # 应用配置
│   ├── images/                     # 后端商品图片存储
│   ├── uploads/                    # 上传文件存储
│   └── pom.xml                     # Maven 依赖
│
├── admin/                  # 后台管理前端项目
│   ├── src/
│   │   ├── api/index.js            # API 接口定义
│   │   ├── layout/index.vue        # 布局组件（侧边栏+头部）
│   │   ├── router/index.js         # 路由配置
│   │   ├── store/user.js           # Pinia 用户状态
│   │   ├── utils/request.js        # Axios 请求封装
│   │   └── views/                  # 页面
│   │       ├── dashboard/          # 数据统计面板
│   │       ├── goods/              # 商品管理
│   │       ├── category/           # 分类管理
│   │       ├── order/              # 订单管理
│   │       ├── config/             # 配送配置
│   │       └── login/              # 管理员登录
│   └── package.json
│
└── sql/
    └── xmb.sql             # 数据库初始化脚本
```

---

## 四、数据库设计

共 **8 张数据表**，使用 MySQL 8.0 + utf8mb4 编码：

| 表名 | 说明 | 核心字段 |
|---|---|---|
| `user` | 用户表 | openid, nickname, avatar, phone, gender, birthday |
| `category` | 商品分类表 | name, icon, sort, status |
| `goods` | 商品表 | category_id, name, image, now_price, old_price, discount, stock, sales |
| `cart` | 购物车表 | user_id, goods_id, num, selected（联合唯一约束） |
| `address` | 收货地址表 | user_id, name, phone, province/city/district, detail, is_default |
| `orders` | 订单表 | order_no, user_id, delivery_type, total_price, delivery_fee, pay_price, status |
| `order_item` | 订单商品表 | order_id, goods_id, goods_name, goods_image, goods_price, num |
| `system_config` | 系统配置表 | config_key, config_value（配送费、免配送门槛、起送价） |

**设计特点**：
- 所有业务表支持**逻辑删除**（`deleted` 字段）
- 自动维护 `create_time` / `update_time` 时间戳
- 购物车表设置 `user_id + goods_id` 联合唯一约束，避免重复添加
- 订单表存储完整的配送方式和费用信息

### 订单状态流转

```
待支付(0) ──支付──> 已支付(1) ──发货──> 配送中(2) ──收货──> 已完成(3)
    │                  │                                    
    │                  └──申请退款──> 退款中(5) ──审核──> 已退款(6)
    │
    └──超时/取消──> 已取消(4)
```

---

## 五、后端 API 设计

后端统一使用 `/api` 前缀，返回标准 `Result<T>` 格式 `{code, message, data}`。

### 5.1 用户端接口

| 模块 | 接口 | 方法 | 说明 |
|---|---|---|---|
| **认证** | `/api/auth/login` | POST | 微信 code 登录，返回 token |
| **分类** | `/api/category/list` | GET | 获取分类列表 |
| **商品** | `/api/goods/hot` | GET | 获取热销商品 |
| **商品** | `/api/goods/{id}` | GET | 获取商品详情 |
| **购物车** | `/api/cart/list` | GET | 获取购物车列表 |
| **购物车** | `/api/cart` | POST | 添加商品到购物车 |
| **购物车** | `/api/cart/num/{id}` | PUT | 修改购物车商品数量 |
| **购物车** | `/api/cart/{id}` | DELETE | 删除购物车商品 |
| **购物车** | `/api/cart/count` | GET | 获取购物车商品数量 |
| **订单** | `/api/order` | POST | 创建订单 |
| **订单** | `/api/order/page` | GET | 分页查询订单 |
| **订单** | `/api/order/{id}` | GET | 获取订单详情 |
| **订单** | `/api/order/pay/{id}` | PUT | 支付订单 |
| **订单** | `/api/order/cancel/{id}` | PUT | 取消订单 |
| **订单** | `/api/order/receive/{id}` | PUT | 确认收货 |
| **订单** | `/api/order/refund/{id}` | PUT | 申请退款 |
| **地址** | `/api/address/**` | CRUD | 收货地址增删改查 |
| **配置** | `/api/config/delivery` | GET | 获取配送配置 |
| **用户** | `/api/user/**` | GET/PUT | 用户信息查询与修改 |

### 5.2 管理端接口

| 模块 | 接口 | 说明 |
|---|---|---|
| **登录** | `/api/admin/login` | 管理员账号密码登录 |
| **统计** | `/api/admin/statistics` | 获取统计数据（支持按日/周/月） |
| **统计** | `/api/admin/sales-compare` | 日/月/年销售对比分析 |
| **统计** | `/api/admin/order-trend` | 近 7 天订单趋势 |
| **统计** | `/api/admin/hot-goods` | 热销商品 Top5 |
| **分类** | `/api/admin/category/**` | 分类 CRUD |
| **商品** | `/api/admin/goods/**` | 商品 CRUD + 批量删除 + 上下架 |
| **订单** | `/api/admin/order/**` | 订单列表/详情/状态更新 |
| **上传** | `/api/admin/upload` | 文件上传（图片） |
| **配置** | `/api/admin/config/delivery` | 配送配置读写 |

---

## 六、核心技术实现

### 6.1 认证鉴权

- **用户端**：微信小程序 `wx.login` 获取 code → 后端调用微信 API 换取 openid → Sa-Token 生成 UUID 格式 token → 小程序存储到 globalData 和 Storage
- **管理端**：账号密码登录 → Sa-Token 生成 token → 存储到 localStorage
- **Token 传递**：请求头 `Authorization` 字段
- **Session 共享**：Sa-Token 整合 Redis，token 存储在 Redis（database 2）

### 6.2 权限隔离

- 用户接口和管理接口使用不同的 Controller 和 Service
- Sa-Token 拦截器配置区分 `/api/admin/**` 和 `/api/**` 路径

### 6.3 订单超时自动取消

- 使用 Spring `@Scheduled` 定时任务
- 每分钟执行一次，扫描超时未支付的订单自动取消
- 实现类：`OrderTimeoutTask`

### 6.4 配送费用计算

系统配置表维护三个核心参数：
- **起送价格**：`min_order_amount = ¥12`
- **配送费**：`delivery_fee = ¥3`
- **免配送费门槛**：`free_delivery_threshold = ¥19.9`

支持两种配送方式：
- **配送**（delivery_type=0）：满 ¥19.9 免配送费，否则加 ¥3
- **自提**（delivery_type=1）：免配送费

### 6.5 图片路径处理

`app.js` 中封装 `getImageUrl` 函数统一处理三种图片来源：
- `/uploads/` 开头 → 后端上传的图片，拼接 `https://wushij.online`
- `/images/` 开头且 UUID 文件名 → 后端内置图片，拼接域名
- 其他 → 小程序本地图标，直接使用

---

## 七、小程序端页面功能

### 7.1 TabBar 四页签

| Tab | 页面 | 功能 |
|---|---|---|
| 首页 | `pages/index` | 分类导航（6个分类图标）+ 热销商品瀑布流 + 购物车角标 |
| 分类 | `pages/category` | 左侧分类列表 + 右侧商品列表（左右联动） |
| 购物车 | `pages/cart` | 商品列表、数量增减、全选、起送价校验、去结算 |
| 我的 | `pages/my` | 用户信息、订单入口（待付款/待收货/已退款/全部）、地址管理、设置 |

### 7.2 购物流程

```
浏览商品 → 加入购物车 → 购物车结算 → 选择地址/配送方式 → 确认下单 
→ 支付（模拟） → 等待配送/自提 → 确认收货 → 完成
```

### 7.3 自定义组件

- `goods-item`：全局注册的商品卡片组件，复用于首页、分类页等商品列表展示

---

## 八、Admin 后台功能

### 8.1 数据统计（Dashboard）

- **6 大统计卡片**：今日订单数、今日销售额、总订单数、总销售额、商品总数、分类总数
- **销售对比分析**：支持日/月/年维度的销售额与订单数对比，自动计算增长率
- **订单趋势图**：ECharts 折线图展示近 7 天订单走势
- **热销商品 Top5**：表格排名展示

### 8.2 商品管理

- 商品列表（分页、按名称/分类/状态筛选）
- 新增/编辑商品（含图片上传）
- 商品上下架、单个删除、批量删除

### 8.3 分类管理

- 分类列表展示
- 新增/编辑/删除分类（支持图标上传、排序）

### 8.4 订单管理

- 订单列表（按订单号/状态/日期范围筛选）
- 订单详情查看
- 订单状态流转操作（发货、退款审核等）

### 8.5 配送配置

- 起送价格、配送费、免配送费门槛的动态配置

---

## 九、技术栈总结

### 小程序端
| 技术 | 说明 |
|---|---|
| 微信小程序原生框架 | WXML + WXSS + JavaScript |
| wx.request 封装 | 统一请求拦截、Token 注入、401 自动跳转登录 |
| 全局组件 | goods-item 商品卡片 |

### 后端
| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis-Plus | 3.5.3 | ORM 框架，支持分页、逻辑删除 |
| Sa-Token | 1.37.0 | 轻量级权限认证，整合 Redis |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | Token 存储、Session 共享 |
| Knife4j | 3.0.3 | 接口文档（Swagger 增强） |
| Hutool | 5.8.23 | 工具类库 |
| Lombok | - | 简化实体类代码 |
| Java | 1.8 | JDK 版本 |

### 管理端
| 技术 | 版本 | 说明 |
|---|---|---|
| Vue | 3.5.30 | 前端框架 |
| Vite | 8.0.1 | 构建工具 |
| Element Plus | 2.13.6 | UI 组件库 |
| ECharts | 6.0.0 | 图表库 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 4.6.4 | 路由管理 |
| Axios | 1.14.0 | HTTP 请求 |

---

## 十、项目亮点与特色

1. **完整的电商闭环**：涵盖商品浏览→购物车→下单→支付→配送→收货→退款全流程
2. **三端统一**：小程序用户端 + Web 管理端 + Spring Boot 后端，共享同一套 API
3. **灵活的配送体系**：支持配送/自提两种模式，配送费满减规则可后台动态配置
4. **数据驱动决策**：管理端提供日/月/年多维度销售对比分析，ECharts 可视化趋势
5. **订单超时自动取消**：定时任务自动清理超时未支付订单，释放库存
6. **微信生态深度集成**：微信授权登录、头像昵称获取，符合最新小程序规范
7. **代码规范**：后端分层清晰（Controller → Service → Mapper），DTO/VO 分离，全局异常处理
8. **接口文档**：集成 Knife4j 自动生成 API 文档，方便前后端联调

---

## 十一、部署环境

| 项目 | 配置 |
|---|---|
| 后端端口 | `8082` |
| 数据库 | MySQL 8.0，库名 `xmb`，用户 `root` |
| Redis | 端口 `6379`，database `2` |
| 线上域名 | `wushij.online`（HTTPS） |
| API 基础路径 | `https://wushij.online/api` |
| 服务器管理 | 宝塔面板 |
| 文件上传限制 | 单文件 10MB |

---

## 十二、默认商品分类

| 分类 | 示例商品 |
|---|---|
| 泡面专区 | 今麦郎红烧牛肉面、统一老坛酸菜牛肉面 |
| 零食小吃 | 大大泡泡糖、唐僧肉（蜜饯） |
| 烤肠 | 火山石烤肠（黑胡椒味）、双汇玉米烤肠 |
| 早餐速食 | 三明治（火腿味）、桃李面包（原味） |
| 冰品冷饮 | 可口可乐、百事可乐、农夫山泉、百岁山 |
| 日用品 | 纸巾（小包）、湿巾（10片装） |
