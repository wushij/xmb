-- =============================================
-- 小卖部数据库初始化脚本（完整版）
-- 数据库名：xmb
-- 包含所有增量更新
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `xmb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `xmb`;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
    `birthday` VARCHAR(20) DEFAULT NULL COMMENT '生日',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT DEFAULT 0 COMMENT '排序（越小越靠前）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 插入分类数据
INSERT INTO `category` (`id`, `name`, `icon`, `sort`) VALUES
(1, '泡面专区', '/images/banner/泡面.png', 1),
(2, '零食小吃', '/images/banner/零食.png', 2),
(3, '烤肠', '/images/banner/烤肠 (1).png', 3),
(4, '早餐速食', '/images/banner/早餐.png', 4),
(5, '冰品冷饮', '/images/banner/图标-休闲.png', 5),
(6, '日用品', '/images/banner/日用品.png', 6);

-- ----------------------------
-- 3. 商品表
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `now_price` DECIMAL(10,2) NOT NULL COMMENT '现价',
    `old_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `discount` VARCHAR(16) DEFAULT NULL COMMENT '折扣标签',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 插入商品数据
INSERT INTO `goods` (`id`, `category_id`, `name`, `image`, `now_price`, `old_price`, `discount`, `stock`, `sales`, `description`) VALUES
-- 泡面专区
(9, 1, '今麦郎红烧牛肉面', '/images/goods/今麦郎红烧牛肉面.png', 3.90, 4.50, '8.7折', 28, 156, '经典红烧口味，香气扑鼻'),
(7, 1, '统一老坛酸菜牛肉面', '/images/goods/统一老坛酸菜牛肉面.png', 5.50, 5.50, NULL, 22, 89, '老坛酸菜，酸爽开胃'),
-- 零食小吃
(15, 2, '大大泡泡糖（混合味）', '/images/goods/大大泡泡糖（混合味）.png', 1.50, 2.00, '7.5折', 50, 320, '多种口味，童年回忆'),
(16, 2, '唐僧肉（蜜饯）', '/images/goods/唐僧肉（蜜饯）.png', 2.80, 2.80, NULL, 32, 78, '经典蜜饯，甜而不腻'),
-- 烤肠
(24, 3, '火山石烤肠（黑胡椒味）', '/images/goods/火山石烤肠（黑胡椒味）.png', 3.50, 4.00, '8.8折', 18, 245, '黑胡椒风味，肉质鲜嫩'),
(22, 3, '双汇玉米烤肠', '/images/goods/双汇玉米烤肠.png', 3.30, 3.30, NULL, 29, 167, '玉米香甜，口感丰富'),
-- 早餐速食
(43, 4, '三明治（火腿味）', '/images/goods/三明治（火腿味）.png', 5.20, 6.50, '8.0折', 12, 98, '火腿三明治，营养早餐'),
(40, 4, '桃李面包（原味）', '/images/goods/桃李面包（原味）.png', 4.00, 4.00, NULL, 21, 156, '松软香甜，早餐必备'),
-- 冰品冷饮
(53, 5, '可口可乐（500ml）', '/images/goods/可口可乐（500ml）.png', 2.80, 3.50, '8.0折', 45, 567, '经典可乐，畅爽一夏'),
(54, 5, '百事可乐（500ml）', '/images/goods/百事可乐（500ml）.png', 3.50, 3.50, NULL, 42, 345, '百事可乐，年轻一代的选择'),
(61, 5, '百岁山矿泉水', '/images/goods/百岁山矿泉水.png', 2.80, 3.50, '8.0折', 36, 234, '矿泉水中的贵族'),
(59, 5, '农夫山泉矿泉水', '/images/goods/农夫山泉矿泉水.png', 2.50, 2.50, NULL, 50, 456, '大自然的搬运工'),
-- 日用品
(64, 6, '纸巾（小包）', '/images/goods/纸巾（小包）.png', 1.20, 1.50, '8.0折', 60, 189, '便携纸巾，随身携带'),
(65, 6, '湿巾（10片装）', '/images/goods/湿巾（10片装）.png', 3.00, 3.00, NULL, 35, 123, '温和湿巾，清洁方便');

-- ----------------------------
-- 4. 购物车表
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `num` INT DEFAULT 1 COMMENT '商品数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中：0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 5. 收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(32) DEFAULT NULL COMMENT '省',
    `city` VARCHAR(32) DEFAULT NULL COMMENT '市',
    `district` VARCHAR(32) DEFAULT NULL COMMENT '区',
    `detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ----------------------------
-- 6. 订单表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `address_id` BIGINT DEFAULT NULL COMMENT '收货地址ID',
    `delivery_type` TINYINT DEFAULT 0 COMMENT '配送方式：0-配送 1-自提',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '订单总价',
    `delivery_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    `pay_price` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-配送中/待取货 3-已完成 4-已取消 5-退款中 6-已退款',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 7. 订单商品表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `goods_id` BIGINT NOT NULL COMMENT '商品ID',
    `goods_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `goods_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `goods_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `num` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- ----------------------------
-- 8. 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key` VARCHAR(64) NOT NULL COMMENT '配置键名',
    `config_value` VARCHAR(255) DEFAULT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认配送配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('delivery_fee', '3', '配送费金额'),
('free_delivery_threshold', '19.9', '满免配送费门槛'),
('min_order_amount', '12', '起送价格');

SET FOREIGN_KEY_CHECKS = 1;
