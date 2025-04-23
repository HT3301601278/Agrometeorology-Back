/*
 Navicat Premium Dump SQL

 Source Server         : MySQL5.7
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : localhost:3305
 Source Schema         : agrometeorology

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 23/04/2025 16:09:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alert_record
-- ----------------------------
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预警记录ID',
  `rule_id` bigint(20) NOT NULL COMMENT '预警规则ID',
  `forecast_dt` bigint(20) NOT NULL COMMENT '预报时间戳',
  `forecast_date` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '预报日期格式化字符串',
  `latitude` decimal(10, 2) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 2) NOT NULL COMMENT '经度',
  `param_value` decimal(10, 2) NOT NULL COMMENT '触发预警的参数值',
  `param_value2` decimal(10, 2) NULL DEFAULT NULL COMMENT '第二参数值（如果有）',
  `message` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '预警消息内容',
  `created_at` datetime NOT NULL COMMENT '记录创建时间',
  `field_id` bigint(20) NOT NULL COMMENT '地块ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkdplqp4m75oxr8lrt2uk4dlhf`(`field_id`) USING BTREE,
  INDEX `FKnjr9ajtrhgi7rkrmv9dcbhuvg`(`rule_id`) USING BTREE,
  CONSTRAINT `FKkdplqp4m75oxr8lrt2uk4dlhf` FOREIGN KEY (`field_id`) REFERENCES `field` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKnjr9ajtrhgi7rkrmv9dcbhuvg` FOREIGN KEY (`rule_id`) REFERENCES `alert_rule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '气象预警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for alert_rule
-- ----------------------------
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预警规则ID',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '规则名称',
  `type` int(11) NOT NULL COMMENT '规则类型：1-温度,2-湿度,3-降水,4-风害,5-光照,6-气压',
  `sub_type` int(11) NOT NULL COMMENT '规则子类型：如温度类型下1-高温,2-低温',
  `param_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '参数名称，如tempMax、humidity等',
  `operator` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '运算符，如>,<,>=,<=,==',
  `threshold` decimal(10, 2) NOT NULL COMMENT '阈值',
  `param_name2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第二参数名称（可选）',
  `operator2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第二运算符（可选）',
  `threshold2` decimal(10, 2) NULL DEFAULT NULL COMMENT '第二阈值（可选）',
  `message` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '预警消息模板',
  `enabled` bit(1) NOT NULL COMMENT '是否启用',
  `email_notify` bit(1) NOT NULL COMMENT '是否发送邮件通知',
  `system_notify` bit(1) NOT NULL COMMENT '是否发送系统通知',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL COMMENT '更新时间',
  `field_id` bigint(20) NOT NULL COMMENT '地块ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3yvx0efhadnmo6tyn79qpekhr`(`field_id`) USING BTREE,
  INDEX `FK1ri0fg876op2e7ppa1aolcawd`(`user_id`) USING BTREE,
  CONSTRAINT `FK1ri0fg876op2e7ppa1aolcawd` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK3yvx0efhadnmo6tyn79qpekhr` FOREIGN KEY (`field_id`) REFERENCES `field` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '气象预警规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告内容',
  `type` int(11) NOT NULL COMMENT '公告类型',
  `status` int(11) NOT NULL COMMENT '公告状态',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for field
-- ----------------------------
DROP TABLE IF EXISTS `field`;
CREATE TABLE `field`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '田地ID',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '田地名称',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `group_id` bigint(20) NULL DEFAULT NULL COMMENT '田地组ID',
  `latitude` decimal(10, 2) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 2) NOT NULL COMMENT '经度',
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '面积(亩)',
  `soil_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '土壤类型',
  `crop_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '作物类型',
  `growth_stage` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生长阶段',
  `planting_season` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '种植季节',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for field_group
-- ----------------------------
DROP TABLE IF EXISTS `field_group`;
CREATE TABLE `field_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '田地组ID',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '组名称',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '通知内容',
  `type` int(11) NOT NULL COMMENT '通知类型',
  `is_read` bit(1) NOT NULL COMMENT '是否已读',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKb0yvoep4h4k92ipon31wmdf7e`(`user_id`) USING BTREE,
  CONSTRAINT `FKb0yvoep4h4k92ipon31wmdf7e` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification_setting
-- ----------------------------
DROP TABLE IF EXISTS `notification_setting`;
CREATE TABLE `notification_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `system_notify` bit(1) NOT NULL COMMENT '系统通知开关',
  `email_notify` bit(1) NOT NULL COMMENT '邮件通知开关',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKbwsuroqorxx1boup2snb1t1u9`(`user_id`) USING BTREE,
  CONSTRAINT `FKbwsuroqorxx1boup2snb1t1u9` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置描述',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_npsxm1erd0lbetjn5d3ayrsof`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `nickname` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色',
  `status` bit(1) NOT NULL COMMENT '状态(1启用,0禁用)',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_sb8bbouer5wak8vyiiy4pf2bx`(`username`) USING BTREE,
  UNIQUE INDEX `UK_ob8kqyqqgmefl0aco34akdtpe`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for weather_current
-- ----------------------------
DROP TABLE IF EXISTS `weather_current`;
CREATE TABLE `weather_current`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '当前天气ID',
  `latitude` decimal(10, 2) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 2) NOT NULL COMMENT '经度',
  `dt` bigint(20) NOT NULL COMMENT '数据时间戳',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地点名称',
  `country` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '国家代码',
  `timezone` int(11) NULL DEFAULT NULL COMMENT '时区',
  `sunrise` bigint(20) NULL DEFAULT NULL COMMENT '日出时间',
  `sunset` bigint(20) NULL DEFAULT NULL COMMENT '日落时间',
  `temp` decimal(5, 2) NOT NULL COMMENT '温度',
  `feels_like` decimal(5, 2) NULL DEFAULT NULL COMMENT '体感温度',
  `temp_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低温度',
  `temp_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高温度',
  `pressure` int(11) NULL DEFAULT NULL COMMENT '大气压力(hPa)',
  `sea_level` int(11) NULL DEFAULT NULL COMMENT '海平面气压(hPa)',
  `grnd_level` int(11) NULL DEFAULT NULL COMMENT '地面气压(hPa)',
  `humidity` int(11) NULL DEFAULT NULL COMMENT '湿度(%)',
  `visibility` int(11) NULL DEFAULT NULL COMMENT '能见度(米)',
  `wind_speed` decimal(5, 2) NULL DEFAULT NULL COMMENT '风速(m/s)',
  `wind_deg` int(11) NULL DEFAULT NULL COMMENT '风向(度)',
  `wind_gust` decimal(5, 2) NULL DEFAULT NULL COMMENT '阵风(m/s)',
  `clouds_all` int(11) NULL DEFAULT NULL COMMENT '云量(%)',
  `rain1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雨量(mm)',
  `snow1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雪量(mm)',
  `weather_id` int(11) NULL DEFAULT NULL COMMENT '天气代码',
  `weather_main` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气主要状况',
  `weather_description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气描述',
  `weather_icon` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气图标',
  `created_at` datetime NOT NULL COMMENT '数据创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKf490mp5tlo8xekcyuag0jy08h`(`latitude`, `longitude`, `dt`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for weather_forecast
-- ----------------------------
DROP TABLE IF EXISTS `weather_forecast`;
CREATE TABLE `weather_forecast`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '天气预报ID',
  `latitude` decimal(10, 2) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 2) NOT NULL COMMENT '经度',
  `dt` bigint(20) NOT NULL COMMENT '预报时间戳',
  `dt_txt` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '预报时间文本',
  `forecast_type` tinyint(4) NOT NULL COMMENT '预报类型(1:小时级预报,2:16天预报,3:30天气候预报)',
  `sunrise` bigint(20) NULL DEFAULT NULL COMMENT '日出时间',
  `sunset` bigint(20) NULL DEFAULT NULL COMMENT '日落时间',
  `temp` decimal(5, 2) NOT NULL COMMENT '温度',
  `feels_like` decimal(5, 2) NULL DEFAULT NULL COMMENT '体感温度',
  `temp_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低温度',
  `temp_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高温度',
  `pressure` int(11) NULL DEFAULT NULL COMMENT '大气压力(hPa)',
  `humidity` int(11) NULL DEFAULT NULL COMMENT '湿度(%)',
  `visibility` int(11) NULL DEFAULT NULL COMMENT '能见度(米)',
  `wind_speed` decimal(5, 2) NULL DEFAULT NULL COMMENT '风速(m/s)',
  `wind_deg` int(11) NULL DEFAULT NULL COMMENT '风向(度)',
  `wind_gust` decimal(5, 2) NULL DEFAULT NULL COMMENT '阵风(m/s)',
  `clouds_all` int(11) NULL DEFAULT NULL COMMENT '云量(%)',
  `pop` decimal(3, 2) NULL DEFAULT NULL COMMENT '降水概率',
  `rain1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雨量(mm)',
  `snow1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雪量(mm)',
  `weather_id` int(11) NULL DEFAULT NULL COMMENT '天气代码',
  `weather_main` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气主要状况',
  `weather_description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气描述',
  `weather_icon` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气图标',
  `created_at` datetime NOT NULL COMMENT '数据创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKcvh84x8ef0y51183g7vbtu89t`(`latitude`, `longitude`, `dt`, `forecast_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for weather_historical
-- ----------------------------
DROP TABLE IF EXISTS `weather_historical`;
CREATE TABLE `weather_historical`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史天气ID',
  `latitude` decimal(10, 2) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 2) NOT NULL COMMENT '经度',
  `dt` bigint(20) NOT NULL COMMENT '数据时间戳',
  `temp` decimal(5, 2) NOT NULL COMMENT '温度',
  `feels_like` decimal(5, 2) NULL DEFAULT NULL COMMENT '体感温度',
  `temp_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '最低温度',
  `temp_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '最高温度',
  `pressure` int(11) NULL DEFAULT NULL COMMENT '大气压力(hPa)',
  `humidity` int(11) NULL DEFAULT NULL COMMENT '湿度(%)',
  `wind_speed` decimal(5, 2) NULL DEFAULT NULL COMMENT '风速(m/s)',
  `wind_deg` int(11) NULL DEFAULT NULL COMMENT '风向(度)',
  `clouds_all` int(11) NULL DEFAULT NULL COMMENT '云量(%)',
  `rain1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雨量(mm)',
  `rain3h` decimal(5, 2) NULL DEFAULT NULL COMMENT '3小时降雨量(mm)',
  `snow1h` decimal(5, 2) NULL DEFAULT NULL COMMENT '1小时降雪量(mm)',
  `snow3h` decimal(5, 2) NULL DEFAULT NULL COMMENT '3小时降雪量(mm)',
  `weather_id` int(11) NULL DEFAULT NULL COMMENT '天气代码',
  `weather_main` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气主要状况',
  `weather_description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气描述',
  `weather_icon` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '天气图标',
  `created_at` datetime NOT NULL COMMENT '数据创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKj4isibx59gf7muryu6rp9tj3v`(`latitude`, `longitude`, `dt`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
