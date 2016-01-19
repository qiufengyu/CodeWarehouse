SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for huanqiu_china
-- ----------------------------
DROP TABLE IF EXISTS `huanqiu_china`;
CREATE TABLE `huanqiu_china` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(511) NOT NULL,
  `agent` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `url` varchar(255) NOT NULL,
  `content` mediumblob,
  `picture` blob DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `saveTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for huanqiu_world
-- ----------------------------
DROP TABLE IF EXISTS `huanqiu_world`;
CREATE TABLE `huanqiu_world` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(511) NOT NULL,
  `agent` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `url` varchar(255) NOT NULL,
  `content` mediumblob,
  `picture` blob DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `saveTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
