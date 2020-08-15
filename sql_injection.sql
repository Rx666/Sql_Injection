/*
 Navicat Premium Data Transfer

 Source Server         : parttime
 Source Server Type    : MySQL
 Source Server Version : 50631
 Source Host           : localhost:3306
 Source Schema         : sql_injection

 Target Server Type    : MySQL
 Target Server Version : 50631
 File Encoding         : 65001

 Date: 12/08/2020 21:59:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_sub_url
-- ----------------------------
DROP TABLE IF EXISTS `t_sub_url`;
CREATE TABLE `t_sub_url`  (
  `url_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主连接ID',
  `crawl_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '爬取的连接',
  `is_injection` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否可注入',
  `blind_injection` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '是否存在盲注',
  `error_injection` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '是否存在错误注入',
  `time_injection` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '是否存在时间注入',
  `normal_injection` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '是否存在正常注入',
  `remark` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '备注',
  `bug_num` int(11) NULL DEFAULT 0 COMMENT '漏洞个数',
  INDEX `u_s_key`(`url_id`) USING BTREE,
  CONSTRAINT `u_s_key` FOREIGN KEY (`url_id`) REFERENCES `t_url` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for t_url
-- ----------------------------
DROP TABLE IF EXISTS `t_url`;
CREATE TABLE `t_url`  (
  `id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键ID',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '连接名称',
  `access_vector` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '攻击途径',
  `access_complexity` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '攻击复杂度',
  `authentication` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否需要认证',
  `confimpact` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '机密性',
  `integimpact` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '完整性',
  `availimpact` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '可用性',
  `database` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
