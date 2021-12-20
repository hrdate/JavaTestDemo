/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80025
 Source Host           : localhost:3306
 Source Schema         : exam1

 Target Server Type    : MySQL
 Target Server Version : 80025
 File Encoding         : 65001

 Date: 13/12/2021 15:47:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for game
-- ----------------------------
DROP TABLE IF EXISTS `game`;
CREATE TABLE `game`  (
  `gameTime` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `gameResult` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `opponent` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `userId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game
-- ----------------------------
INSERT INTO `game` VALUES ('2021-11-11 21:00', 'yes', 'cs', '111');
INSERT INTO `game` VALUES ('2021-11-12 00:00', 'no', 'cs', '222');
INSERT INTO `game` VALUES ('2021-11-17 17:18', 'yes', 'cs', '111');
INSERT INTO `game` VALUES ('2021-11-18 13:18', 'no', 'cs', '222');
INSERT INTO `game` VALUES ('2021-11-30 18:29', 'no', 'cs', '333');
INSERT INTO `game` VALUES ('2021-11-30 19:02', 'yes', 'ps', '333');
INSERT INTO `game` VALUES ('2021-11-30 19:02', 'no', 'ps', '111');
INSERT INTO `game` VALUES ('2021-12-12 12:07', 'no', 'cs', '111');
INSERT INTO `game` VALUES ('2021-12-12 12:20', 'no', 'cs', '333');
INSERT INTO `game` VALUES ('2021-12-12 12:21', 'tie', 'cs', '222');
INSERT INTO `game` VALUES ('2021-12-12 12:23', 'tie', 'cs', '111');
INSERT INTO `game` VALUES ('2021-12-12 12:24', 'tie', 'cs', '222');
INSERT INTO `game` VALUES ('2021-12-12 12:24', 'no', 'cs', '222');
INSERT INTO `game` VALUES ('2021-12-12 12:31', 'no', 'cs', '333');
INSERT INTO `game` VALUES ('2021-12-12 12:31', 'no', 'cs', '333');
INSERT INTO `game` VALUES ('2021-12-12 13:04', 'no', 'ps', '111');
INSERT INTO `game` VALUES ('2021-12-12 13:04', 'yes', 'ps', '222');
INSERT INTO `game` VALUES ('2021-12-13 15:44', 'tie', 'cs', '111');
INSERT INTO `game` VALUES ('2021-12-13 15:44', 'no', 'ps', '111');
INSERT INTO `game` VALUES ('2021-12-13 15:44', 'yes', 'ps', '222');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `userId` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `login` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `userName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('111', '111', 'no', '111');
INSERT INTO `user` VALUES ('222', '222', 'no', '222');
INSERT INTO `user` VALUES ('333', '333', 'no', '333');
INSERT INTO `user` VALUES ('444', '444', 'no', '444');
INSERT INTO `user` VALUES ('555', '555', 'no', '555');
INSERT INTO `user` VALUES ('666', '666', 'no', '666');

SET FOREIGN_KEY_CHECKS = 1;
