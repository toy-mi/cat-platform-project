CREATE DATABASE  IF NOT EXISTS `cat_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cat_db`;
SET FOREIGN_KEY_CHECKS = 0;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: cat_db
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `adoption_application`
--

DROP TABLE IF EXISTS `adoption_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `cat_id` bigint NOT NULL COMMENT '猫咪ID',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '申请状态：0-待初审，1-初审通过，2-初审拒绝，3-待回访，4-回访通过，5-回访失败，6-待终审，7-终审通过，8-终审拒绝，9-已签订协议，10-已完成领养，11-已取消',
  `application_data` text COLLATE utf8mb4_unicode_ci COMMENT '申请表单数据（JSON格式，如家庭情况等）',
  `first_reviewer` bigint DEFAULT NULL COMMENT '初审人ID',
  `first_review_time` datetime DEFAULT NULL COMMENT '初审时间',
  `first_review_remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '初审意见',
  `follow_up_content` text COLLATE utf8mb4_unicode_ci COMMENT '回访记录',
  `follow_up_time` datetime DEFAULT NULL COMMENT '回访时间',
  `follow_up_by` bigint DEFAULT NULL COMMENT '回访人ID',
  `final_reviewer` bigint DEFAULT NULL COMMENT '终审人ID',
  `final_review_time` datetime DEFAULT NULL COMMENT '终审时间',
  `final_review_remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '终审意见',
  `agreement_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子协议文件URL',
  `complete_time` datetime DEFAULT NULL COMMENT '领养完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_cat_id` (`cat_id`),
  KEY `idx_status` (`status`),
  KEY `fk_adoption_first_reviewer` (`first_reviewer`),
  KEY `fk_adoption_follow_up_by` (`follow_up_by`),
  KEY `fk_adoption_final_reviewer` (`final_reviewer`),
  CONSTRAINT `fk_adoption_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_adoption_final_reviewer` FOREIGN KEY (`final_reviewer`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_adoption_first_reviewer` FOREIGN KEY (`first_reviewer`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_adoption_follow_up_by` FOREIGN KEY (`follow_up_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_adoption_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adoption_application`
--

LOCK TABLES `adoption_application` WRITE;
/*!40000 ALTER TABLE `adoption_application` DISABLE KEYS */;
INSERT INTO `adoption_application` VALUES (12,10,5,'2026-03-16 23:37:25',10,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"\",\"other\":\"\"}',1,'2026-03-16 23:37:46','','好','2026-03-16 23:46:58',1,1,'2026-03-16 23:38:16','好','/uploads/agreements/agreement_12_1773675508957.doc','2026-03-16 23:38:33'),(13,1,4,'2026-03-17 00:18:34',10,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"\",\"other\":\"\"}',1,'2026-03-18 02:45:21','','好','2026-03-18 02:48:06',1,1,'2026-03-19 21:26:53','','/uploads/agreements/agreement_13_1774289445449.doc','2026-03-24 02:11:28'),(16,12,3,'2026-03-23 15:52:05',0,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"\",\"other\":\"\"}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(17,1,8,'2026-03-23 21:22:18',1,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"\",\"other\":\"\"}',11,'2026-03-28 17:55:51','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(18,16,7,'2026-04-03 14:37:28',4,'{\"houseType\":\"自有住房\",\"familyMembers\":3,\"hasChildren\":true,\"petExperience\":\"我养过好多只猫，有丰富的养宠经验。\",\"other\":\"我会好好待猫\"}',1,'2026-04-03 15:17:49','','好','2026-04-09 15:34:30',1,NULL,NULL,NULL,NULL,NULL),(21,8,2,'2026-04-21 14:42:12',1,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"有养猫经验\",\"other\":\"\"}',11,'2026-04-21 15:01:05','初审通过，材料齐全',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(22,17,8,'2026-04-22 14:25:19',0,'{\"houseType\":\"自有住房\",\"familyMembers\":2,\"hasChildren\":false,\"petExperience\":\"\",\"other\":\"\"}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `adoption_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `adoption_follow_up`
--

DROP TABLE IF EXISTS `adoption_follow_up`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `adoption_follow_up` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL COMMENT '领养申请ID',
  `follow_up_time` datetime DEFAULT NULL COMMENT '回访时间',
  `follow_up_by` bigint DEFAULT NULL COMMENT '回访人ID',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '回访内容',
  `next_follow_up_date` date DEFAULT NULL COMMENT '下次回访日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `fk_follow_up_user` (`follow_up_by`),
  CONSTRAINT `fk_follow_up_application` FOREIGN KEY (`application_id`) REFERENCES `adoption_application` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_follow_up_user` FOREIGN KEY (`follow_up_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养回访记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `adoption_follow_up`
--

LOCK TABLES `adoption_follow_up` WRITE;
/*!40000 ALTER TABLE `adoption_follow_up` DISABLE KEYS */;
INSERT INTO `adoption_follow_up` VALUES (2,12,'2026-03-16 23:37:58',1,'好','2026-03-20','2026-03-16 23:37:58'),(3,12,'2026-03-16 23:38:09',1,'好','2026-03-31','2026-03-16 23:38:09'),(4,12,'2026-03-16 23:46:58',1,'好','2026-03-31','2026-03-16 23:46:58'),(5,13,'2026-03-18 02:48:06',1,'好','2026-03-26','2026-03-18 02:48:06'),(6,18,'2026-04-09 15:34:30',1,'好','2026-04-30','2026-04-09 15:34:30');
/*!40000 ALTER TABLE `adoption_follow_up` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `priority` tinyint DEFAULT '0' COMMENT '优先级',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-草稿，1-已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `fk_announcement_creator` (`create_by`),
  CONSTRAINT `fk_announcement_creator` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
INSERT INTO `announcement` VALUES (1,'猫咪小知识','猫咪的祖先是古兽猫，古兽猫生活在大约在5000万以前，是生活在树上的小动物。据说狗的祖先爷是古兽猫，猫狗同源，让人吃惊吧。\n猫属于脊索动物门、哺乳纲、食肉目、猫科动物，与虎、狮、豹属于同一科的动物，它们拥有锋利的犬齿和优良的消化系统。\n据说家猫的祖先是利比亚猫，利比亚猫生活在沙漠中，以捕猎小型动物为主。这种猫目前还生活在非洲及东南亚的一些地区，比较耐热。\n与狗不同，猫是喜欢单独行动的动物，独自生活是猫咪的特性。高冷、孤僻、高傲是很多猫咪的特点。家猫会随着生活节奏而改变性格。\n猫咪抓绕家具是天生的标记行为，通过对猫咪行为的了解和后期的饲养过程，绝大多数猫咪都有自己专属的抓绕玩具或区域，所以不要道听途说，因为那不是你的猫。纯种猫是通常指血统比较正宗的猫咪，是繁育人有计划地将两种不同猫咪交配，去各自地长处，然后经过几十代地繁殖，形成稳定独立的性格和外貌。这些品种的猫咪通常是在纯种猫登记在机构注册。\n可不可以选择素食喂养猫咪?这个问题是有争议的，因为猫咪是一种\"食肉动物”，如果素食能保证猫咪所需要的营养的话，猫咪也能健康成长，但是需要精心设计，满足猫咪做需要的牛磺酸、维生素A、蛋白质的摄取量，但是一般家庭喂养很难满足。\n猫的正常体温一般在38-39°C，只要体温不超过39°C，我们就可以观察，紧张会造成它体温升高，外出打疫苗、运输时尤为注意这一点。\n猫咪的心跳在一分钟160-180次。紧张或者发烧都会造成心跳加速猫咪的脉搏可以通过后腿内侧检测到，成年猫咪每分钟140-180次小猫的脉搏要快很多。\n平静状态下猫咪的呼吸频率为每分钟20-30次。呼吸会带动胸部和腹部的起伏，平静状态下如果发生呼吸急促很有可能标志着疼痛、休克、脱水或疾病。\n猫咪和人类一样，也是分不同的血型。血型为 A、B、AB 型三种其中A型最多，AB 型猫非常少。据说英国短毛猫、德文莱克斯猫康沃尔帝王猫中的B型血非常多\n猫咪每天大约需要280-300卡路里的食物，用以补充身体所需热量\n猫是双目视觉动物:毛的色觉远不如对声音气味和动色、灰色、苗它们只能分辨蓝色、灰色、绿色作的感知重要;猫对红色无法识别。色、黄色，绿色也就是说，人能看猫咪都是近视眼，猫看不清二十公分以内的物体清楚的距离范围，在猫咪眼中却是一片模糊。',0,1,'2026-03-21 10:27:04','2026-03-21 18:27:05',1),(2,'领养新家，从这“四步”开始','为了让领养流程更清晰，我们优化了申请页面。现在你只需：① 填写基本信息 ② 等待志愿者初审 ③ 配合家访 ④ 签订协议，即可带猫咪回家。每一步都有温馨提示，如果遇到问题，随时联系我们的客服喵~',0,1,'2026-03-21 04:44:48','2026-03-21 20:44:50',1),(3,'寻找有爱的你，加入“喵星人守护者”','你是否愿意为流浪猫出一份力？我们正在招募周末喂猫、协助领养回访、拍照宣传的志愿者。不需要专业技能，只需要一颗爱猫的心。报名截止到本月底，点击“我的领养”页面的“申请志愿者”即可成为志愿者。',0,1,'2026-03-21 04:44:53','2026-03-21 20:44:54',1),(4,'新来的小可爱们，已上线猫咪图鉴','最近有3只新救助的猫咪已完成体检和疫苗，正式开放领养啦！……点击“猫咪图鉴”查看它们的照片和性格，也许你的命中注定的喵就在这里！',0,1,'2026-03-20 12:44:58','2026-03-21 20:44:59',1),(5,'小长假来临，别忘了关好门窗哦','假期期间，铲屎官们外出时请务必检查家中门窗是否关好，防止猫咪走失。如果带猫咪出行，请使用航空箱并系好牵引绳。如发现走失，可第一时间在平台发布“寻猫启事”，我们会帮忙扩散。',0,1,'2026-03-21 04:45:03','2026-03-21 20:45:04',1),(8,'春暖花开，别忘了给猫咪驱虫哦','春季是寄生虫活跃期，我们联合爱心宠物医院推出“驱虫关爱月”活动。即日起至5月30日，凭平台领养证明可享受体内外驱虫8折优惠，一起守护猫咪健康！',0,1,'2026-03-23 19:00:58','2026-03-24 03:00:59',1);
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cat`
--

DROP TABLE IF EXISTS `cat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '猫咪唯一ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '猫咪姓名',
  `breed` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品种',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0-未知，1-公，2-母',
  `personality` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性格描述',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '特征描述',
  `health_status` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '健康状况摘要',
  `neuter_status` tinyint DEFAULT '0' COMMENT '绝育状态：0-未知，1-已绝育，2-未绝育',
  `adoption_status` tinyint NOT NULL DEFAULT '0' COMMENT '领养状态：0-在养，1-待领养，2-待审核，3-已领养，4-失踪，5-去世',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`id`),
  KEY `idx_adoption_status` (`adoption_status`),
  KEY `fk_cat_creator` (`creator_id`),
  CONSTRAINT `fk_cat_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cat`
--

LOCK TABLES `cat` WRITE;
/*!40000 ALTER TABLE `cat` DISABLE KEYS */;
INSERT INTO `cat` VALUES (2,'咪咪','狮子猫',1,'抽象，很活泼，非常好动','整体白色，头上有黑色花纹','健康',2,1,'2026-03-14 21:39:40','2026-05-24 03:13:57',1,'/uploads/cats/251c61e0-4705-4878-8257-f4b36033e507.jpg'),(3,'邦邦','灰色狸花猫',2,'很活泼亲人，比较调皮','眼睛有白色高光，整体呈灰色，身上有花纹','健康',2,0,'2026-03-14 21:42:10','2026-03-29 23:59:14',1,'/uploads/cats/4f7445d4-3705-4366-9b87-c0b28a422d4f.jpg'),(4,'怪盗','银渐层',2,'温顺可爱、超级亲人','整体银白，身上有灰色纹路','有点瘦',2,3,'2026-03-14 21:44:41','2026-03-14 21:44:41',1,'/uploads/cats/65b0e74c-654c-427f-91a6-d58f12d5666f.jpg'),(5,'小黑','玄猫',1,'很活泼，声音很大，超级黏人','身上有一点白毛','很健康',2,3,'2026-03-14 22:01:17','2026-03-16 23:36:27',11,'/uploads/cats/e852c0f1-2941-4c17-ab26-e9bcd9f3c56b.jpg'),(7,'六一','暹罗猫',1,'活泼可爱，有点凶','脸黑黑的，眼睛很蓝','健康',2,1,'2026-03-23 17:22:48','2026-03-23 17:22:48',11,'/uploads/cats/d93ecc9b-0b05-42c5-8b96-0d0d66c69314.jpg'),(8,'豹子','狸花猫',2,'沉稳懂事，超级能干','整体棕色，身上有褐色条纹','健康',2,1,'2026-03-23 17:32:48','2026-03-29 20:53:53',1,'/uploads/cats/df525906-c681-4ca4-822e-8b7f66072a49.jpg'),(10,'糕糕','布偶猫',2,'温顺、可爱','背上有褐色斑点','健康',2,1,'2026-05-21 18:48:11','2026-05-21 19:23:24',1,'/uploads/cats/2a0cc7bc-faa0-48f9-813b-6160a9de9b3d.jpg'),(11,'小狸花','狸花猫',2,'活泼','四脚有白袜，胸前有一块白色毛发','健康',1,0,'2026-05-21 19:10:37','2026-05-24 02:27:11',1,'/uploads/cats/b232e20a-dc94-49ff-bd8e-a3aabbd23c3a.jpg');
/*!40000 ALTER TABLE `cat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cat_location`
--

DROP TABLE IF EXISTS `cat_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cat_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cat_id` bigint NOT NULL COMMENT '猫咪ID',
  `location_desc` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地点描述',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度',
  `is_current` tinyint DEFAULT '0' COMMENT '是否为当前常出没点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cat_id` (`cat_id`),
  KEY `idx_cat_current` (`cat_id`,`is_current`),
  CONSTRAINT `fk_cat_location_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪位置追踪表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cat_location`
--

LOCK TABLES `cat_location` WRITE;
/*!40000 ALTER TABLE `cat_location` DISABLE KEYS */;
INSERT INTO `cat_location` VALUES (16,4,'八达岭长城',40.35618800,116.01680200,0,'2026-03-23 00:17:25'),(18,3,'鹰潭',28.27209200,117.03953200,0,'2026-03-23 00:21:15'),(21,10,'天安门',39.90918700,116.39746300,0,'2026-05-21 18:53:37');
/*!40000 ALTER TABLE `cat_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cat_photo`
--

DROP TABLE IF EXISTS `cat_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cat_photo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cat_id` bigint NOT NULL COMMENT '猫咪ID',
  `photo_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '照片URL',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cat_id` (`cat_id`),
  CONSTRAINT `fk_cat_photo_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪照片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cat_photo`
--

LOCK TABLES `cat_photo` WRITE;
/*!40000 ALTER TABLE `cat_photo` DISABLE KEYS */;
INSERT INTO `cat_photo` VALUES (47,2,'/uploads/cats/2f410ae1-21da-4ebf-ab2b-2f1cab07802c.jpg',1,'2026-03-23 16:37:32'),(48,2,'/uploads/cats/fbe38234-dd0a-4a3b-81bc-253c1ca6b886.jpg',1,'2026-03-23 16:37:32'),(49,2,'/uploads/cats/251c61e0-4705-4878-8257-f4b36033e507.jpg',2,'2026-03-23 16:37:37'),(50,2,'/uploads/cats/fe117bbf-b748-49ed-9d4d-812410ab7b46.jpg',2,'2026-03-23 16:37:37'),(51,2,'/uploads/cats/158083b4-af01-4444-a9f7-b7dab04cf2a8.jpg',2,'2026-03-23 16:37:37'),(52,5,'/uploads/cats/92e56c14-599e-4dca-bb03-f42adee999f8.jpg',3,'2026-03-23 16:38:07'),(53,5,'/uploads/cats/4654c10a-cbc1-49c9-99e8-3a9f777bb09f.jpg',4,'2026-03-23 16:38:15'),(54,5,'/uploads/cats/e27e70aa-1217-4dca-9e76-2fbcf509fb47.jpg',5,'2026-03-23 16:38:15'),(55,5,'/uploads/cats/11b5fab3-ebf3-44ef-92cf-49946fc9166b.jpg',5,'2026-03-23 16:38:15'),(56,5,'/uploads/cats/6304c0a8-0922-4c17-bb5a-35e9c2b78165.jpg',5,'2026-03-23 16:38:15'),(57,5,'/uploads/cats/4e246833-eba0-4b44-ad53-679f9ce55f1c.jpg',5,'2026-03-23 16:38:15'),(58,5,'/uploads/cats/4368b47c-dc9c-4814-be1f-be49e15af316.jpg',5,'2026-03-23 16:38:15'),(59,5,'/uploads/cats/56c26824-b72d-4255-a5c9-9872e3e82af7.jpg',6,'2026-03-23 16:38:21'),(60,3,'/uploads/cats/5342efb7-f646-439b-8545-89cd0e2a2922.jpg',1,'2026-03-23 16:39:07'),(61,3,'/uploads/cats/de2e24c1-70e9-4e3d-ad76-2aba61027382.jpg',1,'2026-03-23 16:39:07'),(62,3,'/uploads/cats/d6e359b2-39bd-43ff-9e37-e36c4bbf4000.jpg',2,'2026-03-23 16:39:13'),(63,3,'/uploads/cats/fe68def0-50c3-44d0-8795-5dbdb6cb8863.jpg',3,'2026-03-23 16:39:13'),(64,3,'/uploads/cats/64d80f12-c82d-4c0f-865a-399fa1bf242e.jpg',3,'2026-03-23 16:39:13'),(65,3,'/uploads/cats/7ba9ecba-3dcd-44e2-9151-d45fce909195.jpg',3,'2026-03-23 16:39:13'),(66,3,'/uploads/cats/a0c69cd7-852a-460d-b32b-727b7b35d65e.jpg',3,'2026-03-23 16:39:13'),(67,3,'/uploads/cats/86634cf1-7623-4223-af11-cce36d638f0d.jpg',3,'2026-03-23 16:39:13'),(68,3,'/uploads/cats/f9be9eb8-b8f0-4a4c-b958-786ab7b2ce34.jpg',4,'2026-03-23 16:39:30'),(69,3,'/uploads/cats/112c9975-2b9a-4413-b525-5410a944e632.jpg',4,'2026-03-23 16:39:30'),(70,3,'/uploads/cats/1668aa00-8b86-4e42-9be5-4edb4049257a.jpg',4,'2026-03-23 16:39:30'),(71,3,'/uploads/cats/4f7445d4-3705-4366-9b87-c0b28a422d4f.jpg',4,'2026-03-23 16:39:30'),(72,3,'/uploads/cats/de77e291-c7f8-4148-bcd6-82acb390b07b.jpg',4,'2026-03-23 16:39:30'),(73,4,'/uploads/cats/a907504a-9684-4b27-8d70-a9a8b26f80a4.jpg',1,'2026-03-23 16:40:36'),(74,4,'/uploads/cats/65b0e74c-654c-427f-91a6-d58f12d5666f.jpg',2,'2026-03-23 17:07:47'),(75,4,'/uploads/cats/8ae219ce-2deb-4fd7-bf09-20ba9d7f1eb4.jpg',2,'2026-03-23 17:07:47'),(76,4,'/uploads/cats/1e3b9cfc-36f7-4b10-a804-d455c75f3634.jpg',2,'2026-03-23 17:07:47'),(77,4,'/uploads/cats/4f7c75b7-287b-4ba7-b5c3-6edb42064acc.jpg',3,'2026-03-23 17:07:56'),(79,4,'/uploads/cats/e828ba7d-359b-415e-9525-03af08d0d64b.jpg',4,'2026-03-23 17:07:56'),(80,4,'/uploads/cats/39245b84-6c75-45b4-967b-bbba84b98898.jpg',5,'2026-03-23 17:08:02'),(82,4,'/uploads/cats/5d4c5a71-d939-411e-bfb3-7ae33fa5b76f.jpg',6,'2026-03-23 17:08:24'),(83,4,'/uploads/cats/d898e9c3-7c67-4edc-a732-f6d1e64cf4f2.jpg',6,'2026-03-23 17:08:24'),(84,4,'/uploads/cats/491798ef-e0b6-4317-8c7a-480dc4450bb2.jpg',6,'2026-03-23 17:08:24'),(86,4,'/uploads/cats/39c1602d-b06e-41a4-a4bb-c1432ae12ecf.jpg',6,'2026-03-23 17:08:24'),(87,4,'/uploads/cats/b868bedb-3f17-413e-9419-90c2e1dde944.jpg',7,'2026-03-23 17:09:19'),(88,4,'/uploads/cats/fb227360-0538-47bd-88a7-c2d557703d84.jpg',7,'2026-03-23 17:09:19'),(89,5,'/uploads/cats/e852c0f1-2941-4c17-ab26-e9bcd9f3c56b.jpg',7,'2026-03-23 17:10:50'),(90,7,'/uploads/cats/bff5a207-3586-4f5b-94d4-ab2180030136.jpg',0,'2026-03-23 17:23:06'),(91,7,'/uploads/cats/d6e0f85c-fb72-4050-aa19-f3546e779baa.jpg',1,'2026-03-23 17:29:56'),(92,7,'/uploads/cats/4f6ec796-61b8-4a2a-abe0-2aeb18f14be3.jpg',1,'2026-03-23 17:29:56'),(93,7,'/uploads/cats/d93ecc9b-0b05-42c5-8b96-0d0d66c69314.jpg',1,'2026-03-23 17:29:56'),(94,8,'/uploads/cats/6ebcc719-5cc0-44f1-9791-f50406863e18.jpg',0,'2026-03-23 17:32:58'),(95,8,'/uploads/cats/fcf53771-c00f-4ac4-a307-09477d3e0453.jpg',0,'2026-03-23 17:32:58'),(99,8,'/uploads/cats/1fad6570-a22f-40f4-8abf-093fb96289c0.jpg',1,'2026-03-23 17:36:17'),(100,8,'/uploads/cats/69c581da-46cd-49da-a014-2e6b37d62945.jpg',1,'2026-03-23 17:36:17'),(101,8,'/uploads/cats/df525906-c681-4ca4-822e-8b7f66072a49.jpg',1,'2026-03-23 17:36:17'),(102,10,'/uploads/cats/e9547794-d0bc-42cc-8a5b-0f55eb58af5b.jpg',0,'2026-05-21 18:48:26'),(103,10,'/uploads/cats/3761d582-8fbe-4f71-9816-9a3db6e6d954.jpg',1,'2026-05-21 18:48:36'),(104,10,'/uploads/cats/2aa28936-aeda-4fa1-9232-0b5c73824178.jpg',1,'2026-05-21 18:48:36'),(105,10,'/uploads/cats/1a7a0e11-f52b-4dbf-b8d4-08a9893b8048.jpg',2,'2026-05-21 18:48:43'),(106,10,'/uploads/cats/48d02519-50cf-4bb1-9b64-fc45a77fa837.jpg',2,'2026-05-21 18:48:43'),(107,10,'/uploads/cats/c8d906eb-8a3a-43dc-abc0-ecd46d95a5bf.jpg',2,'2026-05-21 18:48:43'),(108,10,'/uploads/cats/2a0cc7bc-faa0-48f9-813b-6160a9de9b3d.jpg',2,'2026-05-21 18:48:43'),(109,10,'/uploads/cats/ec066009-b088-4998-8e7c-5577f3ec8f9b.jpg',3,'2026-05-21 18:50:00'),(110,10,'/uploads/cats/76a02074-d5a9-41ea-b3e4-e0e91021c223.jpg',3,'2026-05-21 18:50:00'),(111,10,'/uploads/cats/7b5465d1-89b8-4abc-9709-816b23993e7f.jpg',3,'2026-05-21 18:50:00'),(112,10,'/uploads/cats/c289c64e-9d7c-44b9-9888-2b5305f0a520.jpg',3,'2026-05-21 18:50:00'),(113,11,'/uploads/cats/4f19e043-d03d-410e-a696-7f42d1cb5a8c.jpg',0,'2026-05-21 19:11:54'),(114,11,'/uploads/cats/a081cb02-b4ca-447b-9908-dd72ebef28c4.jpg',0,'2026-05-21 19:11:54'),(115,11,'/uploads/cats/12346197-cbf2-4ddb-81ac-da3147127e70.jpg',0,'2026-05-21 19:11:54'),(116,11,'/uploads/cats/5431c032-b40c-4a9f-a028-58958f48c51b.jpg',1,'2026-05-21 19:11:54'),(117,11,'/uploads/cats/5ae79ac2-f85d-4df7-a84f-f9dce001f189.jpg',2,'2026-05-21 19:12:08'),(118,11,'/uploads/cats/b232e20a-dc94-49ff-bd8e-a3aabbd23c3a.jpg',2,'2026-05-21 19:12:08'),(119,11,'/uploads/cats/3dde604b-6297-4369-b3a7-e1456be9bb8d.jpg',2,'2026-05-21 19:12:08'),(120,11,'/uploads/cats/cb57d160-ff33-4c53-85ee-55e8bb43fb32.jpg',3,'2026-05-21 19:12:08'),(121,11,'/uploads/cats/70445ec1-73f2-4b79-8627-59f00b324d24.jpg',3,'2026-05-21 19:12:08'),(122,11,'/uploads/cats/a5797ccc-32bc-42d6-b050-665684823136.jpg',4,'2026-05-21 19:12:08'),(123,11,'/uploads/cats/e392d502-396f-4dee-8ab7-d8fa646a073e.jpg',5,'2026-05-21 19:12:08'),(124,11,'/uploads/cats/7c2cc0ef-d1fa-40c2-9a02-ae47cbc321e6.jpg',5,'2026-05-21 19:12:08'),(125,11,'/uploads/cats/1f2f8b42-07fe-4d7e-a832-74da3da02e9c.jpg',5,'2026-05-21 19:12:08'),(126,10,'/uploads/cats/9b53ed7e-8453-416b-bf73-42b3710003e9.jpg',4,'2026-05-21 19:22:49'),(127,10,'/uploads/cats/92d0dafd-84a4-43b8-b7ee-3780cd1c4524.jpg',4,'2026-05-21 19:22:49'),(128,10,'/uploads/cats/3baa2ed9-a18f-4653-9cbd-6b46bf5c8bc0.jpg',5,'2026-05-21 19:22:55');
/*!40000 ALTER TABLE `cat_photo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (10,19,1,'标准 7 天换粮法 第 1-2 天：新粮 25% + 旧粮 75% 第 3-4 天：新粮 50% + 旧粮 50% 第 5-6 天：新粮 75% + 旧粮 25% 第 7 天：完全换成新粮 猫咪肠胃敏感的话，可以延长到10-14 天。','2026-03-29 18:00:15'),(17,22,1,'加油！','2026-06-15 19:26:47');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donation`
--

DROP TABLE IF EXISTS `donation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '捐赠人ID',
  `campaign_id` bigint DEFAULT NULL COMMENT '关联募捐活动ID（可选）',
  `donation_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '捐赠类型：MONEY-资金，GOODS-物资',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '捐赠金额（如果是资金）',
  `goods_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物资名称',
  `goods_quantity` int DEFAULT NULL COMMENT '物资数量',
  `goods_unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物资单位',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审核，1-已审核，2-已拒绝',
  `donation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `attachment_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '凭证文件URL',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `fk_donation_auditor` (`audit_by`),
  CONSTRAINT `fk_donation_auditor` FOREIGN KEY (`audit_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_donation_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `donation_campaign` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_donation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donation`
--

LOCK TABLES `donation` WRITE;
/*!40000 ALTER TABLE `donation` DISABLE KEYS */;
INSERT INTO `donation` VALUES (1,10,1,'MONEY',10.00,NULL,NULL,'',1,'2026-03-19 16:33:47','2026-03-19 16:34:56',1,'',NULL),(2,1,1,'MONEY',20.00,NULL,NULL,'',1,'2026-03-19 16:34:42','2026-03-19 16:34:54',1,'',NULL),(3,1,2,'MONEY',20.00,NULL,NULL,'',1,'2026-03-19 17:18:57','2026-03-19 17:19:34',1,'',NULL),(4,1,1,'MONEY',20.00,NULL,NULL,'',1,'2026-03-21 10:10:03','2026-03-21 10:10:07',1,'',NULL),(5,1,1,'MONEY',50.00,NULL,NULL,'',1,'2026-03-21 10:19:30','2026-03-23 15:57:51',1,'',NULL),(6,1,3,'MONEY',100.00,NULL,NULL,'',1,'2026-03-21 10:30:30','2026-03-21 10:30:33',1,'',NULL),(7,1,3,'MONEY',10.00,NULL,NULL,'',2,'2026-03-21 10:54:20','2026-03-21 11:35:32',1,'','/uploads/donations/donation_1774061656229.png'),(8,1,3,'MONEY',100.00,NULL,NULL,'',1,'2026-03-21 11:34:59','2026-03-21 11:35:02',1,'',''),(9,1,1,'MONEY',10.00,NULL,NULL,'',1,'2026-03-21 17:35:16','2026-03-21 17:35:20',1,'',''),(10,1,1,'GOODS',NULL,'猫粮',2,'袋',1,'2026-03-21 17:36:58','2026-03-21 17:37:01',1,'',''),(11,1,1,'GOODS',NULL,'猫粮',5,'袋',1,'2026-03-21 17:57:08','2026-03-21 17:57:10',1,'',''),(12,1,1,'GOODS',NULL,'猫砂',1,'袋',1,'2026-03-21 17:59:58','2026-03-21 18:00:02',1,'',''),(13,1,1,'MONEY',10.00,NULL,NULL,'',1,'2026-03-23 15:48:55','2026-03-23 15:57:47',1,'','/uploads/donations/donation_1774252130932.jpg'),(14,10,1,'MONEY',20.00,NULL,NULL,'',1,'2026-03-23 15:49:53','2026-03-23 15:57:49',1,'',''),(15,1,4,'MONEY',50.00,NULL,NULL,'',1,'2026-03-23 15:57:34','2026-03-23 15:57:36',1,'',''),(16,16,4,'MONEY',20.00,NULL,NULL,'',1,'2026-04-03 15:09:34','2026-04-03 15:18:28',1,'',''),(17,1,4,'MONEY',20.00,NULL,NULL,'',1,'2026-04-09 15:55:19','2026-04-09 15:55:28',1,'',''),(18,11,4,'MONEY',10.00,NULL,NULL,'',2,'2026-04-09 17:39:02','2026-05-20 19:56:51',1,'',''),(19,17,4,'MONEY',20.00,NULL,NULL,'',0,'2026-04-22 15:22:22',NULL,NULL,'',''),(20,17,4,'MONEY',10.00,NULL,NULL,'',1,'2026-04-22 15:22:48','2026-05-20 19:58:45',1,'',''),(21,17,4,'MONEY',0.01,NULL,NULL,'',0,'2026-04-22 15:24:52',NULL,NULL,'',''),(22,1,4,'GOODS',NULL,'猫粮',2,'袋',1,'2026-05-20 19:54:55','2026-05-20 19:55:02',1,'','');
/*!40000 ALTER TABLE `donation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donation_campaign`
--

DROP TABLE IF EXISTS `donation_campaign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `donation_campaign` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '活动描述',
  `target_amount` decimal(10,2) NOT NULL COMMENT '目标金额',
  `current_amount` decimal(10,2) DEFAULT '0.00' COMMENT '当前筹集金额',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `cat_id` bigint DEFAULT NULL COMMENT '关联猫咪ID：NULL-全体猫咪，其他-某只猫咪',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-筹备中，1-进行中，2-已结束，3-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_cat_id` (`cat_id`),
  KEY `idx_status` (`status`),
  KEY `fk_donation_campaign_creator` (`create_by`),
  CONSTRAINT `fk_donation_campaign_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_donation_campaign_creator` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='募捐活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donation_campaign`
--

LOCK TABLES `donation_campaign` WRITE;
/*!40000 ALTER TABLE `donation_campaign` DISABLE KEYS */;
INSERT INTO `donation_campaign` VALUES (1,'猫咪生活资金筹款','感谢您为毛孩子贡献的一份力，我们不胜感激~',1000.00,140.00,'2026-03-19 08:26:29','2026-04-29 16:00:00',NULL,2,'2026-03-19 16:26:53',1),(2,'绝育资金','为小黑绝育募集资金',20.00,20.00,'2026-03-11 09:18:13','2026-03-11 09:18:16',5,4,'2026-03-19 17:18:20',1),(3,'体检资金','定期体检',200.00,200.00,'2026-03-19 10:30:06','2026-04-27 00:00:00',NULL,4,'2026-03-21 10:30:14',1),(4,'小猫之家活动','为小猫们创造一个遮风避雨的小家?',500.00,100.00,'2026-03-21 16:57:07','2026-06-24 16:00:00',NULL,1,'2026-03-23 15:57:15',1);
/*!40000 ALTER TABLE `donation_campaign` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_record`
--

DROP TABLE IF EXISTS `health_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cat_id` bigint NOT NULL COMMENT '猫咪ID',
  `record_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '记录类型：VACCINE-疫苗，NEUTER-绝育，CHECKUP-体检，OTHER-其他',
  `record_date` date DEFAULT NULL COMMENT '记录日期',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `vet_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兽医/机构',
  `attachment_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件（如体检报告图片）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_cat_id` (`cat_id`),
  KEY `fk_health_record_creator` (`create_by`),
  CONSTRAINT `fk_health_record_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_health_record_creator` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康档案表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_record`
--

LOCK TABLES `health_record` WRITE;
/*!40000 ALTER TABLE `health_record` DISABLE KEYS */;
INSERT INTO `health_record` VALUES (3,5,'VACCINE','2026-03-25','','',NULL,'2026-03-22 23:08:33',1),(4,4,'NEUTER','2026-03-16','','',NULL,'2026-03-23 00:23:50',1),(5,3,'CHECKUP','2026-03-04','','',NULL,'2026-03-23 00:26:39',1),(6,7,'VACCINE','2026-04-23','','',NULL,'2026-04-09 15:27:52',1),(7,8,'CHECKUP','2026-04-15','','',NULL,'2026-04-09 15:28:09',17),(8,2,'VACCINE','2026-07-22','很大','',NULL,'2026-07-24 16:35:21',1);
/*!40000 ALTER TABLE `health_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型（如：ADOPTION_STATUS_CHANGE, POST_COMMENT, POST_LIKE, TASK_ASSIGN）',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知内容',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID（如申请ID、动态ID等）',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,1,'POST_LIKE','您的动态收到点赞','testuser1 点赞了您的动态',13,1,'2026-03-18 02:42:01'),(2,10,'POST_LIKE','您的动态收到点赞','管理员 点赞了您的动态',12,1,'2026-03-18 02:43:36'),(3,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审通过',13,1,'2026-03-18 02:45:21'),(4,10,'POST_LIKE','您的动态收到点赞','管理员 点赞了您的动态',11,1,'2026-03-18 02:49:39'),(5,10,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：好',11,1,'2026-03-18 02:49:48'),(6,1,'TASK_APPLY','志愿者报名任务','志愿者 testvolunteer 报名了任务：1',7,1,'2026-03-18 03:04:13'),(7,11,'TASK_ASSIGN','您被指派了新任务','任务：1 已指派给您，请及时处理',7,1,'2026-03-18 03:06:35'),(8,1,'TASK_APPLY','志愿者报名任务','志愿者 testvolunteer 报名了任务：5',6,1,'2026-03-18 03:09:53'),(9,11,'TASK_APPLICATION_SELECTED','报名被选中','您在任务【5】的报名已被管理员选中',6,1,'2026-03-18 03:10:09'),(10,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',2,1,'2026-03-19 16:34:54'),(11,10,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',1,1,'2026-03-19 16:34:56'),(12,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',3,1,'2026-03-19 17:19:34'),(13,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：终审通过',13,1,'2026-03-19 21:26:53'),(14,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',4,1,'2026-03-21 10:10:07'),(15,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',6,1,'2026-03-21 10:30:33'),(16,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核通过，备注：',8,1,'2026-03-21 11:35:03'),(17,1,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核拒绝，备注：',7,1,'2026-03-21 11:35:32'),(18,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥10.00',9,1,'2026-03-21 17:35:20'),(19,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：物资：猫粮 × 2 袋',10,1,'2026-03-21 17:37:01'),(20,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：物资：猫粮 × 5 袋',11,1,'2026-03-21 17:57:10'),(21,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：物资：猫砂 × 1 袋',12,1,'2026-03-21 18:00:02'),(22,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审通过',14,1,'2026-03-22 16:51:14'),(23,1,'POST_COMMENT','您的动态收到新评论','testuser1 评论了您的动态：22',13,1,'2026-03-23 01:01:21'),(24,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥50.00',15,1,'2026-03-23 15:57:36'),(25,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥10.00',13,1,'2026-03-23 15:57:47'),(26,10,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥20.00',14,0,'2026-03-23 15:57:49'),(27,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥50.00',5,1,'2026-03-23 15:57:51'),(28,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：领养完成',13,1,'2026-03-24 02:11:28'),(29,10,'POST_LIKE','您的动态收到点赞','邦邦好卡哇 点赞了您的动态',20,0,'2026-03-24 03:14:06'),(30,1,'POST_LIKE','您的动态收到点赞','邦邦好卡哇 点赞了您的动态',18,0,'2026-03-24 03:14:07'),(31,1,'POST_LIKE','您的动态收到点赞','邦邦好卡哇 点赞了您的动态',13,0,'2026-03-24 03:14:09'),(32,11,'POST_LIKE','您的动态收到点赞','邦邦好卡哇 点赞了您的动态',21,0,'2026-03-24 03:14:16'),(33,11,'TASK_NEW','新任务发布','新任务：2 已发布，请及时报名',9,0,'2026-03-27 17:31:46'),(34,12,'TASK_NEW','新任务发布','新任务：2 已发布，请及时报名',9,0,'2026-03-27 17:31:46'),(35,11,'TASK_ASSIGN','您被指派了新任务','任务：2 已指派给您，请及时处理',9,0,'2026-03-27 17:32:01'),(36,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审通过',17,1,'2026-03-28 17:55:51'),(37,10,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：标准 7 天换粮法 第 1-2 天：新粮 25% + 旧粮 75% 第 3-4 天：新粮 50% + 旧粮 50% 第 5-6 天：新粮 75% + 旧粮 25% 第 7 天：完全换成新粮 猫咪肠胃敏感的话，可以延长到10-14 天。',19,0,'2026-03-29 18:00:15'),(38,10,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：1',19,0,'2026-03-29 18:01:21'),(39,10,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：5',19,0,'2026-03-29 18:41:49'),(40,10,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：3',19,0,'2026-03-29 18:41:57'),(41,10,'POST_LIKE','您的动态收到点赞','管理员 点赞了您的动态',19,0,'2026-03-29 20:51:33'),(42,10,'POST_LIKE','您的动态收到点赞','管理员 点赞了您的动态',19,0,'2026-03-29 20:51:41'),(43,1,'TASK_APPLY','志愿者报名任务','志愿者 lili 报名了任务：2',3,1,'2026-04-03 14:14:42'),(44,16,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审通过',18,0,'2026-04-03 15:17:49'),(45,16,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥20.00',16,0,'2026-04-03 15:18:28'),(46,16,'TASK_APPLICATION_SELECTED','报名被选中','您在任务【2】的报名已被管理员选中',3,1,'2026-04-05 15:15:36'),(47,11,'TASK_APPLICATION_SELECTED','报名被选中','您在任务【2】的报名已被管理员选中',3,1,'2026-04-05 16:49:05'),(48,11,'TASK_NEW','新任务发布','新任务：3 已发布，请及时报名',10,0,'2026-04-05 16:50:50'),(49,12,'TASK_NEW','新任务发布','新任务：3 已发布，请及时报名',10,0,'2026-04-05 16:50:50'),(50,16,'TASK_NEW','新任务发布','新任务：3 已发布，请及时报名',10,0,'2026-04-05 16:50:50'),(51,17,'TASK_NEW','新任务发布','新任务：3 已发布，请及时报名',10,0,'2026-04-05 16:50:50'),(52,11,'TASK_ASSIGN','您被指派了新任务','任务：3 已指派给您，请及时处理',10,0,'2026-04-05 16:50:54'),(53,11,'TASK_COMPLETION_REVIEW','任务完成审核通过','您提交的任务【33】已完成审核，任务已结束。',8,0,'2026-04-05 17:49:11'),(54,11,'TASK_COMPLETION_REVIEW','任务完成审核未通过','您提交的任务【1】审核未通过，原因：',7,0,'2026-04-05 17:58:37'),(55,11,'TASK_COMPLETION_REVIEW','任务完成审核通过','您提交的任务【1】已完成审核，任务已结束。',7,0,'2026-04-05 18:29:58'),(56,11,'TASK_NEW','新任务发布','新任务：5 已发布，请及时报名',11,0,'2026-04-05 18:50:05'),(57,12,'TASK_NEW','新任务发布','新任务：5 已发布，请及时报名',11,0,'2026-04-05 18:50:05'),(58,16,'TASK_NEW','新任务发布','新任务：5 已发布，请及时报名',11,0,'2026-04-05 18:50:05'),(59,17,'TASK_NEW','新任务发布','新任务：5 已发布，请及时报名',11,0,'2026-04-05 18:50:05'),(60,11,'TASK_ASSIGN','您被指派了新任务','任务：5 已指派给您，请及时处理',11,0,'2026-04-05 18:50:09'),(61,11,'TASK_COMPLETION_REVIEW','任务完成审核未通过','您提交的任务【3】审核未通过，原因：',10,0,'2026-04-05 18:55:23'),(62,11,'TASK_COMPLETION_REVIEW','任务完成审核未通过','您提交的任务【3】审核未通过，原因：',10,0,'2026-04-05 19:01:02'),(63,11,'TASK_NEW','新任务发布','新任务：9 已发布，请及时报名',12,0,'2026-04-05 19:03:52'),(64,12,'TASK_NEW','新任务发布','新任务：9 已发布，请及时报名',12,0,'2026-04-05 19:03:52'),(65,16,'TASK_NEW','新任务发布','新任务：9 已发布，请及时报名',12,0,'2026-04-05 19:03:52'),(66,17,'TASK_NEW','新任务发布','新任务：9 已发布，请及时报名',12,0,'2026-04-05 19:03:52'),(67,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥20.00',17,0,'2026-04-09 15:55:28'),(68,11,'TASK_NEW','新任务发布','新任务：56 已发布，请及时报名',13,0,'2026-04-19 15:27:51'),(69,12,'TASK_NEW','新任务发布','新任务：56 已发布，请及时报名',13,0,'2026-04-19 15:27:51'),(70,16,'TASK_NEW','新任务发布','新任务：56 已发布，请及时报名',13,0,'2026-04-19 15:27:51'),(71,17,'TASK_NEW','新任务发布','新任务：56 已发布，请及时报名',13,0,'2026-04-19 15:27:51'),(72,11,'TASK_NEW','新任务发布','新任务：66 已发布，请及时报名',14,0,'2026-04-19 15:28:01'),(73,12,'TASK_NEW','新任务发布','新任务：66 已发布，请及时报名',14,0,'2026-04-19 15:28:01'),(74,16,'TASK_NEW','新任务发布','新任务：66 已发布，请及时报名',14,0,'2026-04-19 15:28:01'),(75,17,'TASK_NEW','新任务发布','新任务：66 已发布，请及时报名',14,0,'2026-04-19 15:28:01'),(78,8,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审通过',21,0,'2026-04-21 15:01:05'),(79,1,'TASK_APPLY','志愿者报名任务','志愿者 ooo 报名了任务：66',14,1,'2026-04-22 15:15:38'),(80,11,'TASK_COMPLETION_REVIEW','任务完成审核通过','您提交的任务【3】已完成审核，任务已结束。',10,0,'2026-05-20 00:42:15'),(81,1,'ADOPTION_STATUS_CHANGE','领养申请状态更新','您的领养申请状态已变更为：初审拒绝',23,1,'2026-05-20 19:22:32'),(82,1,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：物资：猫粮 × 2 袋',22,1,'2026-05-20 19:55:02'),(83,11,'DONATION_AUDIT','捐赠审核结果','您的捐赠记录已审核拒绝，备注：',18,0,'2026-05-20 19:56:51'),(84,17,'DONATION_AUDIT','感谢您的捐赠','您的捐赠已审核通过，感谢您对猫咪的关爱！捐赠详情：金额￥10.00',20,0,'2026-05-20 19:58:45'),(85,10,'POST_LIKE','您的动态收到点赞','管理员 点赞了您的动态',19,0,'2026-05-21 19:17:58'),(86,12,'POST_COMMENT','您的动态收到新评论','管理员 评论了您的动态：加油！',22,0,'2026-06-15 19:26:47');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题（可选）',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `images` text COLLATE utf8mb4_unicode_ci COMMENT '图片URL列表，可JSON或每行一个URL（改用TEXT类型）',
  `location_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置描述',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区动态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (18,1,'领养流程优化，让爱心更高效','为了让大家更清晰地了解领养进度，我们优化了“我的领养”页面，现在可以随时查看申请状态（初审、回访、终审、签订协议等）。同时，志愿者回访记录也会同步显示，方便沟通。感谢大家的支持，一起帮助更多猫咪找到温暖的家！','/uploads/community/community_1774292875074.jpg','',NULL,NULL,1,0,'2026-03-24 03:07:59','2026-03-24 03:07:59'),(19,10,'猫咪换粮要注意什么？求经验','之前一直喂皇家，想给主子换天然粮，但听说换粮要循序渐进，否则容易软便。有没有成功换粮的家长分享一下经验？我家猫肠胃一般，很怕踩雷?','','',NULL,NULL,1,1,'2026-03-24 03:09:34','2026-03-24 03:09:34'),(20,10,'主子今天又翻垃圾桶了…','第108次从垃圾桶里把猫抓出来，明明刚吃完罐头！求推荐好用的防翻垃圾桶（或防猫妙招），在线等挺急的?','','',NULL,NULL,1,0,'2026-03-24 03:10:06','2026-03-24 03:10:06'),(21,11,'今天在校园里遇到一只小橘猫','在教学楼后面发现一只小橘，瘦瘦的，有点怕人。蹲了半小时才拍到它吃东西的样子。已经联系了平台志愿者，希望能尽快帮它体检，找个家。有想一起投喂的小伙伴吗？','/uploads/community/community_1774293059657.jpg','',NULL,NULL,1,0,'2026-03-24 03:11:03','2026-03-24 03:11:03'),(22,12,'周末回访小分队出发！','今天和几位志愿者一起去回访上个月领养的小黑。看到它在新家吃得胖乎乎，还学会了用猫爬架，太治愈了！领养人特别用心，还给猫做了个小窝。每只被善待的猫咪，都是我们坚持的动力?','/uploads/community/community_1774293223632.jpg','',NULL,NULL,1,1,'2026-03-24 03:13:47','2026-03-24 03:13:47'),(26,1,'猫咪小课堂：新手养猫需要注意什么？?','新手养猫其实没那么难，抓住吃、住、健康、陪伴这几块就够了，我给你整理了一份超实用、不踩坑的新手攻略，照着做就行～\n一、接猫前准备（必备清单）\n1. 基础用品\n猫砂盆：开放式 / 半封闭，幼猫选矮边方便进出\n猫砂：新手推荐豆腐砂，结团好、可冲厕所\n猫粮：幼猫吃幼猫粮 / 奶糕粮，成猫吃全价成猫粮，选正规品牌\n食盆水盆：陶瓷 / 不锈钢，避免塑料黑下巴\n猫窝 / 垫子：柔软保暖，放在安静角落\n猫抓板：必备！保护沙发，磨爪子是天性\n2. 可选提升用品\n猫梳子、指甲剪、洗耳液\n猫包（外出就医 / 洗澡用）\n逗猫棒、玩具\n二、饮食喂养（最关键）\n1.幼猫（＜12 个月）：\n少食多餐，一天 3–4 顿\n2 个月以下可喂羊奶粉泡软猫粮\n不要喂牛奶、洋葱、葡萄、巧克力、骨头等\n2.成猫：\n一天 2 顿，定时定量\n保证干净饮用水随时有\n零食少喂，避免挑食\n三、猫砂盆与如厕\n1.猫砂厚度5cm 以上\n2.每天铲屎，每周彻底清洗砂盆\n3.多猫家庭：猫砂盆数量 = 猫数 + 1\n4.刚到家乱尿：先排查应激、发情、泌尿问题\n四、健康与疫苗驱虫\n1.疫苗：\n猫三联：8 周、12 周、16 周各一针，之后每年 / 每三年加强\n狂犬疫苗：满 3 个月可打，每年一次\n2.驱虫：\n体内外同驱：幼猫每月一次，成猫1–3 个月一次\n！！！出现这些情况及时就医：\n不吃不喝、呕吐腹泻、精神差、尿闭（公猫尤其危险）\n五、刚接回家注意事项（防应激）\n先关在小房间适应，不要强行抱、追着玩\n环境安静，别频繁换地方、别洗澡\n适应 1–2 周后再洗澡、驱虫、打疫苗\n不要一上来就和原住民猫直接接触，要隔离过渡\n六、日常护理\n剪指甲：2–3 周一次，别剪到血线\n梳毛：长毛猫天天梳，短毛猫每周 1–2 次\n刷牙：最好从小培养，预防口炎\n洗澡：不用频繁洗，2–3 个月一次即可\n七、安全禁忌\n封窗！封窗！封窗！（高层坠楼非常常见）\n家里不要放百合、绿萝等有毒植物\n电线、塑料袋、针线收好，防止误食\n八、心态准备\n猫会掉毛、抓家具、半夜跑酷，都是正常行为\n养猫是十几年的责任，生病要花钱花时间\n多陪伴、多互动，小猫也会越来越粘人的~','/uploads/community/community_1774778335005.jpg','小猫天堂',NULL,NULL,1,0,'2026-03-29 17:59:03','2026-03-29 17:59:03');
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_like`
--

DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_like`
--

LOCK TABLES `post_like` WRITE;
/*!40000 ALTER TABLE `post_like` DISABLE KEYS */;
INSERT INTO `post_like` VALUES (12,22,12,'2026-03-24 03:14:04'),(13,20,12,'2026-03-24 03:14:05'),(14,18,12,'2026-03-24 03:14:07'),(16,21,12,'2026-03-24 03:14:15'),(17,26,1,'2026-03-29 17:59:18'),(20,19,1,'2026-05-21 19:17:58');
/*!40000 ALTER TABLE `post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `task_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务描述',
  `cat_id` bigint DEFAULT NULL COMMENT '关联猫咪ID：NULL-全体猫咪，其他-某只猫咪',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待分配，1-已分配，2-进行中，3-已完成，4-已取消',
  `assigned_to` bigint DEFAULT NULL COMMENT '指派的志愿者ID（用户ID）',
  `assigned_type` tinyint DEFAULT '0' COMMENT '分配方式：0-手动，1-自动',
  `priority` tinyint DEFAULT '0' COMMENT '优先级：0-普通，1-紧急',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `completion_description` text COLLATE utf8mb4_unicode_ci COMMENT '志愿者完成描述',
  `completion_images` text COLLATE utf8mb4_unicode_ci COMMENT '完成图片（逗号分隔URL）',
  `completion_time` datetime DEFAULT NULL COMMENT '完成提交时间',
  `completion_status` tinyint DEFAULT '0' COMMENT '完成状态：0-未提交，1-待审核，2-审核通过，3-审核拒绝',
  `completion_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`id`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_status` (`status`),
  KEY `idx_cat_id` (`cat_id`),
  KEY `fk_task_creator` (`create_by`),
  CONSTRAINT `fk_task_cat` FOREIGN KEY (`cat_id`) REFERENCES `cat` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_creator` FOREIGN KEY (`create_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_user` FOREIGN KEY (`assigned_to`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养护任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (3,'2','定时投喂',NULL,NULL,3,16,0,0,'2026-03-15 05:18:26','2026-03-15 13:18:28',1,'2026-04-05 15:26:59','1','/uploads/tasks/task_1775388149224.jpg','2026-04-05 19:05:24',2,'1'),(4,'0','医疗陪护','0',NULL,3,12,0,0,'2026-03-14 21:30:00','2026-03-15 13:30:03',1,'2026-03-15 14:39:55',NULL,NULL,NULL,0,NULL),(5,'2','清洁任务',NULL,NULL,3,11,0,0,'2026-03-15 05:30:18','2026-03-15 13:30:20',1,'2026-03-15 14:20:00',NULL,NULL,NULL,0,NULL),(6,'5','定时投喂','5',5,2,11,0,0,'2026-03-15 06:23:31','2026-03-15 14:23:32',1,NULL,'好','/uploads/tasks/task_1775387123924.png','2026-04-05 19:05:24',1,NULL),(7,'1','定期回访','',5,3,11,0,0,'2026-03-16 13:19:33','2026-03-16 21:19:34',1,'2026-04-05 18:29:58','4','/uploads/tasks/task_1775384895631.png','2026-04-05 18:28:16',2,NULL),(8,'33','清洁任务','',3,3,11,0,0,'2026-03-17 18:50:56','2026-03-18 02:50:58',1,'2026-04-05 17:49:11','好','/uploads/community/community_1775381786511.jpg','2026-04-05 17:36:27',2,'好'),(10,'3','医疗陪护','',8,3,11,0,0,'2026-04-05 08:50:46','2026-04-05 16:50:50',1,'2026-05-20 00:42:15','1','/uploads/tasks/task_1775402169807.png,/uploads/tasks/task_1775402169895.png','2026-04-05 23:16:10',2,NULL),(11,'5','清洁任务','',7,2,11,0,0,'2026-04-05 10:50:03','2026-04-05 18:50:05',1,NULL,NULL,NULL,NULL,0,NULL),(12,'9','定时投喂','先来固定位置领取猫粮，在固定位置投喂猫粮',NULL,0,NULL,0,1,'2026-04-28 16:00:00','2026-04-05 19:03:52',1,NULL,NULL,NULL,NULL,0,NULL),(13,'56','定时投喂','',4,0,NULL,0,0,'2026-04-19 07:27:50','2026-04-19 15:27:51',1,NULL,NULL,NULL,NULL,0,NULL),(14,'66','医疗陪护',NULL,7,0,NULL,0,0,'2026-04-19 07:28:00','2026-04-19 15:28:01',1,NULL,NULL,NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_application`
--

DROP TABLE IF EXISTS `task_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '报名用户ID',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审核，1-已选中，2-未选中，3-已取消',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_user` (`task_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_task_app_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_app_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务报名表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_application`
--

LOCK TABLES `task_application` WRITE;
/*!40000 ALTER TABLE `task_application` DISABLE KEYS */;
INSERT INTO `task_application` VALUES (2,4,12,'2026-03-15 13:57:56',1),(3,3,11,'2026-03-15 14:20:29',1),(4,8,11,'2026-03-18 02:51:34',1),(5,7,11,'2026-03-18 03:04:12',1),(6,6,11,'2026-03-18 03:09:53',1),(7,3,16,'2026-04-03 14:14:42',1),(8,14,17,'2026-04-22 15:15:38',0);
/*!40000 ALTER TABLE `task_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户，VOLUNTEER-志愿者，ADMIN-管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'testuser','$2a$10$5y4p1ZT.cWE/EAJJnl9G2uEy2RSHy.dfFMS31GEx4q6GpPWmi1Yyi','管理员','/uploads/avatars/avatar_1773754709970.webp','test@cat.com','13888888888','ADMIN',1,'2026-03-14 14:13:43','2026-03-14 21:22:13'),(8,'user','$2a$10$A/R4O6tg/M5IzmeQ7vqRqOp/neQILejUDTXjIMcshe2Y1XYMSkshG','测试用户',NULL,'test@cat.com','13888888888','USER',1,'2026-03-14 20:03:52','2026-04-21 14:33:51'),(9,'testuser_88','$2a$10$Chxyk45vNhvNKckMDJoiQ.fPF2X7NSs4bGd2AwDh9OdjmN/6Mkb7e','测试用户',NULL,'test@cat.com','13888888888','USER',1,'2026-03-14 20:04:18','2026-03-14 20:04:18'),(10,'user1','$2a$10$O4L0UUfpyc6ZXl4Ybdgx3eZW/qn7wH7Czy447uWdMM4GQvLozSg1q','猫猫~','/uploads/avatars/avatar_1773754631974.webp','test@cat.com','13888888887','USER',1,'2026-03-14 20:17:28','2026-03-14 22:19:49'),(11,'testvolunteer','$2a$10$AoF29F1qGxigEoFK.dQFIOGMYD4trqsg0NfUgdgbQbSIsm3P5pn7a','VOLUNTEER','/uploads/avatars/avatar_1774293079189.jpg','test@cat.com','13888888889','VOLUNTEER',1,'2026-03-14 22:00:31','2026-03-14 22:01:07'),(12,'testvolunteer2','$2a$10$AoF29F1qGxigEoFK.dQFIOGMYD4trqsg0NfUgdgbQbSIsm3P5pn7a','邦邦好卡哇','/uploads/avatars/avatar_1774293183256.jpg','test@cat.com','13888888888','VOLUNTEER',1,'2026-03-14 22:00:31','2026-03-14 22:01:07'),(16,'lili','$2a$10$gnhnADa/oG73QQdwgaZzLuB/ZHAzyxxYkd96gLhF2pCn/2tkjLNf2','丽丽',NULL,'1349552599@qq.com','16953337485','VOLUNTEER',1,'2026-04-03 14:13:38','2026-04-03 14:13:38'),(17,'ooo','$2a$10$E4npbFlET4Rvhn4FyZTwYeU8BLxwWFIN8B1qkxKCuJLCcdl9CRA4i','ooo',NULL,'31445548946@qq.com','15985426394','VOLUNTEER',1,'2026-04-03 18:05:51','2026-04-03 18:05:51'),(102,'111','$2a$10$XiJkxNNVk9HOnwYNFE0GgOXpyTwRKnc1fvNyXlLjpZZKVJtedBzW6','111',NULL,'12345678@cat.com','13888888888','USER',1,'2026-04-22 14:59:34','2026-04-22 14:59:34');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

SET FOREIGN_KEY_CHECKS = 1;