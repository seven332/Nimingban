/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.nimingban.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hippo.nimingban.client.ac.data.ACForum;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.CommonPost;
import com.hippo.nimingban.client.data.DisplayForum;
import com.hippo.nimingban.dao.ACCommonPostDao;
import com.hippo.nimingban.dao.ACCommonPostRaw;
import com.hippo.nimingban.dao.ACForumDao;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.dao.ACRecordDao;
import com.hippo.nimingban.dao.ACRecordRaw;
import com.hippo.nimingban.dao.DaoMaster;
import com.hippo.nimingban.dao.DaoSession;
import com.hippo.nimingban.dao.DraftDao;
import com.hippo.nimingban.dao.DraftRaw;
import com.hippo.util.Arrays2;
import com.hippo.yorozuya.AssertUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;

public final class DB {

    private static String[] AC_FORUM_ID_ARRAY = {"4", "20", "11", "30", "32", "40", "35", "56", "103", "17", "98",
            "102", "97", "89", "27", "81", "14", "12", "99", "90", "87", "19", "64", "6", "5",
            "93", "101", "2", "73", "72", "86", "22", "70", "95", "10", "34", "51", "44", "23",
            "45", "80", "28", "38", "29", "24", "25", "92", "16", "100", "13", "55", "39", "31",
            "54", "33", "37", "75", "88", "18"};

    private static String[] AC_FORUM_MSG_ARRAY = {
            // 综合版1
            "<p><a href=\"http://www.acfun.tv/random.aspx\" target=\"_blank\"><img alt=\"随便看看~\" src=\"http://cover.acfunwiki.org/face.php\" style=\"float:left\" /></a></p>\n" +
                    "\n" +
                    "<p>&bull;欢迎光临，有关A岛的介绍请点&rarr;<a href=\"http://sx1.atv.ac:5566/index.php/%C4%E4%C3%FB%CC%D6%C2%DB%B0%E6\" target=\"_blank\">这里</a><br />\n" +
                    "&bull;禁H，露点图删，永久封推广链接，<strong>严禁张贴他人隐私资料，引战一律删。</strong><br />\n" +
                    "&bull;请文明讨论，人身攻击、辱骂内容一律砍+没收饼干，<strong>造谣砍串封IP。</strong><br />\n" +
                    "&bull;主站相关&rarr;<a href=\"/f/%E5%80%BC%E7%8F%AD%E5%AE%A4\">值班室</a>。宣传QQ群&rarr;<a href=\"http://sx1.atv.ac:5566/index.php/QQ\" target=\"_blank\">QQ群索引</a><br />\n" +
                    "&bull;有关<a href=\"http://sx1.atv.ac:5566/index.php/SAGE\" target=\"_blank\">SAGE</a>的规则请点&rarr;<a href=\"http://sx1.atv.ac:5566/index.php/SAGE\" target=\"_blank\">这里</a><br />\n" +
                    "&bull;<strong>丧尸们<a href=\"http://static.acfun.mm111.net/h/mp3/tnnaii-h-island-c.mp3\">快来食我大雕啦</a>~</strong><br />\n" +
                    "&bull;本版发文间隔为30秒。</p>\n",
            // 欢乐恶搞
            "<img src=\"http://static.acfun.mm111.net/h/upload2/images/2014-06-05/aa2a8b7f-5071-48a7-b068-fecee12d3fd9.gif\" style=\"float:left\"> &bull;H物求种求红领巾，砍楼+至少24小时封禁。这里不是H网<br />\n" +
                    "&bull;禁止张贴密集图、恶心图，违者一律砍串封IP。<br />\n" +
                    "&bull;引战吃SAGE，人身攻击和辱骂文字一律砍，其余同总版规。<br />\n" +
                    "&bull;本版发文间隔为15秒。\n",
            // 推理
            "•微小说、图片推理、解谜。<br/>•本版发文间隔为15秒。",
            // 技术宅
            "•程序语言、压制投稿、视频制作以及各领域的技术问题<br />\n" +
                    "•技术版官方QQ群：243472212<br />\n" +
                    "•<strong style='color:red'>电脑组装等一般问题请移步<a href=\"/f/数码\">数码</a>，否则删</strong><br />\n" +
                    "•本版发文间隔为15秒。",
            // 料理
            "<p><img alt=\"\" src=\"http://static.acfun.mm111.net/h/image/20147/33ed6df166ff1b18d040c24c309d7546.png\" style=\"float:left\" /></p>\n" +
                    "\n" +
                    "<p>&nbsp;</p>\n" +
                    "\n" +
                    "<p>你们都是吃货！汪！</p>\n" +
                    "\n" +
                    "<p>今天<a href=\"/t/283078\">开心</a>还是<a href=\"/t/2950161\">不开心</a>呢？</p>\n" +
                    "\n" +
                    "<p>本版发文间隔为15秒</p>\n" +
                    "\n" +
                    "<p>A岛吃货群299246743</p>\n",
            // 貓版
            "<p><img alt=\"愚蠢的人类喵～\" src=\"http://static.acfun.mm111.net/h/thumb/2014-10-8/d5d3d878-aead-4610-ac70-898b4c3f592b.jpg\" style=\"float:left\" /></p>\n" +
                    "\n" +
                    "<p>&bull;愚蠢的人类喵～<br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 音乐
            "黑喂狗！<br/>本版发文间隔为15秒",
            // 考试
            "<p>\n" +
                    "\t•备考还上什么A岛！<br/>\n" +
                    "\t•欢迎各路学霸考王互触<br/>\n" +
                    "\t•可以询问备考问题、日西考试成绩<br/>\n" +
                    "\t•本版发文间隔15秒\n" +
                    "</p>",
            // 文学
            "•书虫专用版，欢迎咬文嚼字、评文推书<br>\n" +
                    "•今天的风儿好喧嚣<br>\n" +
                    "•<s>那边超市的薯片半价啦！</s><br>\n" +
                    "•本版发文间隔15秒。",
            // 二次创作
            "<p><a href=\"http://wiki.acfun.tv/index.php/%C9%B3%B0%FC\" target=\"_blank\"><img alt=\"你们都是触！\" src=\"http://static.acfun.mm111.net/h/upload2/images/2013-09-04/4edea57d-5dc3-4543-ae63-9898eca33c45.gif\" style=\"float:left; height:200px; width:240px\" /></a><br />\n" +
                    "&bull;你们都是触！<br />\n" +
                    "&bull;欢迎各位深海巨触前来吃沙包、切磋画技，屠新手村请谨慎。<br />\n" +
                    "&bull;<a href=\"http://drawdevil.com/acfun\" target=\"_blank\">在线画室</a>欢迎互触。<br />\n" +
                    "&bull;可发布同人作品宣传、推广，漫展摊位贩卖信息。<br />\n" +
                    "&bull;欢迎进行AC娘、TD娘的各种创作。好的作品可能会放在首页哦=w=<br />\n" +
                    "&bull;<strong>涂鸦⑨课群：242826209</strong><br />\n" +
                    "&bull;<a href=\"http://static.acfun.mm111.net/h/h/PlantAC1.03.swf\">种植AC娘试玩</a>|<a href=\"http://static.acfun.mm111.net/h/h/ACmario.swf\">超级AC姐妹试玩</a><br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 姐妹1
            "<p>&bull;此版默认只有妹子，请勿以男性身份和视角发串<br />\n" +
                    "&bull;请勿刷存在感、地图炮、秀恩爱<br />\n" +
                    "&bull;树洞请转<a href=\"/f/%E6%97%A5%E8%AE%B0\">日记</a>版，八卦请转<a href=\"/f/%E5%9C%88%E5%86%85\">圈内</a>版，被恶意骚扰请转<a href=\"/f/%E5%80%BC%E7%8F%AD%E5%AE%A4\">值班室</a>举报<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 女性向
            "•本版用于小说、drama、动画、游戏、名人等兴趣及作品的女性向讨论，默认以女性视角发串<br>\n" +
                    "•请和谐讨论，一串一事，请勿争吵CP，禁发H图或H文，违者一律删+封<br>\n" +
                    "•本版发文间隔为15秒。",
            // 女装
            "<p>&bull;可以讨论流行时尚，贴女装照<br />\n" +
                    "&bull;其实版块名倒着念就是&ldquo;丧尸装女&rdquo;<br />\n" +
                    "&bull;请遵守版规，擦边请自重，严禁五位数<br />\n" +
                    "&bull;<a href=\"http://www.acfun.tv/v/ac1869204\" target=\"_blank\">这就是你开女装版的原因吗！</a><br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 日记
            "<p>&bull;~记录生活点滴~<br />\n" +
                    "&bull;其实就是树洞版。<br />\n" +
                    "&bull;建设中，会加入奇怪的设定，敬请期待。<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // WIKI
            "•本版发文间隔为15秒。<br />\n" +
                    "•本版用于讨论WIKI（<a href=\"http://wiki.acfun.tv/\" target=\"_blank\">http://wiki.acfun.tv/</a>）相关，欢迎在此提出想要创建的条目。（这里不是开新版的请求区！）<br />\n" +
                    "•请先搜索WIKI以确定条目尚无人编辑。<br />\n" +
                    "•除了条目名称之外，请提供更多的内容以方便编写者编辑，例如某名词、黑话、成句何时何地因何出现，用法为何，影响为何。<br />\n" +
                    "•有特色的视频/文章/合辑系列也可在此提出，请提供特色简介和相应的链接。<br />\n" +
                    "•与WIKI无关内容将会被删除。",
            // 都市怪谈
            "<p><img src=\"http://static.acfun.mm111.net/h/image/2014-8-26/7f36127a-e7dc-406e-bd1d-47dc489625ac.gif\" /><br />\n" +
                    "&bull;请注意不要张贴恶心图片<br />\n" +
                    "&bull;当感到不适时请立即离开<br />\n" +
                    "&bull;本版发文间隔???秒</p>\n",
            // 动画
            "<p><img src=\"http://hacfun-tv.n1.yun.tf:8999/Public/Upload/thumb/image/2015-6-21/7407834f-17d2-4ab8-a31f-b5205e201bcb.jpg\" /><br />\n" +
                    "&bull;我说你们这些银啊，知不知道王老爷子说的：&ldquo;安心做个萌豚，岂不美哉？&rdquo;<br />\n" +
                    "&bull;<a href=\"http://acgdb.com/anime201507\" target=\"blank\">每日放送</a><br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 漫画
            "<p><img src=\"http://static.acfun.mm111.net/h/upload2/images/2014-01-20/a1e3fd04-c0b9-45fb-b270-5840c571344a.jpg\" style=\"float:left\" /></p>\n" +
                    "\n" +
                    "<p>&bull;日系漫画专版，美漫请戳&rarr;<a href=\"/f/%E7%BE%8E%E6%BC%AB\">美漫</a><br />\n" +
                    "&bull;只贴图没正文的一律SAGE。<br />\n" +
                    "&bull;本版禁讨论18X漫画，被发现一律禁24小时。<br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 国漫
            "<p>&bull;国产漫画、动画相关可在此发串，欢迎挖掘潜力内容<br />\n" +
                    "&bull;此行业仍在艰难发展当中，请勿苛求和非难<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 美漫
            "<p><img alt=\"保安！保安呢？\" src=\"http://static.acfun.mm111.net/h/image/2014-10-10/73c5fb58-cad5-481b-970b-9e71a75295d1.jpg\" style=\"float:left\" /></p>\n" +
                    "\n" +
                    "<p>&bull;买~买~买类头泼尼| &omega;・&acute;)<br />\n" +
                    "&bull;美式漫画综合讨论版，包括美式卡通<br />\n" +
                    "&bull;日漫请戳&rarr;<a href=\"/f/%E6%BC%AB%E7%94%BB\">漫画</a><br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 轻小说
            "•本版以日系轻小说类为主，网文也可讨论。<br />\n" +
                    "•连载禁止，讨论为主。<br />\n" +
                    "•小说连载→<a href=\"/f/%E5%B0%8F%E8%AF%B4\">小说</a><br />\n" +
                    "•非小说类→<a href=\"/f/%E6%96%87%E5%AD%A6\">文学</a><br>\n" +
                    "•本版发文间隔15秒",
            // 小说
            "•欢迎光临小说版，这里是文字描绘的缤纷世界！<br />\n" +
                    "•小说推荐、剧情讨论、原创小说均可在此发串。<br />\n" +
                    "•长篇连载允许，欢迎各路文豪。<br />\n" +
                    "•H文禁止，擦边球自重，一定要发的话请张贴站外链接而不是直接发文。<br />\n" +
                    "•本版发文间隔为15秒。<br />",
            // GALGAME
            "<img title=\"大家一起来推黄油\" src=\"http://hacfun.tv/Public/Upload/thumb/2015-08-19/55d4437e66e9b.jpg\" height=132px width=200px style=\"float:left\" />•只允许发大网盘链接，如百度云、360云盘、迅雷快传、115之类，带广告跳转或种子（磁力）链接一律删+封<br>\n" +
                    "•请遵守封面总版规，尤其禁止发H图或H文，违者一律删+封<br>\n" +
                    "•黄油⑨课：304166656<br>\n" +
                    "•本版发文间隔15秒",
            // VOCALOID
            "•本版发文间隔为15秒。",
            // 东方Project
            "•本版发文间隔为15秒。",
            // 舰娘
            "<p>&bull;舰C和舰N都可以讨论，请在发串时说明<br>&bull;本版发文间隔15秒</p>\n",
            // LoveLive
            "<p>&bull;Niconiconi~<br>&bull;本版发文间隔15秒</p>\n",
            // 游戏
            "•本版块为综合版块，不要大量发布相同游戏的串，请以跟串为主！<br />\n" +
                    "•本版发文间隔为15秒。<br />\n" +
                    "•舰队代理 <a target=\"_blank\" href=\"http://www.acggate.net/\">http://www.acggate.net/</a>",
            // EVE
            "<p>\n" +
                    "\t•这宇宙，属于你！这一刻，你占尽先机~ <br />\n" +
                    "•A岛专用EVE频道：绅士们的爱<br />\n" +
                    "•常用链接：<a href=\"http://killboard.nl/cn/?a=home\" target=\"_blank\">KillBoard</a>，<a href=\"http://www.ceve-market.org/home/\" target=\"_blank\">国服市场</a>，<a href=\"http://eve.sgfans.org/navigator/jumpLayout\" target=\"_blank\">旗舰导航</a>，<a href=\"http://tools.ceve-market.org/\" target=\"_blank\">斥候工具</a>，<a href=\"http://tieba.baidu.com/f?kw=eve\" target=\"_blank\">EVE贴吧</a>，<a href=\"http://bbs.eve-china.com/forum-140-1.html\" target=\"_blank\">ECF军团区</a> <br />\n" +
                    "•本版发文间隔15秒\n" +
                    "</p>",
            // DNF
            "<p>&bull;本版发文间隔15秒</p>\n",
            // 战争雷霆
            "•游戏和历史分开看待<br />\n" +
                    "•不讨论账号买卖<br />\n" +
                    "•严禁挑起WT和WOT的矛盾<br />\n" +
                    "•AcFun战争雷霆群号：235738751<br />\n" +
                    "•本版发文间隔15秒",
            // LOL
            "•DOTA和LOL版块拆分，相关讨论请移步专版，跨版引战一律锁IP<br/>\n" +
                    "•本版发文间隔为15秒。",
            // DOTA
            "<p>&bull;DOTA和LOL版块拆分，相关讨论请移步专版，跨版引战一律锁IP<br />\n" +
                    "&bull;DOTA⑨课群：366844448<br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // GTA5
            "<p>&bull;来联机横行霸道吧！<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // Minecraft
            "<p>Minecraft游戏的讨论。<br />\n" +
                    "更多详情请点击 <a href=\"http://mc.acfun.tv\" target=\"_blank\">mc.acfun.tv</a><br />\n" +
                    "您也许需要最新版本<br />\n" +
                    "请仔细阅读说明文件</p>\n" +
                    "\n" +
                    "<p>请勿破坏游戏秩序，妨碍别的玩家正常游戏。请多种树，多养羊，少生孩子多养猪</p>\n" +
                    "\n" +
                    "<p>&nbsp;</p>\n",
            // MUG
            "music game音乐游戏专版<br/>本版发文间隔为15秒",
            // WOT
            "<p align=\"center\">\n" +
                    "\t<img src=\"http://static.acfun.mm111.net/h/upload/fa972954-6b20-4896-bd66-da255d206b31.png\" width=\"280\" height=\"76\" title=\"We didn't penetrate their armor!\" align=\"left\" alt=\"跳蛋！\" /> \n" +
                    "</p>\n" +
                    "<p>\n" +
                    "\t•本版发文间隔15秒<br />\n" +
                    "•坦克⑨课群：61987268\n" +
                    "</p>",
            // WOW
            "•本版发文间隔15秒",
            // D3
            "•本版发文间隔为15秒。",
            // 卡牌桌游
            "•桌游不再开版，请一律在此讨论。<br />\n" +
                    "•本版发文间隔15秒。",
            // 炉石传说
            "•本版发文间隔15秒",
            // 怪物猎人
            "•本版发文间隔为15秒。",
            // 口袋妖怪
            "•本版发文间隔为15秒。",
            // AC大逃杀
            "<p>&bull;本版发文间隔为15秒。<br />\n" +
                    "&bull;本版用于AcFun大逃杀（http://0.76573.org）相关讨论。<br />\n" +
                    "&bull;0F管理员已经选出，祝他们好运！</p>\n" +
                    "\n" +
                    "<p>&bull;大逃杀官方玩家群：248504962</p>\n" +
                    "\n" +
                    "<p>&bull;大逃杀玩家吹水群：101336468</p>\n" +
                    "\n" +
                    "<p>&bull;常磐大逃杀项目源码：https://code.google.com/p/phpbr/</p>\n" +
                    "\n" +
                    "<p>&bull;对0F或者1F任何管理员的行动有任何疑问者，请直接通过上面的玩家群或者官方论坛联系我。</p>\n" +
                    "\n" +
                    "<p>&bull;对电波或者篝火等衍生服务器任何管理员的行动有任何疑问者，也请到玩家群中去找对应的负责人，谢谢。</p>\n",
            // 索尼
            "<img src=\"http://static.acfun.mm111.net/h/upload/th/ec8c9093-44da-482b-ba51-b4d4b75d38df.jpg\"><br />•本版发文间隔为15秒。<br/>",
            // 任天堂
            "•本版发文间隔为15秒。",
            // 日麻
            "<p>天凤&amp;雀龙门</p>\n",
            // AKB
            "•本版发文间隔为15秒。",
            // SNH48
            "生男孩48",
            // COSPLAY
            "<p><a href=\"/t/640819\"><img alt=\"咪~\" src=\"http://static.acfun.mm111.net/h/upload/th/af4057b8-67ea-445f-8b18-f05e22fd9a40.jpg\" style=\"float:left\" /></a></p>\n" +
                    "\n" +
                    "<p>&bull;贴图请使用回应模式，不要每一张图单独开串。<br />\n" +
                    "&bull;跟风、恶意顶回首页、挖坟一律SAGE。<br />\n" +
                    "&bull;非COS摄影请发<a href=\"/f/%E6%91%84%E5%BD%B1\">摄影</a>版。<br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 声优
            "•声优版不是用来恶搞的！请合理讨论！否则锁IP<br />\n" +
                    "•禁止PO有关兵库北的串（转恶搞）<br />\n" +
                    "•本版发文间隔15秒",
            // 模型
            "<p>&bull;模型⑨课群：284047581<br />\n" +
                    "&bull;本版发文间隔为15秒。</p>\n",
            // 影视
            "欢迎讨论各类电影和电视节目<br/>禁止发布于询问AV相关内容，违者封+删串<br/>本版发文间隔为15秒。",
            // 摄影
            "<p>&bull;欢迎交流摄影技巧、摄影作品、日西装备等<br />\n" +
                    "&bull;非原创作品请说明出处<br />\n" +
                    "&bull;系列作品请点回应在一个串内发表，多开刷版砍串<br />\n" +
                    "&bull;COS照请发<a href=\"/f/cosplay\">眼科</a><br />\n" +
                    "&bull;食物照请发<a href=\"/f/%E6%96%99%E7%90%86\">料理</a><br />\n" +
                    "&bull;谁说盗摄不是摄影！<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 体育
            "有好的身躯才能找到好基友，好好锻炼吧！<br/>本版发文间隔为15秒",
            // 军武
            "•禁止涉及任何政治话题<br/>•请保持理性讨论<br/>•违反上述版规一律砍掉+封IP。<br/>•本版发文间隔为15秒。",
            // 数码
            "•移动数码,pc数码,app分享,手机技巧.<br />\n" +
                    "•本版发文间隔15秒",
            // 天台
            "<p><img src=\"http://cdn.ovear.info:8999/image/2015-6-21/7b1ce966-4c16-46e4-bdf5-f72c98f22bca.jpg\" /><br />\n" +
                    "&bull;天台风很险，上楼需谨慎。<br />\n" +
                    "&bull;原世界杯版，其实就是赌球/炒股版。<br />\n" +
                    "&bull;干脆就叫天台版算了&hellip;&hellip;<br />\n" +
                    "&bull;可以求荐股，风险自理，但严禁发布或求QQ/微信群<br />\n" +
                    "&bull;本版发文间隔15秒</p>\n",
            // 值班室
            "<p>&bull;本版发文间隔为15秒。<br />\n" +
                    "&bull;请在此举报不良内容，并附上串地址以及发言者ID。如果是回复串，请附上&ldquo;回应&rdquo;链接的地址，格式为&gt;&gt;No.串ID或&gt;&gt;No.回复ID<br />\n" +
                    "&bull;主站相关问题反馈、建议请在这里留言<br />\n" +
                    "&bull;已处理的举报将锁定。</p>\n"
    };

    private static DaoSession sDaoSession;

    public static class DBOpenHelper extends DaoMaster.OpenHelper {

        private boolean mCreate;
        private boolean mUpgrade;
        private int mOldVersion;
        private int mNewVersion;

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            super.onCreate(db);
            mCreate = true;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mUpgrade = true;
            mOldVersion = oldVersion;
            mNewVersion = newVersion;

            switch (oldVersion) {
                case 1:
                    ACRecordDao.createTable(db, true);
                case 2:
                    ACCommonPostDao.createTable(db, true);
                case 3:
                    db.execSQL("ALTER TABLE '" + ACForumDao.TABLENAME + "' ADD COLUMN '" +
                            ACForumDao.Properties.Msg.columnName + "' TEXT");
            }
        }

        public boolean isCreate() {
            return mCreate;
        }

        public void clearCreate() {
            mCreate = false;
        }

        public boolean isUpgrade() {
            return mUpgrade;
        }

        public void clearUpgrade() {
            mUpgrade = false;
        }

        public int getOldVersion() {
            return mOldVersion;
        }
    }

    public static void initialize(Context context) {
        DBOpenHelper helper = new DBOpenHelper(
                context.getApplicationContext(), "nimingban", null);

        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);

        sDaoSession = daoMaster.newSession();

        if (helper.isCreate()) {
            helper.clearCreate();

            insertDefaultACForums();
            insertDefaultACCommonPosts();
        }

        if (helper.isUpgrade()) {
            helper.clearUpgrade();

            switch (helper.getOldVersion()) {
                case 1:
                case 2:
                    insertDefaultACCommonPosts();
                case 3:
                    addACForumMsg();
            }
        }
    }

    private static void insertDefaultACForums() {
        ACForumDao dao = sDaoSession.getACForumDao();
        dao.deleteAll();

        int size = 59;
        String[] names = {"综合版1", "欢乐恶搞", "推理", "技术宅", "料理", "貓版", "音乐", "考试", "文学",
                "二次创作", "姐妹1", "女性向", "女装", "日记", "WIKI", "都市怪谈", "动画", "漫画", "国漫",
                "美漫", "轻小说", "小说", "GALGAME", "VOCALOID", "东方Project", "舰娘", "LoveLive",
                "游戏", "EVE", "DNF", "战争雷霆", "LOL", "DOTA", "GTA5", "Minecraft", "MUG", "WOT",
                "WOW", "D3", "卡牌桌游", "炉石传说", "怪物猎人", "口袋妖怪", "AC大逃杀", "索尼", "任天堂",
                "日麻", "AKB", "SNH48", "COSPLAY", "声优", "模型", "影视", "摄影", "体育", "军武",
                "数码", "天台", "值班室"};

        AssertUtils.assertEquals("AC_FORUM_ID_ARRAY.size must be size", size, AC_FORUM_ID_ARRAY.length);
        AssertUtils.assertEquals("names.size must be size", size, names.length);
        AssertUtils.assertEquals("AC_FORUM_MSG_ARRAY.size must be size", size, AC_FORUM_MSG_ARRAY.length);

        for (int i = 0; i < size; i++) {
            ACForumRaw raw = new ACForumRaw();
            raw.setPriority(i);
            raw.setForumid(AC_FORUM_ID_ARRAY[i]);
            raw.setDisplayname(names[i]);
            raw.setVisibility(true);
            raw.setMsg(AC_FORUM_MSG_ARRAY[i]);
            dao.insert(raw);
        }
    }

    private static void insertDefaultACCommonPosts() {
        ACCommonPostDao dao = sDaoSession.getACCommonPostDao();
        dao.deleteAll();

        int size = 13;
        String[] names = {
                "人，是会思考的芦苇", "丧尸图鉴", "壁纸楼", "足控福利", "淡定红茶",
                "胸器福利", "黑妹", "总有一天", "这是芦苇", "赵日天",
                "二次元女友", "什么鬼", "Banner画廊"};
        String[] ids = {
                "6064422", "585784", "117617", "103123", "114373",
                "234446", "55255", "328934", "49607", "1738904",
                "553505", "5739391", "6538597"};
        AssertUtils.assertEquals("ids.size must be size", size, ids.length);
        AssertUtils.assertEquals("names.size must be size", size, names.length);

        for (int i = 0; i < size; i++) {
            ACCommonPostRaw raw = new ACCommonPostRaw();
            raw.setName(names[i]);
            raw.setPostid(ids[i]);
            dao.insert(raw);
        }
    }

    private static void addACForumMsg() {
        ACForumDao dao = sDaoSession.getACForumDao();
        List<ACForumRaw> list = dao.queryBuilder().list();
        for (ACForumRaw raw : list) {
            int index = Arrays2.indexOf(AC_FORUM_ID_ARRAY, raw.getForumid());
            if (index != -1) {
                raw.setMsg(AC_FORUM_MSG_ARRAY[index]);
            }
        }
        dao.updateInTx(list);
    }

    public static List<DisplayForum> getACForums(boolean onlyVisible) {
        ACForumDao dao = sDaoSession.getACForumDao();
        List<ACForumRaw> list = dao.queryBuilder().orderAsc(ACForumDao.Properties.Priority).list();
        List<DisplayForum> result = new ArrayList<>();
        for (ACForumRaw raw : list) {
            if (onlyVisible && !raw.getVisibility()) {
                continue;
            }

            DisplayForum dForum = new DisplayForum();
            dForum.site = ACSite.getInstance();
            dForum.id = raw.getForumid();
            dForum.displayname = raw.getDisplayname();
            dForum.priority = raw.getPriority();
            dForum.visibility = raw.getVisibility();
            dForum.msg = raw.getMsg();
            result.add(dForum);
        }

        return result;
    }

    public static void setACForums(List<ACForum> list) {
        ACForumDao dao = sDaoSession.getACForumDao();
        dao.deleteAll();

        int i = 0;
        List<ACForumRaw> insertList = new ArrayList<>();
        for (ACForum forum : list) {
            ACForumRaw raw = new ACForumRaw();
            raw.setDisplayname(forum.name);
            raw.setForumid(forum.id);
            raw.setPriority(i);
            raw.setVisibility(true);
            raw.setMsg(forum.msg);
            insertList.add(raw);
            i++;
        }

        dao.insertInTx(insertList);
    }

    public static LazyList<ACForumRaw> getACForumLazyList() {
        return sDaoSession.getACForumDao().queryBuilder().orderAsc(ACForumDao.Properties.Priority).listLazy();
    }

    public static void setACForumVisibility(ACForumRaw raw, boolean visibility) {
        raw.setVisibility(visibility);
        sDaoSession.getACForumDao().update(raw);
    }

    public static void updateACForum(Iterable<ACForumRaw> entities) {
        sDaoSession.getACForumDao().updateInTx(entities);
    }

    public static List<DisplayForum> getForums(int site, boolean onlyVisible) {
        // TODO
        return null;
    }

    public static LazyList<DraftRaw> getDraftLazyList() {
        return sDaoSession.getDraftDao().queryBuilder().orderDesc(DraftDao.Properties.Time).listLazy();
    }

    public static void addDraft(String content) {
        addDraft(content, -1);
    }

    public static void addDraft(String content, long time) {
        DraftRaw raw = new DraftRaw();
        raw.setContent(content);
        raw.setTime(time == -1 ? System.currentTimeMillis() : time);
        sDaoSession.getDraftDao().insert(raw);
    }

    public static void removeDraft(long id) {
        sDaoSession.getDraftDao().deleteByKey(id);
    }

    public static final int AC_RECORD_POST = 0;
    public static final int AC_RECORD_REPLY = 1;

    public static LazyList<ACRecordRaw> getACRecordLazyList() {
        return sDaoSession.getACRecordDao().queryBuilder().orderDesc(ACRecordDao.Properties.Time).listLazy();
    }

    public static void addACRecord(int type, String recordid, String postid, String content, String image) {
        addACRecord(type, recordid, postid, content, image, -1);
    }

    public static void addACRecord(int type, String recordid, String postid, String content, String image, long time) {
        ACRecordRaw raw = new ACRecordRaw();
        raw.setType(type);
        raw.setRecordid(recordid);
        raw.setPostid(postid);
        raw.setContent(content);
        raw.setImage(image);
        raw.setTime(time == -1 ? System.currentTimeMillis() : time);
        sDaoSession.getACRecordDao().insert(raw);
    }

    public static void removeACRecord(ACRecordRaw raw) {
        sDaoSession.getACRecordDao().delete(raw);
    }

    public static List<CommonPost> getAllACCommentPost() {
        ACCommonPostDao dao = sDaoSession.getACCommonPostDao();
        List<ACCommonPostRaw> list = dao.queryBuilder().orderAsc(ACCommonPostDao.Properties.Id).list();
        List<CommonPost> result = new ArrayList<>();
        for (ACCommonPostRaw raw : list) {
            CommonPost cp = new CommonPost();
            cp.name = raw.getName();
            cp.id = raw.getPostid();
            result.add(cp);
        }
        return result;
    }

    public static void setACCommonPost(List<CommonPost> list) {
        ACCommonPostDao dao = sDaoSession.getACCommonPostDao();
        dao.deleteAll();

        List<ACCommonPostRaw> insertList = new ArrayList<>();
        for (CommonPost cp : list) {
            ACCommonPostRaw raw = new ACCommonPostRaw();
            raw.setName(cp.name);
            raw.setPostid(cp.id);
            insertList.add(raw);
        }

        dao.insertInTx(insertList);
    }
}
