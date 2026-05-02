/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50622
 Source Host           : localhost:3306
 Source Schema         : citylife_review

 Target Server Type    : MySQL
 Target Server Version : 50622
 File Encoding         : 65001

 Date: 02/03/2022 23:12:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_blog
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `shop_id` bigint(20) NOT NULL COMMENT '鍟嗘埛id',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '鐢ㄦ埛id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏍囬',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鎺㈠簵鐨勭収鐗囷紝鏈€澶?寮狅紝澶氬紶浠",\"闅斿紑',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎺㈠簵鐨勬枃瀛楁弿杩?,
  `liked` int(8) UNSIGNED NULL DEFAULT 00000000 COMMENT '鐐硅禐鏁伴噺',
  `comments` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '璇勮鏁伴噺',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_blog
-- ----------------------------
INSERT INTO `tb_blog` VALUES (4, 4, 2, '鏃犲敖娴极鐨勫鏅氫辅鍦ㄤ竾鑺变笡涓憞鏅冪潃绾㈤厭鏉煃峰搧鎴樻枾鐗涙帓馃ォ', '/imgs/blogs/7/14/4771fefb-1a87-4252-816c-9f7ec41ffa4a.jpg,/imgs/blogs/4/10/2f07e3c9-ddce-482d-9ea7-c21450f8d7cd.jpg,/imgs/blogs/2/6/b0756279-65da-4f2d-b62a-33f74b06454a.jpg,/imgs/blogs/10/7/7e97f47d-eb49-4dc9-a583-95faa7aed287.jpg,/imgs/blogs/1/2/4a7b496b-2a08-4af7-aa95-df2c3bd0ef97.jpg,/imgs/blogs/14/3/52b290eb-8b5d-403b-8373-ba0bb856d18e.jpg', '鐢熸椿灏辨槸涓€鍗婄儫鐏蜂竴鍗婅瘲鎰廫n鎵嬫墽鐑熺伀璋嬬敓娲宦峰績鎬€璇楁剰浠ヨ皨鐖甭穃n褰撶劧 鐢锋湅鍙嬬粰涓嶄簡鐨勬氮婕瀛︿細鑷繁缁欚煃抃n鏃犳硶閲嶆潵鐨勪竴鐢熉峰敖閲忓揩涔?\n\n馃彴銆屽皬绛戦噷路绁炵娴极鑺卞洯椁愬巺銆嶐煆癨n\n馃挴杩欐槸涓€瀹舵渶鏈€鏈€缇庤姳鍥殑瑗块鍘吢峰埌澶勯兘鏄姳椁愭涓婃槸鑺卞墠鍙版槸鑺? 缇庡ソ鏃犲涓嶅湪\n鍝佷竴鍙ｈ憽钀勯厭锛岀淮浜氱孩閰掗┈鐟熷叞路寰喓涓婂ご宸ヤ綔鐨勭柌鎯秷澶辨棤闄吢风敓濡傛澶氬▏馃崈\n\n馃搷鍦板潃:寤跺畨璺?00鍙?瀹朵箰绂忛潰)\n\n馃殞浜ら€?鍦伴搧鈶犲彿绾垮畾瀹夎矾B鍙ｅ嚭鍙宠浆杩囦笅閫氶亾鍙宠浆灏卞埌鍟︼綖\n\n--------------馃ィ鑿滃搧璇︽儏馃ィ---------------\n\n銆屾垬鏂х墰鎺掋€峔n瓒呭ぇ涓€鍧楁垬鏂х墰鎺掔粡杩囩伀鐒扮殑鐐欑儰鍙戝嚭闃甸樀棣欙紝澶栫劍閲屽璁╀汉鍨傛稁娆叉淮锛屽垏寮€鐗涙帓鐨勯偅涓€鍒伙紝鐗涙帓鐨勬眮姘撮『鍔挎祦浜嗗嚭鏉ワ紝鍒嗙啛鐨勭墰鎺掕倝璐ㄨ蒋锛岀畝鐩寸粏瀚╁埌鐘锛屼竴鍒婚兘绛変笉浜嗚鏀惧叆鍢撮噷鍜€鍤硷綖\n\n銆屽ザ娌瑰煿鏍规剰闈€峔n澶お澶ソ鍚冧簡馃挴\n鎴戠湡鐨勬棤娉曞舰瀹瑰畠鐨勭編濡欙紝鎰忛潰娣峰悎濂舵补棣欒弴鐨勯鍛崇湡鐨勫お澶お棣欎簡锛屾垜鐪熺殑鑸旂洏浜嗭紝涓€涓佺偣缇庡懗閮戒笉鎯虫氮璐光€硷笍\n\n銆岄鑿滄眮鐑ら矆楸笺€峔n杩欎釜閰辨槸杈ｇ殑 鐪熺殑缁濆ソ鍚冣€硷笍\n椴堥奔鏈韩灏卞緢瀚╂病浠€涔堝埡锛岀儰杩囦箣鍚庡鐨叆閰ョ殑锛岄奔鑲夎樃涓婇叡鏂欐牴鏈仠涓嶄笅鏉ュ晩鍟婂晩鍟奬n鑳藉悆杈ｆ鐨勫皬浼欎即涓€瀹氳灏濆皾\n\n 闈炲父鍙?濂藉悆瀛愷煃絓n\n--------------馃崈涓汉鎰熷彈馃崈---------------\n\n銆愷煈煆烩€嶐煃虫湇鍔°€慭n灏忓濮愮壒鍒€愬績鐨勭粰鎴戜滑浠嬬粛褰╃エ 鎺ㄨ崘鐗硅壊鑿滃搧锛屾媿鐓ч渶瑕佸府蹇欎篃鏄敖蹇冨敖鍔涢厤鍚堬紝澶埍浠栦滑浜哱n\n銆愷煃冪幆澧冦€慭n姣旇緝鏈夋牸璋冪殑瑗块鍘?鏁翠釜椁愬巺鐨勫竷灞€鍙О寰椾笂鐨勪竾鑺变笡鐢?鏈夌鍦ㄤ汉闂翠粰澧冪殑鎰熻馃尭\n闆嗙編椋熺編閰掍笌椴滆姳涓轰竴浣撶殑椋庢牸搴楅摵 浠や汉鍚戝線\n鐑熺伀鐨嗘槸鐢熸椿 浜洪棿鐨嗘槸娴极', 00000013, 00000104, '2021-12-28 19:50:01', '2022-01-06 20:30:03');
INSERT INTO `tb_blog` VALUES (5, 1, 2, '浜哄潎30馃挵鏉窞杩欏娓紡鑼堕鍘呮垜鐤媯鎵揷all鈥硷笍', '/imgs/blogs/4/7/863cc302-d150-420d-a596-b16e9232a1a6.jpg,/imgs/blogs/11/12/8b37d208-9414-4e78-b065-9199647bb3e3.jpg,/imgs/blogs/4/1/fa74a6d6-3026-4cb7-b0b6-35abb1e52d11.jpg,/imgs/blogs/9/12/ac2ce2fb-0605-4f14-82cc-c962b8c86688.jpg,/imgs/blogs/4/0/26a7cd7e-6320-432c-a0b4-1b7418f45ec7.jpg,/imgs/blogs/15/9/cea51d9b-ac15-49f6-b9f1-9cf81e9b9c85.jpg', '鍙堝悆鍒颁竴瀹跺ソ鍚冪殑鑼堕鍘咅煃寸幆澧冩槸鎬€鏃vb娓馃摵杈瑰悆杈规媿鐓х墖馃摲鍑犲崄绉嶈彍鍝佸潎浠烽兘鍦?0+馃挵鍙互鏄緢骞充环浜嗭紒\n路\n搴楀悕锛氫節璁板啺鍘?杩滄磱搴?\n鍦板潃锛氭澀宸炲競涓芥按璺繙娲嬩箰鍫ゆ腐璐熶竴妤硷紙婧滃啺鍦烘梺杈癸級\n路\n鉁旓笍榛劧閿€榄傞キ锛?8馃挵锛塡n杩欑楗垜鍚圭垎锛佺背楗笂鐩栨弧浜嗙敎鐢滅殑鍙夌儳 杩樻湁涓ら婧忓績铔嬸煃虫瘡涓€绮掔背楗兘瑁圭潃娴撻儊鐨勯叡姹?鍏夌洏浜哱n路\n鉁旓笍閾滈敚婀炬紡濂跺崕锛?8馃挵锛塡n榛勬补鍚愬徃鐑ょ殑鑴嗚剢鐨?涓婇潰娲掓弧浜嗗彲鍙矇馃崼涓€鍒€鍒囧紑 濂剁洊娴佸績鍍忕€戝竷涓€鏍锋祦鍑烘潵  婊¤冻\n路\n鉁旓笍绁炰粰涓€鍙ｈタ澶氬＋澹紙16馃挵锛塡n绠€绠€鍗曞崟鍗磋秴绾уソ鍚冿紒瑗垮澹儰鐨勫緢鑴?榛勬补鍛虫祿閮?闈㈠寘浣撹秴绾ф煍杞?涓婇潰娣嬩簡鐐间钩\n路\n鉁旓笍鎬€鏃т簲鏌崇偢铔嬮キ锛?8馃挵锛塡n鍥涗釜楦¤泲鐐告垚钃澗鐨勭偢铔嬶紒涔熷お濂藉悆浜嗗惂锛佽繕鏈夊ぇ鍧楅浮鎺?涓婃穻浜嗛吀鐢滅殑閰辨眮 澶悎鎴戣儍鍙ｄ簡锛侊紒\n路\n鉁旓笍鐑у懗鍙屾嫾渚嬬墝锛?6馃挵锛塡n閫変簡鐑ч箙鉃曞弶鐑?浠栧鐑ц厞鍝佽川鐪熺殑鎯婅壋鍒版垜锛佹嵁璇存槸姣忔棩骞垮窞鍙戣揣 鍒板簵鐜扮儳鐜板崠鐨勯粦妫曢箙 姣忓彛閮芥槸姝ｅ畻鐨勫懗閬擄紒鑲夎川寰堝 鐨秴绾ц秴绾ч叆鑴嗭紒涓€鍙ｇ垎娌癸紒鍙夌儳鑲変篃涓€鐐归兘涓嶆煷 鐢滅敎鐨勫緢鍏ュ懗 鎼厤姊呭瓙閰卞緢瑙ｈ吇 锛乗n路\n鉁旓笍绾㈢儳鑴嗙毊涔抽附锛?8.8馃挵锛塡n涔抽附寰堝ぇ鍙?杩欎釜浠锋牸涔熷お鍒掔畻浜嗗惂锛?鑲夎川寰堟湁鍤煎姴 鑴嗙毊寰堥叆 瓒婂悆瓒婇锝瀄n路\n鉁旓笍澶ф弧瓒冲皬鍚冩嫾鐩橈紙25馃挵锛塡n缈呭皷鉃曞挅鍠遍奔铔嬧灂铦磋澏铏锯灂鐩愰叆楦nzui鍠滄閲岄潰鐨勫挅鍠遍奔锛佸挅鍠遍叡棣欑敎娴撻儊锛侀奔铔嬪緢q寮癸綖\n路\n鉁旓笍娓紡鐔婁粩涓濊濂惰尪锛?9馃挵锛塡n灏忕唺馃惢閫犲瀷鐨勫ザ鑼跺啺涔熷お鍙埍浜嗭紒棰滃€兼媴褰?寰堝湴閬撶殑涓濊濂惰尪 鑼跺懗鐗瑰埆娴撻儊锝瀄n路', 00000002, 00000000, '2021-12-28 20:57:49', '2022-01-06 20:30:22');
INSERT INTO `tb_blog` VALUES (6, 10, 1, '鏉窞鍛ㄦ湯濂藉幓澶勶綔馃挵50灏卞彲浠ラ獞椹暒馃悗', '/imgs/blogs/blog1.jpg', '鏉窞鍛ㄦ湯濂藉幓澶勶綔馃挵50灏卞彲浠ラ獞椹暒馃悗', 00000011, 00000000, '2022-01-11 16:05:47', '2022-01-11 16:05:47');
INSERT INTO `tb_blog` VALUES (7, 10, 1, '鏉窞鍛ㄦ湯濂藉幓澶勶綔馃挵50灏卞彲浠ラ獞椹暒馃悗', '/imgs/blogs/blog1.jpg', '鏉窞鍛ㄦ湯濂藉幓澶勶綔馃挵50灏卞彲浠ラ獞椹暒馃悗', 00000011, 00000000, '2022-01-11 16:05:47', '2022-01-11 16:05:47');

-- ----------------------------
-- Table structure for tb_blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '鐢ㄦ埛id',
  `blog_id` bigint(20) UNSIGNED NOT NULL COMMENT '鎺㈠簵id',
  `parent_id` bigint(20) UNSIGNED NOT NULL COMMENT '鍏宠仈鐨?绾ц瘎璁篿d锛屽鏋滄槸涓€绾ц瘎璁猴紝鍒欏€间负0',
  `answer_id` bigint(20) UNSIGNED NOT NULL COMMENT '鍥炲鐨勮瘎璁篿d',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍥炲鐨勫唴瀹?,
  `liked` int(8) UNSIGNED NULL DEFAULT NULL COMMENT '鐐硅禐鏁?,
  `status` tinyint(1) UNSIGNED NULL DEFAULT NULL COMMENT '鐘舵€侊紝0锛氭甯革紝1锛氳涓炬姤锛?锛氱姝㈡煡鐪?,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_blog_comments
-- ----------------------------

-- ----------------------------
-- Table structure for tb_follow
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '鐢ㄦ埛id',
  `follow_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '鍏宠仈鐨勭敤鎴穒d',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_follow
-- ----------------------------

-- ----------------------------
-- Table structure for tb_seckill_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher`  (
  `voucher_id` bigint(20) UNSIGNED NOT NULL COMMENT '鍏宠仈鐨勪紭鎯犲埜鐨刬d',
  `stock` int(8) NOT NULL COMMENT '搴撳瓨',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `begin_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '鐢熸晥鏃堕棿',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '澶辨晥鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '绉掓潃浼樻儬鍒歌〃锛屼笌浼樻儬鍒告槸涓€瀵逛竴鍏崇郴' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_seckill_voucher
-- ----------------------------

-- ----------------------------
-- Table structure for tb_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍟嗛摵鍚嶇О',
  `type_id` bigint(20) UNSIGNED NOT NULL COMMENT '鍟嗛摵绫诲瀷鐨刬d',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍟嗛摵鍥剧墖锛屽涓浘鐗囦互\',\'闅斿紑',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鍟嗗湀锛屼緥濡傞檰瀹跺槾',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍦板潃',
  `x` double UNSIGNED NOT NULL COMMENT '缁忓害',
  `y` double UNSIGNED NOT NULL COMMENT '缁村害',
  `avg_price` bigint(10) UNSIGNED NULL DEFAULT NULL COMMENT '鍧囦环锛屽彇鏁存暟',
  `sold` int(10) UNSIGNED NOT NULL COMMENT '閿€閲?,
  `comments` int(10) UNSIGNED NOT NULL COMMENT '璇勮鏁伴噺',
  `score` int(2) UNSIGNED NOT NULL COMMENT '璇勫垎锛?~5鍒嗭紝涔?0淇濆瓨锛岄伩鍏嶅皬鏁?,
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '钀ヤ笟鏃堕棿锛屼緥濡?10:00-22:00',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop
-- ----------------------------
INSERT INTO `tb_shop` VALUES (1, '103鑼堕鍘?, 1, 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg,https://qcloud.dpfile.com/pc/IOf6VX3qaBgFXFVgp75w-KKJmWZjFc8GXDU8g9bQC6YGCpAmG00QbfT4vCCBj7njuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '澶у叧', '閲戝崕璺敠鏄屾枃鍗庤嫅29鍙?, 120.149192, 30.316078, 80, 0000004215, 0000003035, 37, '10:00-22:00', '2021-12-22 18:10:39', '2022-01-13 17:32:19');
INSERT INTO `tb_shop` VALUES (2, '钄￠Μ娲稕鐑よ倝路鑰佸寳浜摐閿呮懂缇婅倝', 1, 'https://p0.meituan.net/bbia/c1870d570e73accbc9fee90b48faca41195272.jpg,http://p0.meituan.net/mogu/397e40c28fc87715b3d5435710a9f88d706914.jpg,https://qcloud.dpfile.com/pc/MZTdRDqCZdbPDUO0Hk6lZENRKzpKRF7kavrkEI99OxqBZTzPfIxa5E33gBfGouhFuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '鎷卞妗?涓婂', '涓婂璺?035鍙凤紙涓浗宸ュ晢閾惰鏃侊級', 120.151505, 30.333422, 85, 0000002160, 0000001460, 46, '11:30-03:00', '2021-12-22 19:00:13', '2022-01-11 16:12:26');
INSERT INTO `tb_shop` VALUES (3, '鏂扮櫧楣块鍘?杩愭渤涓婅搴?', 1, 'https://p0.meituan.net/biztone/694233_1619500156517.jpeg,https://img.meituan.net/msmerchant/876ca8983f7395556eda9ceb064e6bc51840883.png,https://img.meituan.net/msmerchant/86a76ed53c28eff709a36099aefe28b51554088.png', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇僃5', 120.151954, 30.32497, 61, 0000012035, 0000008045, 47, '10:30-21:00', '2021-12-22 19:10:05', '2022-01-11 16:12:42');
INSERT INTO `tb_shop` VALUES (4, 'Mamala(鏉窞杩滄磱涔愬牑娓簵)', 1, 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '鎷卞妗?涓婂', '涓芥按璺?6鍙疯繙娲嬩箰鍫ゆ腐鍟嗗煄2鏈?灞侭115鍙?, 120.146659, 30.312742, 290, 0000013519, 0000009529, 49, '11:00-22:00', '2021-12-22 19:17:15', '2022-01-11 16:12:51');
INSERT INTO `tb_shop` VALUES (5, '娴峰簳鎹炵伀閿?姘存櫠鍩庤喘鐗╀腑蹇冨簵锛?, 1, 'https://img.meituan.net/msmerchant/054b5de0ba0b50c18a620cc37482129a45739.jpg,https://img.meituan.net/msmerchant/59b7eff9b60908d52bd4aea9ff356e6d145920.jpg,https://qcloud.dpfile.com/pc/Qe2PTEuvtJ5skpUXKKoW9OQ20qc7nIpHYEqJGBStJx0mpoyeBPQOJE4vOdYZwm9AuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '澶у叧', '涓婂璺?58鍙锋按鏅跺煄璐墿涓績F6', 120.15778, 30.310633, 104, 0000004125, 0000002764, 49, '10:00-07:00', '2021-12-22 19:20:58', '2022-01-11 16:13:01');
INSERT INTO `tb_shop` VALUES (6, '骞哥閲岃€佸寳浜懂閿咃紙涓濊仈搴楋級', 1, 'https://img.meituan.net/msmerchant/e71a2d0d693b3033c15522c43e03f09198239.jpg,https://img.meituan.net/msmerchant/9f8a966d60ffba00daf35458522273ca658239.jpg,https://img.meituan.net/msmerchant/ef9ca5ef6c05d381946fe4a9aa7d9808554502.jpg', '鎷卞妗?涓婂', '閲戝崕鍗楄矾189鍙蜂笣鑱?66鍙?, 120.148603, 30.318618, 130, 0000009531, 0000007324, 46, '11:00-13:50,17:00-20:50', '2021-12-22 19:24:53', '2022-01-11 16:13:09');
INSERT INTO `tb_shop` VALUES (7, '鐐夐奔(鎷卞涓囪揪骞垮満搴?', 1, 'https://img.meituan.net/msmerchant/909434939a49b36f340523232924402166854.jpg,https://img.meituan.net/msmerchant/32fd2425f12e27db0160e837461c10303700032.jpg,https://img.meituan.net/msmerchant/f7022258ccb8dabef62a0514d3129562871160.jpg', '鍖楅儴鏂板煄', '鏉璺?66鍙蜂竾杈惧晢涓氫腑蹇?骞?鍗曞厓409瀹?閾轰綅鍙?005)', 120.124691, 30.336819, 85, 0000002631, 0000001320, 47, '00:00-24:00', '2021-12-22 19:40:52', '2022-01-11 16:13:19');
INSERT INTO `tb_shop` VALUES (8, '娴呰崏灞嬪鍙革紙杩愭渤涓婅搴楋級', 1, 'https://img.meituan.net/msmerchant/cf3dff697bf7f6e11f4b79c4e7d989e4591290.jpg,https://img.meituan.net/msmerchant/0b463f545355c8d8f021eb2987dcd0c8567811.jpg,https://img.meituan.net/msmerchant/c3c2516939efaf36c4ccc64b0e629fad587907.jpg', '杩愭渤涓婅', '鎷卞鍖洪噾鍗庤矾80鍙疯繍娌充笂琛桞1', 120.150526, 30.325231, 88, 0000002406, 0000001206, 46, ' 11:00-21:30', '2021-12-22 19:51:06', '2022-01-11 16:13:25');
INSERT INTO `tb_shop` VALUES (9, '缇婅€佷笁缇婅潕瀛愮墰浠旀帓鍖楁淳鐐伀閿?杩愭渤涓婅搴?', 1, 'https://p0.meituan.net/biztone/163160492_1624251899456.jpeg,https://img.meituan.net/msmerchant/e478eb16f7e31a7f8b29b5e3bab6de205500837.jpg,https://img.meituan.net/msmerchant/6173eb1d18b9d70ace7fdb3f2dd939662884857.jpg', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇僃5', 120.150598, 30.325251, 101, 0000002763, 0000001363, 44, '11:00-21:30', '2021-12-22 19:53:59', '2022-01-11 16:13:34');
INSERT INTO `tb_shop` VALUES (10, '寮€涔愯开KTV锛堣繍娌充笂琛楀簵锛?, 2, 'https://p0.meituan.net/joymerchant/a575fd4adb0b9099c5c410058148b307-674435191.jpg,https://p0.meituan.net/merchantpic/68f11bf850e25e437c5f67decfd694ab2541634.jpg,https://p0.meituan.net/dpdeal/cb3a12225860ba2875e4ea26c6d14fcc197016.jpg', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇僃4', 120.149093, 30.324666, 67, 0000026891, 0000000902, 37, '00:00-24:00', '2021-12-22 20:25:16', '2021-12-22 20:25:16');
INSERT INTO `tb_shop` VALUES (11, 'INLOVE KTV(姘存櫠鍩庡簵)', 2, 'https://p0.meituan.net/dpmerchantpic/53e74b200211d68988a4f02ae9912c6c1076826.jpg,https://qcloud.dpfile.com/pc/4iWtIvzLzwM2MGgyPu1PCDb4SWEaKqUeHm--YAt1EwR5tn8kypBcqNwHnjg96EvT_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/WZsJWRI447x1VG2x48Ujgu7vwqksi_9WitdKI4j3jvIgX4MZOpGNaFtM93oSSizbGybIjx5eX6WNgCPvcASYAw.jpg', '姘存櫠鍩?, '涓婂璺?58鍙锋按鏅跺煄璐墿涓績6灞?, 120.15853, 30.310002, 75, 0000035977, 0000005684, 47, '11:30-06:00', '2021-12-22 20:29:02', '2021-12-22 20:39:00');
INSERT INTO `tb_shop` VALUES (12, '榄?鏉窞杩滄磱涔愬牑娓簵)', 2, 'https://p0.meituan.net/dpmerchantpic/63833f6ba0393e2e8722420ef33f3d40466664.jpg,https://p0.meituan.net/dpmerchantpic/ae3c94cc92c529c4b1d7f68cebed33fa105810.png,', '杩滄磱涔愬牑娓?, '涓芥按璺?8鍙疯繙娲嬩箰鍫ゆ腐F4', 120.14983, 30.31211, 88, 0000006444, 0000000235, 46, '10:00-02:00', '2021-12-22 20:34:34', '2021-12-22 20:34:34');
INSERT INTO `tb_shop` VALUES (13, '璁碖鎷夐噺璐㎏TV(鍖楀煄澶╁湴搴?', 2, 'https://p1.meituan.net/merchantpic/598c83a8c0d06fe79ca01056e214d345875600.jpg,https://qcloud.dpfile.com/pc/HhvI0YyocYHRfGwJWqPQr34hRGRl4cWdvlNwn3dqghvi4WXlM2FY1te0-7pE3Wb9_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/F5ZVzZaXFE27kvQzPnaL4V8O9QCpVw2nkzGrxZE8BqXgkfyTpNExfNG5CEPQX4pjGybIjx5eX6WNgCPvcASYAw.jpg', 'D32澶╅槼璐墿涓績', '婀栧窞琛?67鍙峰寳鍩庡ぉ鍦?灞?, 120.130453, 30.327655, 58, 0000018997, 0000001857, 41, '12:00-02:00', '2021-12-22 20:38:54', '2021-12-22 20:40:04');
INSERT INTO `tb_shop` VALUES (14, '鏄熻仛浼欿TV(鎷卞鍖轰竾杈惧簵)', 2, 'https://p0.meituan.net/dpmerchantpic/f4cd6d8d4eb1959c3ea826aa05a552c01840451.jpg,https://p0.meituan.net/dpmerchantpic/2efc07aed856a8ab0fc75c86f4b9b0061655777.jpg,https://qcloud.dpfile.com/pc/zWfzzIorCohKT0bFwsfAlHuayWjI6DBEMPHHncmz36EEMU9f48PuD9VxLLDAjdoU_Gd2X_f-v9T8Yj4uLt25Gg.jpg', '鍖楅儴鏂板煄', '鏉璺?66鍙蜂竾杈惧箍鍦篊搴?-2F', 120.128958, 30.337252, 60, 0000017771, 0000000685, 47, '10:00-22:00', '2021-12-22 20:48:54', '2021-12-22 20:48:54');

-- ----------------------------
-- Table structure for tb_shop_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绫诲瀷鍚嶇О',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鍥炬爣',
  `sort` int(3) UNSIGNED NULL DEFAULT NULL COMMENT '椤哄簭',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop_type
-- ----------------------------
INSERT INTO `tb_shop_type` VALUES (1, '缇庨', '/types/ms.png', 1, '2021-12-22 20:17:47', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (2, 'KTV', '/types/KTV.png', 2, '2021-12-22 20:18:27', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (3, '涓戒汉路缇庡彂', '/types/lrmf.png', 3, '2021-12-22 20:18:48', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (4, '鍋ヨ韩杩愬姩', '/types/jsyd.png', 10, '2021-12-22 20:19:04', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (5, '鎸夋懇路瓒崇枟', '/types/amzl.png', 5, '2021-12-22 20:19:27', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (6, '缇庡SPA', '/types/spa.png', 6, '2021-12-22 20:19:35', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (7, '浜插瓙娓镐箰', '/types/qzyl.png', 7, '2021-12-22 20:19:53', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (8, '閰掑惂', '/types/jiuba.png', 8, '2021-12-22 20:20:02', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (9, '杞拌洞棣?, '/types/hpg.png', 9, '2021-12-22 20:20:08', '2021-12-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (10, '缇庣潾路缇庣敳', '/types/mjmj.png', 4, '2021-12-22 20:21:46', '2021-12-23 11:24:31');

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鎵嬫満鍙风爜',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '瀵嗙爜锛屽姞瀵嗗瓨鍌?,
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '鏄电О锛岄粯璁ゆ槸鐢ㄦ埛id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浜虹墿澶村儚',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1010 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, '13686869696', '', '灏忛奔鍚屽', '/imgs/blogs/blog1.jpg', '2021-12-24 10:27:19', '2022-01-11 16:04:00');
INSERT INTO `tb_user` VALUES (2, '13838411438', '', '鍙彲浠婂ぉ涓嶅悆鑲?, '/imgs/icons/kkjtbcr.jpg', '2021-12-24 15:14:39', '2021-12-28 19:58:04');
INSERT INTO `tb_user` VALUES (4, '13456789011', '', 'user_slxaxy2au9f3tanffaxr', '', '2022-01-07 12:07:53', '2022-01-07 12:07:53');
INSERT INTO `tb_user` VALUES (5, '13456789001', '', 'user_n0bb8mwwg4', '', '2022-01-07 16:11:33', '2022-01-07 16:11:33');

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '涓婚敭锛岀敤鎴穒d',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '鍩庡競鍚嶇О',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '涓汉浠嬬粛锛屼笉瑕佽秴杩?28涓瓧绗?,
  `fans` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '绮変笣鏁伴噺',
  `followee` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '鍏虫敞鐨勪汉鐨勬暟閲?,
  `gender` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '鎬у埆锛?锛氱敺锛?锛氬コ',
  `birthday` date NULL DEFAULT NULL COMMENT '鐢熸棩',
  `credits` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '绉垎',
  `level` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '浼氬憳绾у埆锛?~9绾?0浠ｈ〃鏈紑閫氫細鍛?,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------

-- ----------------------------
-- Table structure for tb_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
  `shop_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '鍟嗛摵id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '浠ｉ噾鍒告爣棰?,
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鍓爣棰?,
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浣跨敤瑙勫垯',
  `pay_value` bigint(10) UNSIGNED NOT NULL COMMENT '鏀粯閲戦锛屽崟浣嶆槸鍒嗐€備緥濡?00浠ｈ〃2鍏?,
  `actual_value` bigint(10) NOT NULL COMMENT '鎶垫墸閲戦锛屽崟浣嶆槸鍒嗐€備緥濡?00浠ｈ〃2鍏?,
  `type` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,鏅€氬埜锛?,绉掓潃鍒?,
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,涓婃灦; 2,涓嬫灦; 3,杩囨湡',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher
-- ----------------------------
INSERT INTO `tb_voucher` VALUES (1, 1, '50鍏冧唬閲戝埜', '鍛ㄤ竴鑷冲懆鏃ュ潎鍙娇鐢?, '鍏ㄥ満閫氱敤\\n鏃犻渶棰勭害\\n鍙棤闄愬彔鍔燶\涓嶅厬鐜般€佷笉鎵鹃浂\\n浠呴檺鍫傞', 4750, 5000, 0, 1, '2022-01-04 09:42:39', '2022-01-04 09:43:31');

-- ----------------------------
-- Table structure for tb_voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order`  (
  `id` bigint(20) NOT NULL COMMENT '涓婚敭',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '涓嬪崟鐨勭敤鎴穒d',
  `voucher_id` bigint(20) UNSIGNED NOT NULL COMMENT '璐拱鐨勪唬閲戝埜id',
  `pay_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '鏀粯鏂瑰紡 1锛氫綑棰濇敮浠橈紱2锛氭敮浠樺疂锛?锛氬井淇?,
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '璁㈠崟鐘舵€侊紝1锛氭湭鏀粯锛?锛氬凡鏀粯锛?锛氬凡鏍搁攢锛?锛氬凡鍙栨秷锛?锛氶€€娆句腑锛?锛氬凡閫€娆?,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '涓嬪崟鏃堕棿',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '鏀粯鏃堕棿',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '鏍搁攢鏃堕棿',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '閫€娆炬椂闂?,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_voucher`(`user_id`, `voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher_order
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
