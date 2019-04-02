/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50553
Source Host           : localhost:3306
Source Database       : smg_db

Target Server Type    : MYSQL
Target Server Version : 50553
File Encoding         : 65001

Date: 2019-03-25 13:39:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `visitor_info`
-- ----------------------------
DROP TABLE IF EXISTS `visitor_info`;
CREATE TABLE `visitor_info` (
  `id` varchar(32) NOT NULL COMMENT 'ID',
  `visitor_name` varchar(255) NOT NULL COMMENT '访客姓名',
  `visitee_name` varchar(255) NOT NULL COMMENT '被访人',
  `photo_data` longtext NOT NULL COMMENT '照片base64数据',
  `company` varchar(255) DEFAULT NULL COMMENT '访客所属公司',
  `phone` varchar(255) DEFAULT NULL COMMENT '访客电话',
  `visit_purpose` varchar(1000) DEFAULT NULL COMMENT '来访目的  0: 其它，1: 商务洽谈, 2: 面试，3：亲友会见，4：外卖快递',
  `visit_start_time` datetime NOT NULL COMMENT '访问开始时间',
  `visit_end_time` datetime NOT NULL COMMENT '访问结束时间',
  `floor` text NOT NULL COMMENT '访问设备（楼层）的数据["一楼","二楼"]',
  `status` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"',
  `create_time` datetime NOT NULL COMMENT '记录创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `staff_id` varchar(255) DEFAULT NULL COMMENT '访客唯一id',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of visitor_info
-- ----------------------------
