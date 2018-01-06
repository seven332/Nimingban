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
import com.hippo.yorozuya.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;

public final class DB {

    private static String[] AC_FORUM_ID_ARRAY = {"-1", "4", "20", "11", "30", "32", "40", "35", "56", "110", "15", "19", "106", "17", "98", "102", "75", "97", "89", "96", "81", "14", "12", "90", "99", "87", "64", "5", "93", "101", "6", "103", "2", "109", "72", "80", "3", "25", "107", "24", "22", "70", "44", "38", "86", "51", "10", "28", "108", "23", "45", "34", "29", "16", "100", "13", "55", "39", "31", "37", "33", "18", };
    private static String[] AC_FORUM_NAME_ARRAY = {"\u65F6\u95F4\u7EBF", "\u7EFC\u5408\u72481", "\u6B22\u4E50\u6076\u641E", "\u63A8\u7406", "\u6280\u672F\u5B85", "\u6599\u7406", "\u8C93\u7248", "\u97F3\u4E50", "\u8003\u8BD5", "\u793E\u755C", "\u79D1\u5B66", "\u5C0F\u8BF4", "\u4E70\u4E70\u4E70", "\u4E8C\u6B21\u521B\u4F5C", "\u59D0\u59B91", "LGBT", "\u6570\u7801", "\u5973\u88C5", "\u65E5\u8BB0", "\u5708\u5185", "\u90FD\u5E02\u602A\u8C08", "\u52A8\u753B", "\u6F2B\u753B", "\u7F8E\u6F2B", "\u56FD\u6F2B", "\u8F7B\u5C0F\u8BF4", "GALGAME", "\u4E1C\u65B9Project", "\u8230\u5A18", "LoveLive", "VOCALOID", "\u6587\u5B66", "\u6E38\u620F", "\u5B88\u671B\u5148\u950B", "DNF", "\u7089\u77F3\u4F20\u8BF4", "\u624B\u6E38", "\u4EFB\u5929\u5802", "Steam", "\u7D22\u5C3C", "LOL", "DOTA", "WOW", "\u53E3\u888B\u5996\u602A", "\u6218\u4E89\u96F7\u9706", "WOT", "Minecraft", "\u602A\u7269\u730E\u4EBA", "\u8F90\u5C04", "D3", "\u5361\u724C\u684C\u6E38", "MUG", "AC\u5927\u9003\u6740", "AKB", "SNH48", "COSPLAY", "\u58F0\u4F18", "\u6A21\u578B", "\u5F71\u89C6", "\u519B\u6B66", "\u4F53\u80B2", "\u503C\u73ED\u5BA4", };
    private static String[] AC_FORUM_MSG_ARRAY = {"\u8FD9\u91CC\u662F\u533F\u540D\u7248\u6700\u65B0\u7684\u4E32", "<p><a href=\"https://h.nimingban.com/t/11111245\" target=\"_blank\"><img alt=\"\u4E3E\u9AD8\u9AD8\uFF01\" src=\"http://cover.acfunwiki.org/face.php\" style=\"float:left\" /></a></p>\r\n\r\n<p>&bull;\u6B22\u8FCE\u5149\u4E34\uFF0C\u6709\u5173A\u5C9B\u7684\u4ECB\u7ECD\u8BF7\u70B9&rarr;<a href=\"http://sx1.atv.ac:5566/index.php/%C4%E4%C3%FB%CC%D6%C2%DB%B0%E6\" target=\"_blank\">\u8FD9\u91CC</a><br />\r\n<strong>&bull;<a href=\"https://h.nimingban.com/Forum\">\u5168\u5C9B\u603B\u7248\u89C4</a>\u8BF7\u4ED4\u7EC6\u9605\u8BFB\uFF0C\u8FDD\u8005\u89C6\u89C4\u5219\u5220\u5C01\u780D</strong><br />\r\n&bull;\u8BF7\u6587\u660E\u8BA8\u8BBA\uFF0C\u7EFC\u5408\u4E00\u4E0D\u662F<strong>\u5783\u573E\u573A</strong>\uFF0C\u65E0\u516C\u5171\u8BA8\u8BBA\u610F\u4E49/\u6709\u788D\u516C\u4F17\u6D4F\u89C8\u7684\u5185\u5BB9\u8BF7\u53BB<a href=\"https://h.nimingban.com/f/%E6%97%A5%E8%AE%B0\" target=\"_blank\">\u6811\u6D1E\u7248</a>\uFF0C\u5426\u5219\u53EF\u80FD\u88ABSAGE<br />\r\n&bull;<a href=\"http://sx1.atv.ac:5566/index.php/SAGE\" target=\"_blank\">SAGE</a>\u5E76\u4E0D\u610F\u5473\u7740\u5C01\u7981\uFF0C\u4F60\u53EF\u4EE5\u7EE7\u7EED\u5728\u4E32\u91CC/\u5DE6\u4FA7\u5BFC\u822A\u91CC\u5BFB\u627E\u8BDD\u9898\u5BF9\u5E94\u5B50\u7248\u8FDB\u884C\u8BA8\u8BBA</a><br />\r\n&bull;\u4E3B\u7AD9\u5BA2\u670DQQ\uFF1A800055564 \u5BA3\u4F20QQ\u7FA4&rarr;<a href=\"http://sx1.atv.ac:5566/index.php/QQ\" target=\"_blank\">QQ\u7FA4\u7D22\u5F15</a><br />\r\n&bull;<strong>\u4E27\u5C38\u4EEC<a href=\"http://cdn.aixifan.com/h/mp3/tnnaii-h-island-c.mp3\">\u5FEB\u6765\u98DF\u6211\u5927\u96D5\u5566</a>~</strong><br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A30\u79D2\u3002</p>\r\n", "<img src=\"http://cdn.aixifan.com/h/upload2/images/2014-06-05/aa2a8b7f-5071-48a7-b068-fecee12d3fd9.gif\" style=\"float:left\"> &bull;H\u7269\u6C42\u79CD\u6C42\u7EA2\u9886\u5DFE\uFF0C\u780D\u697C+\u81F3\u5C1124\u5C0F\u65F6\u5C01\u7981\u3002\u8FD9\u91CC\u4E0D\u662FH\u7F51<br />\r\n&bull;\u7981\u6B62\u5F20\u8D34\u5BC6\u96C6\u56FE\u3001\u6076\u5FC3\u56FE\uFF0C\u8FDD\u8005\u4E00\u5F8B\u780D\u4E32\u5C01IP\u3002<br />\r\n&bull;\u5F15\u6218\u5403SAGE\uFF0C\u4EBA\u8EAB\u653B\u51FB\u548C\u8FB1\u9A82\u6587\u5B57\u4E00\u5F8B\u780D\uFF0C\u5176\u4F59\u540C\u603B\u7248\u89C4\u3002<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002\r\n", "\u2022\u8111\u6D1E\u3001\u5FAE\u5C0F\u8BF4\u3001\u56FE\u7247\u63A8\u7406\u3001\u89E3\u8C1C\u3002<br/>\u2022\u5B66\u672F\u8BA8\u8BBA\u8BF7\u8F6C<a href=\"/f/\u7406\u5B66\">\u7406\u5B66\u7248</a><br>\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u2022\u7A0B\u5E8F\u8BED\u8A00\u3001\u538B\u5236\u6295\u7A3F\u3001\u89C6\u9891\u5236\u4F5C\u4EE5\u53CA\u5404\u8BA1\u7B97\u673A\u9886\u57DF\u7684\u6280\u672F\u95EE\u9898<br />\r\n\u2022<strong style='color:red'>\u7535\u8111\u7EC4\u88C5\u53CA\u4EA7\u54C1\u7B49\u95EE\u9898\u8BF7\u79FB\u6B65<a href=\"/f/\u6570\u7801\">\u6570\u7801</a>\uFF0C\u5426\u5219\u5220</strong><br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "<p><img alt=\"\" src=\"http://cdn.aixifan.com/h/image/20147/33ed6df166ff1b18d040c24c309d7546.png\" style=\"float:left\" /></p>\r\n\r\n<p>&nbsp;</p>\r\n\r\n<p>\u4F60\u4EEC\u90FD\u662F\u5403\u8D27\uFF01\u6C6A\uFF01</p>\r\n\r\n<p>\u4ECA\u5929<a href=\"/t/283078\">\u5F00\u5FC3</a>\u8FD8\u662F<a href=\"/t/2950161\">\u4E0D\u5F00\u5FC3</a>\u5462\uFF1F</p>\r\n\r\n<p>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2</p>\r\n", "<p><img alt=\"\u611A\u8822\u7684\u4EBA\u7C7B\u55B5\uFF5E\" src=\"http://cdn.aixifan.com/h/thumb/2014-10-8/d5d3d878-aead-4610-ac70-898b4c3f592b.jpg\" style=\"float:left\" /></p>\r\n\r\n<p>&bull;\u611A\u8822\u7684\u4EBA\u7C7B\u55B5\uFF5E<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "\u9ED1\u5582\u72D7\uFF01<br/>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2", "<p>\r\n\t\u2022\u5907\u8003\u8FD8\u4E0A\u4EC0\u4E48A\u5C9B\uFF01<br/>\r\n\t\u2022\u6B22\u8FCE\u5404\u8DEF\u5B66\u9738\u8003\u738B\u4E92\u89E6<br/>\r\n\t\u2022\u53EF\u4EE5\u8BE2\u95EE\u5907\u8003\u95EE\u9898\u3001\u65E5\u897F\u8003\u8BD5\u6210\u7EE9<br/>\r\n\t\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2\r\n</p>", "<img src=\"http://nmbimg.fastmirror.org/thumb/2017-02-24/58af8fab15ecc.jpg\"><br>\u53EF\u4EE5\u8BA8\u8BBA\u5DE5\u4F5C\u3001\u6295\u8D44\u3001\u8F66\u623F\u3001\u80B2\u513F\u3001\u517B\u8001\u76F8\u5173\uFF0C\u8BF7\u9075\u5B88<a href=\"/\">\u603B\u7248\u89C4</a><br>\r\n\u5B66\u4E1A\u3001\u8003\u8BD5\u76F8\u5173\u8BF7\u5230<a href=\"/f/%E8%80%83%E8%AF%95\">\u6821\u56ED\u7248</a><br>\u5728\u533F\u540D\u7248\u5F20\u8D34\u771F\u5B9E\u4FE1\u606F\u65F6\u8BF7<b>\u8C28\u614E\u8003\u8651</b>\uFF0C\u672C\u7AD9\u4E0D\u5BF9\u4E2A\u4EBA\u4FE1\u606F\u6CC4\u9732\u5F15\u8D77\u7684\u540E\u679C\u8D1F\u8D23<br>\r\n\u6295\u8D44\u98CE\u9669\u4E0E\u56DE\u62A5\u5BF9\u7B49\uFF0C\u8BF7\u8C28\u614E\u9009\u62E9\u9AD8\u56DE\u62A5\u7387\u6295\u8D44\u65B9\u5F0F\uFF0C\u672C\u7248\u7981\u6B62\u9F13\u5439\u9AD8\u98CE\u9669\u6295\u8D44\u9879\u76EE\uFF0C\u7C7B\u4F3C\u5185\u5BB9\u5C06\u89C6\u4E3A\u5546\u4E1A\u5E7F\u544A\u5220\u9664\u5904\u7406", "\u6DB5\u76D6\u5404\u7C7B\u79D1\u5B66\u7684\u8BA8\u8BBA\u677F\u5757<br>\r\n\u6B22\u8FCE\u8C23\u8A00\u7C89\u788E\u673A\uFF0C\u5927\u4F17\u79D1\u666E\uFF0C\u5C16\u7AEF\u524D\u6CBF\uFF0C\u5B66\u672F\u8FA9\u8BBA<br>\r\n\u5E72\u8D27\u4EC0\u4E48\u7684\u6700\u559C\u6B22\u4E86\uFF01<br>\r\n\u5F15\u7528\u8BF7\u6CE8\u660E\u51FA\u5904\uFF0C\u6C11\u79D1\u3001\u4F2A\u79D1\u5B66\u9000\u6563<br>", "\u2022\u6B22\u8FCE\u5149\u4E34\u5C0F\u8BF4\u7248\uFF0C\u8FD9\u91CC\u662F\u6587\u5B57\u63CF\u7ED8\u7684\u7F24\u7EB7\u4E16\u754C\uFF01<br />\r\n\u2022\u5C0F\u8BF4\u63A8\u8350\u3001\u5267\u60C5\u8BA8\u8BBA\u3001\u539F\u521B\u5C0F\u8BF4\u5747\u53EF\u5728\u6B64\u53D1\u4E32\u3002<br />\r\n\u2022\u957F\u7BC7\u8FDE\u8F7D\u5141\u8BB8\uFF0C\u6B22\u8FCE\u5404\u8DEF\u6587\u8C6A\u3002<br />\r\n\u2022H\u6587\u7981\u6B62\uFF0C\u64E6\u8FB9\u7403\u81EA\u91CD\uFF0C\u4E00\u5B9A\u8981\u53D1\u7684\u8BDD\u8BF7\u5F20\u8D34\u7AD9\u5916\u94FE\u63A5\u800C\u4E0D\u662F\u76F4\u63A5\u53D1\u6587\u3002<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002<br />", "<img title=\"\u4E70\u4E70\u4E70\uFF01\u4E0D\u4E70\u8FD8\u662F\u4EBA\uFF1F\u8D76\u7D27\u5241\u624B\uFF01\" src=\"http://nmbimg.fastmirror.org/thumb/2015-11-02/56374260dac57.jpg\" height=132px width=200px style=\"float:left\" />\u2022\u5241\u624B\u4F18\u60E0\u4FE1\u606F\u3001\u503C\u5F97\u4E70\u7684\u5B9D\u8D1D\u5728\u6B64\u7248\u96C6\u4E2D\u4EA4\u6D41\uFF0C\u4E8C\u624B\u8BF7\u8D70\u95F2\u9C7C<br />\u2022\u672C\u7248\u539F\u5219\uFF1A\u53EA\u5206\u4EAB\u597D\u7269\uFF0C\u4E0D\u505A\u4F38\u624B\u515A\uFF0C\u88C5\u673A\u6C42\u52A9\u8BF7\u8F6C<a href=\"/f/%E6%95%B0%E7%A0%81\">\u6570\u7801</a>\u7248<br />\u2022\u5141\u8BB8\u53D1\u5546\u54C1\u94FE\u63A5\uFF0C\u53D1\u94FE\u63A5\u65F6\u8BF7\u9644\u5B9D\u8D1D\u56FE\uFF0C\u672A\u7ECF\u5141\u8BB8\u7981\u6B62\u5546\u4E1A\u63A8\u9500\u3002\u7981\u6B62\u5E7F\u544A\u5237\u7248<br>\u2022A\u5C9B\u4E32\u5185\u968F\u673A\u5E7F\u544A\u4F4D\u62DB\u5546\uFF0C\u8BE6\u60C5\u8BF7\u8054\u7CFBhelp@nimingban.com", "<p><a href=\"http://wiki.acfun.tv/index.php/%C9%B3%B0%FC\" target=\"_blank\"><img alt=\"\u4F60\u4EEC\u90FD\u662F\u89E6\uFF01\" src=\"http://cdn.aixifan.com/h/upload2/images/2013-09-04/4edea57d-5dc3-4543-ae63-9898eca33c45.gif\" style=\"float:left; height:200px; width:240px\" /></a><br />\r\n&bull;\u4F60\u4EEC\u90FD\u662F\u89E6\uFF01<br />\r\n&bull;\u6B22\u8FCE\u5404\u4F4D\u6DF1\u6D77\u5DE8\u89E6\u524D\u6765\u5403\u6C99\u5305\u3001\u5207\u78CB\u753B\u6280\uFF0C\u5C60\u65B0\u624B\u6751\u8BF7\u8C28\u614E\u3002<br />\r\n&bull;<a href=\"http://drawdevil.com/acfun\" target=\"_blank\">\u5728\u7EBF\u753B\u5BA4</a>\u6B22\u8FCE\u4E92\u89E6\u3002<br />\r\n&bull;\u53EF\u53D1\u5E03\u540C\u4EBA\u4F5C\u54C1\u5BA3\u4F20\u3001\u63A8\u5E7F\uFF0C\u6F2B\u5C55\u644A\u4F4D\u8D29\u5356\u4FE1\u606F\u3002<br />\r\n&bull;\u6B22\u8FCE\u8FDB\u884CAC\u5A18\u3001TD\u5A18\u7684\u5404\u79CD\u521B\u4F5C\u3002\u597D\u7684\u4F5C\u54C1\u53EF\u80FD\u4F1A\u653E\u5728\u9996\u9875\u54E6=w=<br />\r\n&bull;<a href=\"http://static.acfun.mm111.net/h/h/PlantAC1.03.swf\">\u79CD\u690DAC\u5A18\u8BD5\u73A9</a>|<a href=\"http://static.acfun.mm111.net/h/h/ACmario.swf\">\u8D85\u7EA7AC\u59D0\u59B9\u8BD5\u73A9</a><br />\r\n&bull;\u4E0A\u8272AI\uFF1A<a href=\"http://paintschainer.preferred.tech/\" target=\"_blank\">http://paintschainer.preferred.tech/</a>\r\n", "<p>&bull;\u6B64\u7248\u9ED8\u8BA4\u53EA\u6709\u59B9\u5B50\uFF0C\u8BF7\u52FF\u4EE5\u7537\u6027\u8EAB\u4EFD\u548C\u89C6\u89D2\u53D1\u4E32\uFF0C<a href=\"/t/12623731\">\u8BE6\u60C5\u8BF7\u89C1\u6B64\u5904</a><br />\r\n&bull;\u8BF7\u52FF\u5237\u5B58\u5728\u611F\u3001\u5730\u56FE\u70AE\u3001\u79C0\u6069\u7231<br />\r\n&bull;\u6811\u6D1E\u8BF7\u8F6C<a href=\"/f/%E6%97%A5%E8%AE%B0\">\u65E5\u8BB0</a>\u7248\uFF0C\u516B\u5366\u8BF7\u8F6C<a href=\"/f/%E5%9C%88%E5%86%85\">\u5708\u5185</a>\u7248\uFF0C\u88AB\u6076\u610F\u9A9A\u6270\u8BF7\u8F6C<a href=\"/f/%E5%80%BC%E7%8F%AD%E5%AE%A4\">\u503C\u73ED\u5BA4</a>\u4E3E\u62A5<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "\u2022\u539F\u5973\u6027\u5411\u5185\u5BB9\u53EF\u4EE5\u53D1\u9001\u81F3<\u59D0\u59B91>\u4E2D\u7684\u6307\u5B9A\u8BA8\u8BBA\u4E32\uFF0C\u8150\u5973\u5411\u7684\u5185\u5BB9\u53EF\u4EE5\u81EA\u884C\u8003\u8651\u7248\u5757<br/>\r\n\u2022\u8BF7\u548C\u8C10\u8BA8\u8BBA\uFF0C\u4E00\u4E32\u4E00\u4E8B\uFF0C\u7981\u53D1H\u56FE\u6216H\u6587\uFF0C\u4EE5\u53CA\u5BB6\u957F\u4E0D\u5B9C\u5185\u5BB9\uFF0C\u5144\u8D35\u3001\u6DEB\u68A6\u3001\u6076\u81ED<br/>\r\n\u2022\u7981\u4EA4\u914D\uFF0C\u7EA6\u70AE\uFF0C\u540C\u57CE\u4EA4\u53CB\uFF0C\u4EE5\u53CA\u4EFB\u4F55\u8FDD\u53CD\u533F\u540D\u7248\u603B\u89C4\u4EE5\u53CA\u6CD5\u5F8B\u7684\u5185\u5BB9\uFF0C\u5426\u5219\u4E00\u5F8B\u5220\uFF0C\u4E25\u91CD\u60C5\u51B5\u76F4\u63A5\u5C01<br>\r\n\u2022\u56E0\u672C\u7248\u60C5\u51B5\u7279\u6B8A\uFF0C\u539F\u5219\u4E0A\u4E0D\u652F\u6301\u8DE8\u4E32\u8BA8\u8BBA\uFF0C\u5982\u770B\u5230\u4E0D\u80FD\u7B26\u5408\u81EA\u5DF1\u4EF7\u503C\u89C2\u7684\u5185\u5BB9\uFF0C\u8BF7\u52FF\u8FDB\u5165\uFF0C\u56E0\u8FDD\u53CD\u672C\u6761\u7684\u5BFC\u81F4\u4E89\u5435\uFF0C\u4E00\u5F8B\u5220+\u5C01<br>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u2022\u79FB\u52A8\u6570\u7801,pc\u6570\u7801,app\u5206\u4EAB,\u624B\u673A\u6280\u5DE7.<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "<p><img src=\"http://nmbimg.fastmirror.org/thumb/2016-04-24/571c9c1fbd520.jpg\" height=200px width=132px style=\"float:left\" /><br />&bull;\u53EF\u4EE5\u8BA8\u8BBA\u6D41\u884C\u65F6\u5C1A\uFF0C\u8D34\u5973\u88C5\u7167<br />\r\n&bull;\u5176\u5B9E\u7248\u5757\u540D\u5012\u7740\u5FF5\u5C31\u662F&ldquo;\u4E27\u5C38\u88C5\u5973&rdquo;<br />\r\n&bull;\u8BF7\u9075\u5B88\u7248\u89C4\uFF0C\u64E6\u8FB9\u8BF7\u81EA\u91CD\uFF0C\u4E25\u7981\u4E94\u4F4D\u6570<br />\r\n&bull;<a href=\"https://h.nimingban.com/t/8939600\">\u533B\u7597\u884C\u4E3A\u8BF7\u52A1\u5FC5\u9075\u533B\u5631\uFF0C\u7981\u6B62\u529D\u8BF1\u3001\u717D\u52A8\u3001\u8BA8\u8BBA\u8D2D\u4E70\u5904\u65B9\u836F</a><br>\r\n&bull;<a href=\"http://www.acfun.tv/v/ac1869204\" target=\"_blank\">\u8FD9\u5C31\u662F\u4F60\u5F00\u5973\u88C5\u7248\u7684\u539F\u56E0\u5417\uFF01</a><br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "<p><img title=\"\u6BCF\u5929\u90FD\u6709\u65B0\u6536\u83B7\uFF01\" src=\"http://img6.nimingban.com/thumb/2016-09-18/57dd6e3f1e806.jpg\" height=237px width=250px style=\"float:left\" />&bull;~\u8BB0\u5F55\u751F\u6D3B\u70B9\u6EF4~<br />\r\n&bull;\u5176\u5B9E\u5C31\u662F\u6811\u6D1E\u7248\u3002<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "<p><img alt=\"\u4E8B\u5230\u5982\u4ECA\u2026\u2026\" src=\"http://nmbimg.fastmirror.org/thumb/2016-04-02/56ff8276c58b0.jpg\" style=\"float:left; height:200px; width:156px\" />&bull;\u4E3A\u7EF4\u62A4\u7EFC\u54081\u7684\u826F\u597D\u98CE\u6C14\uFF0C\u4E89\u8BBA\u3001\u9ED1\u8BDD\u3001\u8282\u594F\u8BAE\u9898\u8BF7\u5728\u6B64\u7248\u8BA8\u8BBA\u3002<br />&bull;\u672C\u7248\u53EF\u4EE5\u5E26\u5E72\u8D27\u6B63\u9762\u5A4A\u4EFB\u4F55\u4E00\u4E2A\u7EA2\u540D<br>\r\n&bull;\u8BF7\u52FF\u6076\u610F\u5F15\u6218\uFF0C<strong>\u4E25\u7981\u8BFD\u8C24\uFF01\u6CA1\u6709\u5E72\u8D27\u76F4\u63A5\u5220\uFF01</strong><br />\r\n&bull;\u8BF7\u52FF\u8BA8\u8BBA\u5E7F\u5DDE\u5F39\u5E55\u7F51\u7EDC\u79D1\u6280\u6709\u9650\u516C\u53F8\u5185\u90E8\u6D88\u606F\u3002<br />\r\n&bull;<span class=\"marker\"><strong>\u6B22\u8FCE<a href=\"/f/%E5%80%BC%E7%8F%AD%E5%AE%A4\" target=\"_blank\">&rarr;\u4E3E\u62A5&larr;</a>\u623E\u6C14\u8FC7\u91CD\u548C\u6076\u610F\u5E26\u8282\u594F\u7684\u997C\u5E72</strong></span><br />\r\n&bull;\u5982\u65E0\u6CD5\u8BA4\u540CA\u5C9B\u7684\u7BA1\u7406\uFF0C\u8BF7<a href=\"http://pan.baidu.com/s/1c1KYVtQ\r\n\" target=\"_blank\">\u53C2\u8003\u8FD9\u91CC</a>\u81EA\u884C\u5EFA\u5C9B\u8BC1\u660E\u81EA\u5DF1\u3002\r\n<br><a href=\"/t/7101074\">\u2192\u70B9\u6B64\u5145\u503C\u7EA2\u540D\u2190</a>\r\n<br>&bull;A\u7AD9\u4E3B\u7AD9\u76F8\u5173\u95EE\u9898\u8BF7\u54A8\u8BE2\u5BA2\u670DQQ\uFF1A800055564<br>\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "<p><img src=\"http://cdn.aixifan.com/h/image/2014-8-26/7f36127a-e7dc-406e-bd1d-47dc489625ac.gif\" /><br />\r\n&bull;\u8BF7\u6CE8\u610F\u4E0D\u8981\u5F20\u8D34\u6076\u5FC3\u56FE\u7247<br />\r\n&bull;\u5F53\u611F\u5230\u4E0D\u9002\u65F6\u8BF7\u7ACB\u5373\u79BB\u5F00<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694???\u79D2</p>\r\n", "<p><img src=\"http://nmbimg.fastmirror.org/thumb/2016-10-10/57fb02f2f1b3d.jpg\" /><br />\r\n&bull;\u6211\u8BF4\u4F60\u4EEC\u8FD9\u4E9B\u94F6\u554A\uFF0C\u77E5\u4E0D\u77E5\u9053\u738B\u8001\u7237\u5B50\u8BF4\u7684\uFF1A&ldquo;\u5B89\u5FC3\u505A\u4E2A\u840C\u8C5A\uFF0C\u5C82\u4E0D\u7F8E\u54C9\uFF1F&rdquo;<br />\r\n&bull;<a href=\"http://bgmlist.com/\" target=\"blank\">\u6BCF\u65E5\u65B0\u756A\u5217\u8868</a><br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "<p><img src=\"http://cdn.aixifan.com/h/upload2/images/2014-01-20/a1e3fd04-c0b9-45fb-b270-5840c571344a.jpg\" style=\"float:left\" /></p>\r\n\r\n<p>&bull;\u65E5\u7CFB\u6F2B\u753B\u4E13\u7248\uFF0C\u7F8E\u6F2B\u8BF7\u6233&rarr;<a href=\"/f/%E7%BE%8E%E6%BC%AB\">\u7F8E\u6F2B</a><br />\r\n&bull;\u53EA\u8D34\u56FE\u6CA1\u6B63\u6587\u7684\u4E00\u5F8BSAGE\u3002<br />\r\n&bull;\u672C\u7248\u7981\u8BA8\u8BBA18X\u6F2B\u753B\uFF0C\u88AB\u53D1\u73B0\u4E00\u5F8B\u798124\u5C0F\u65F6\u3002<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "<p><img alt=\"\u4FDD\u5B89\uFF01\u4FDD\u5B89\u5462\uFF1F\" src=\"http://cdn.aixifan.com/h/image/2014-10-10/73c5fb58-cad5-481b-970b-9e71a75295d1.jpg\" style=\"float:left\" /></p>\r\n\r\n<p>&bull;\u4E70~\u4E70~\u4E70\u7C7B\u5934\u6CFC\u5C3C| &omega;\u30FB&acute;)<br />\r\n&bull;\u7F8E\u5F0F\u6F2B\u753B\u7EFC\u5408\u8BA8\u8BBA\u7248\uFF0C\u5305\u62EC\u7F8E\u5F0F\u5361\u901A<br />\r\n&bull;\u65E5\u6F2B\u8BF7\u6233&rarr;<a href=\"/f/%E6%BC%AB%E7%94%BB\">\u6F2B\u753B</a><br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "<p>&bull;\u56FD\u4EA7\u6F2B\u753B\u3001\u52A8\u753B\u76F8\u5173\u53EF\u5728\u6B64\u53D1\u4E32\uFF0C\u6B22\u8FCE\u6316\u6398\u6F5C\u529B\u5185\u5BB9<br />\r\n&bull;\u6B64\u884C\u4E1A\u4ECD\u5728\u8270\u96BE\u53D1\u5C55\u5F53\u4E2D\uFF0C\u8BF7\u52FF\u82DB\u6C42\u548C\u975E\u96BE<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "\u2022\u672C\u7248\u4EE5\u65E5\u7CFB\u8F7B\u5C0F\u8BF4\u7C7B\u4E3A\u4E3B\uFF0C\u7F51\u6587\u4E5F\u53EF\u8BA8\u8BBA\u3002<br />\r\n\u2022\u8FDE\u8F7D\u7981\u6B62\uFF0C\u8BA8\u8BBA\u4E3A\u4E3B\u3002<br />\r\n\u2022\u5C0F\u8BF4\u8FDE\u8F7D\u2192<a href=\"/f/%E5%B0%8F%E8%AF%B4\">\u5C0F\u8BF4</a><br />\r\n\u2022\u975E\u5C0F\u8BF4\u7C7B\u2192<a href=\"/f/%E6%96%87%E5%AD%A6\">\u6587\u5B66</a><br>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "<img title=\"\u5927\u5BB6\u4E00\u8D77\u6765\u63A8\u9EC4\u6CB9\" src=\"http://nmbimg.fastmirror.org/image/2015-08-19/55d4437e66e9b.jpg\" height=132px width=200px style=\"float:left\" />\u2022\u53EA\u5141\u8BB8\u53D1\u5927\u7F51\u76D8\u94FE\u63A5\uFF0C\u5982\u767E\u5EA6\u4E91\u3001360\u4E91\u76D8\u3001\u8FC5\u96F7\u5FEB\u4F20\u3001115\u4E4B\u7C7B\uFF0C\u5E26\u5E7F\u544A\u8DF3\u8F6C\u6216\u79CD\u5B50\uFF08\u78C1\u529B\uFF09\u94FE\u63A5\u4E00\u5F8B\u5220+\u5C01<br>\r\n\u2022\u8BF7\u9075\u5B88\u5C01\u9762\u603B\u7248\u89C4\uFF0C\u5C24\u5176\u7981\u6B62\u53D1H\u56FE\u6216H\u6587\uFF0C\u8FDD\u8005\u4E00\u5F8B\u5220+\u5C01<br>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "<img src=\"http://nmbimg.fastmirror.org/thumb/2016-08-27/57c1ab9f5a58d.png\" title=\"\u6B22\u8FCE\u5149\u4E34\u4E1C\u65B9\u517B\u8001\u9662\uFF01\"><br>\u6B22\u8FCE\u5149\u4E34\u4E1C\u65B9\u517B\u8001\u9662\uFF01~<br>\u8BF7\u9075\u5B88\u603B\u7248\u89C4\uFF0C\u8D44\u6E90\u7C7B\u8BF7\u4F7F\u7528\u7AD9\u5916\u94FE\u63A5\u800C\u975E\u8D34\u56FE", "<img src=\"http://nmbimg.fastmirror.org/thumb/2017-06-02/5930e117606fd.jpg\"><p>&bull;\u8230C\u548C\u8230R\u90FD\u53EF\u4EE5\u8BA8\u8BBA\uFF0C\u8BF7\u5728\u3010\u53D1\u4E32\u65F6\u8BF4\u660E\u3011C or R<br>\r\n&bull;\u7981\u6B62\u51FA\u73B0\u811A\u672C\u3001\u5916\u6302\u76F8\u5173\u5185\u5BB9\uFF0C\u4E00\u5F8Bsage<br>\r\n&bull;\u7981\u6B62\u5F15\u6218\u9493\u9C7C\u3001\u65E0\u8111\u5B89\u5229\uFF08\u5305\u62EC\u5728C\u4E32\u4E2D\u5B89\u5229R\u6216\u53CD\u4E4B\uFF09\uFF0C\u4E00\u5F8B\u5220\u9664<br>\r\n&bull;C\u6216R\u5F00\u6D3B\u52A8\u65F6\u8BF7\u5728\u94A6\u5B9A\u6216\u56DE\u590D\u6700\u591A\u7684\u4E32\u4E2D\u56DE\u590D\uFF0C\u907F\u514D\u5237\u7248<br>\r\n&bull;\u672C\u7248\u5757\u5F00\u653E\u5305\u5BB9\uFF0C\u4E0D\u8BBA\u54EA\u4E2A\u8001\u5A46\u90FD\u662F\u8001\u5A46\uFF0C\u8BF7\u4E3B\u52A8\u907F\u514D\u4E0D\u6109\u5FEB<br>\r\n&bull;\u5BF9\u4E8E\u8FDD\u89C4\u6216\u5F71\u54CD\u7248\u5757\u79E9\u5E8F\u8BF7\u4E3E\u62A5\u5E76\u9644\u7406\u7531\uFF0C\u4F1A\u6709\u7EA2\u540D\u5E2E\u4F60\u5904\u7406<br>\r\n&bull;\u5171\u5EFA\u5927CR\u5171\u8363\u5708\uFF01\uFF08\u62D6\uFF09<br>\r\n&bull;\u8230\u5A18\u52A0\u901F\u3001\u672C\u5730\u7F13\u5B58\u3001\u9632\u732B\u5DE5\u5177 ACG POWER \u652F\u6301Windows/Mac/Android\uFF1A<a href=\"http://acggate.net\" target=\"_blank\">http://acggate.net</a>\r\n<br>&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>", "<p>&bull;Niconiconi~<br>&bull;\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2</p>\r\n", "\u2022\u8FC7\u6C14\u8001\u5A46\u7597\u517B\u9662", "\u2022\u4E66\u866B\u4E13\u7528\u7248\uFF0C\u6B22\u8FCE\u54AC\u6587\u56BC\u5B57\u3001\u8BC4\u6587\u63A8\u4E66<br>\r\n\u2022\u4ECA\u5929\u7684\u98CE\u513F\u597D\u55A7\u56A3<br>\r\n\u2022<s>\u90A3\u8FB9\u8D85\u5E02\u7684\u85AF\u7247\u534A\u4EF7\u5566\uFF01</s><br>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2\u3002", "\u2022\u672C\u7248\u5757\u4E3A\u7EFC\u5408\u7248\u5757\uFF0C\u4E0D\u8981\u5927\u91CF\u53D1\u5E03\u76F8\u540C\u6E38\u620F\u7684\u4E32\uFF0C\u8BF7\u4EE5\u8DDF\u4E32\u4E3A\u4E3B\uFF01<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002<br />\r\n\u2022\u8230\u961F\u4EE3\u7406 <a target=\"_blank\" href=\"http://www.acggate.net/\">http://www.acggate.net/</a>", "\u5B88\u671B\u5927\u5C41\u80A1\u5148\u950B\uFF01", "<img src=\"http://nmbimg.fastmirror.org/thumb/2017-01-13/58785af026a54.jpg\"><br>\u8BF7\u9075\u5B88\u603B\u7248\u89C4\uFF0C\u6270\u4E71\u8BA8\u8BBA\u6C1B\u56F4\u4E32\u780D<br>\u5047\u732A\u5957\u5929\u4E0B\u7B2C\u4E00\uFF01<br>\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "*\u624B\u6E38\u8BA8\u8BBA\uFF0C\u5165\u5751\u5B89\u5229\uFF0C\u622A\u56FE\u65E5\u897F<br>\r\n*\u8BF7\u548C\u8C10\u8BA8\u8BBA\uFF0C\u73A9\u8FC7\u624D\u6709\u53D1\u8A00\u6743<br>\r\n*\u6709\u4E13\u7248\u7684\u6E38\u620F\u8BF7\u5728\u4E13\u7248\u8BA8\u8BBA<br>\r\n*\u6C2A\u6C2A\u6C2A\uFF01\u4E0D\u6C2A\u8FD8\u662F\u4EBA\uFF1F\uFF01<br>", "\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "<img title=\"\u559C+1\" src=\"http://nmbimg.fastmirror.org/image/2015-12-23/5679ff89d5df6.gif\" height=125px width=222px style=\"float:left\" /><br>\u2022\u559C+1\u7248\uFF0C\u6B22\u8FCE\u5404\u4F4D\u4EA4\u6D41+1\u7ECF\u9A8C<br>\u2022<a href=\"/f/DOTA\">Dota2</a>\u3001<a href=\"/f/GTA5\">GTA5</a>\u8BF7\u81F3\u4E13\u7248\u8BA8\u8BBA.", "<img src=\"http://cdn.aixifan.com/h/upload/th/ec8c9093-44da-482b-ba51-b4d4b75d38df.jpg\"><br />\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002<br/>", "\u2022DOTA\u548CLOL\u7248\u5757\u62C6\u5206\uFF0C\u76F8\u5173\u8BA8\u8BBA\u8BF7\u79FB\u6B65\u4E13\u7248\uFF0C\u8DE8\u7248\u5F15\u6218\u4E00\u5F8B\u9501IP<br/>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "<p>&bull;\u8DE8\u7248\u5F15\u6218\u4E00\u5F8B\u9501IP<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "\u2022\u8BF7\u52FF\u53D1\u5E03VPN\u76F8\u5173\u5185\u5BB9\u53CA\u94FE\u63A5<br>\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u2022\u6E38\u620F\u548C\u5386\u53F2\u5206\u5F00\u770B\u5F85<br />\r\n\u2022\u4E0D\u8BA8\u8BBA\u8D26\u53F7\u4E70\u5356<br />\r\n\u2022\u4E25\u7981\u6311\u8D77WT\u548CWOT\u7684\u77DB\u76FE<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "<p align=\"center\">\r\n\t<img src=\"http://cdn.aixifan.com/h/upload/fa972954-6b20-4896-bd66-da255d206b31.png\" width=\"280\" height=\"76\" title=\"We didn't penetrate their armor!\" align=\"left\" alt=\"\u8DF3\u86CB\uFF01\" /> \r\n</p>\r\n<p>\r\n\u2022\u672C\u7248\u53EF\u8BA8\u8BBA\u5766\u514B\u4E16\u754C\u3001\u6218\u673A\u4E16\u754C\u3001\u6218\u8230\u4E16\u754C<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2<br />\r\n</p>", "<p>Minecraft\u6E38\u620F\u7684\u8BA8\u8BBA\u3002<br />\r\n\u66F4\u591A\u8BE6\u60C5\u8BF7\u70B9\u51FB <a href=\"http://mc.acfun.tv\" target=\"_blank\">mc.acfun.tv</a><br />\r\n\u60A8\u4E5F\u8BB8\u9700\u8981\u6700\u65B0\u7248\u672C<br />\r\n\u8BF7\u4ED4\u7EC6\u9605\u8BFB\u8BF4\u660E\u6587\u4EF6</p>\r\n\r\n<p>\u8BF7\u52FF\u7834\u574F\u6E38\u620F\u79E9\u5E8F\uFF0C\u59A8\u788D\u522B\u7684\u73A9\u5BB6\u6B63\u5E38\u6E38\u620F\u3002\u8BF7\u591A\u79CD\u6811\uFF0C\u591A\u517B\u7F8A\uFF0C\u5C11\u751F\u5B69\u5B50\u591A\u517B\u732A</p>\r\n\r\n<p>&nbsp;</p>\r\n", "\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u5E9F\u571F\u7834\u70C2\u56DE\u6536\u5904\u7406\u4E2D\u5FC3", "\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u2022\u684C\u6E38\u4E0D\u518D\u5F00\u7248\uFF0C\u8BF7\u4E00\u5F8B\u5728\u6B64\u8BA8\u8BBA\u3002<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2\u3002", "music game\u97F3\u4E50\u6E38\u620F\u4E13\u7248<br/>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2", "<p>&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002<br />\r\n&bull;\u672C\u7248\u7528\u4E8EAcFun\u5927\u9003\u6740\uFF08http://0.76573.org\uFF09\u76F8\u5173\u8BA8\u8BBA\u3002<br />\r\n&bull;0F\u7BA1\u7406\u5458\u5DF2\u7ECF\u9009\u51FA\uFF0C\u795D\u4ED6\u4EEC\u597D\u8FD0\uFF01</p>\r\n\r\n<p>&bull;\u5927\u9003\u6740\u5B98\u65B9\u73A9\u5BB6\u7FA4\uFF1A248504962</p>\r\n\r\n<p>&bull;\u5927\u9003\u6740\u73A9\u5BB6\u5439\u6C34\u7FA4\uFF1A101336468</p>\r\n\r\n<p>&bull;\u5E38\u78D0\u5927\u9003\u6740\u9879\u76EE\u6E90\u7801\uFF1Ahttps://code.google.com/p/phpbr/</p>\r\n\r\n<p>&bull;\u5BF90F\u6216\u80051F\u4EFB\u4F55\u7BA1\u7406\u5458\u7684\u884C\u52A8\u6709\u4EFB\u4F55\u7591\u95EE\u8005\uFF0C\u8BF7\u76F4\u63A5\u901A\u8FC7\u4E0A\u9762\u7684\u73A9\u5BB6\u7FA4\u6216\u8005\u5B98\u65B9\u8BBA\u575B\u8054\u7CFB\u6211\u3002</p>\r\n\r\n<p>&bull;\u5BF9\u7535\u6CE2\u6216\u8005\u7BDD\u706B\u7B49\u884D\u751F\u670D\u52A1\u5668\u4EFB\u4F55\u7BA1\u7406\u5458\u7684\u884C\u52A8\u6709\u4EFB\u4F55\u7591\u95EE\u8005\uFF0C\u4E5F\u8BF7\u5230\u73A9\u5BB6\u7FA4\u4E2D\u53BB\u627E\u5BF9\u5E94\u7684\u8D1F\u8D23\u4EBA\uFF0C\u8C22\u8C22\u3002</p>\r\n", "\u53EF\u8BA8\u8BBA\u65E5\u672C\u5076\u50CF\uFF08\u56E2\u4F53\u6216\u4E2A\u4EBA\uFF09<br>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u53EF\u8BA8\u8BBA\u4E2D\u56FD\u5076\u50CF\uFF08\u4E2A\u4EBA\u6216\u56E2\u4F53\uFF09<br>TFboys\u9664\u5916", "<p><a href=\"/t/640819\"><img alt=\"\u54AA~\" src=\"http://cdn.aixifan.com/h/upload/th/af4057b8-67ea-445f-8b18-f05e22fd9a40.jpg\" style=\"float:left\" /></a><br><font color=\"red\">\u6765\u81EA\u5FAE\u535A\u7684\u8F6C\u8F7D\u8BF7\u52A1\u5FC5\u5148\u5F81\u5F97Coser\u540C\u610F!</font></p>\r\n\r\n<p>&bull;\u8D34\u56FE\u8BF7\u4F7F\u7528\u56DE\u5E94\u6A21\u5F0F\uFF0C\u4E0D\u8981\u6BCF\u4E00\u5F20\u56FE\u5355\u72EC\u5F00\u4E32\u3002<br />\r\n&bull;\u8DDF\u98CE\u3001\u6076\u610F\u9876\u56DE\u9996\u9875\u3001\u6316\u575F\u4E00\u5F8BSAGE\u3002<br />\r\n&bull;\u975ECOS\u6444\u5F71\u8BF7\u53D1<a href=\"/f/%E6%91%84%E5%BD%B1\">\u6444\u5F71</a>\u7248\u3002<br />\r\n&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "\u2022\u58F0\u4F18\u7248\u4E0D\u662F\u7528\u6765\u6076\u641E\u7684\uFF01\u8BF7\u5408\u7406\u8BA8\u8BBA\uFF01\u5426\u5219\u9501IP<br />\r\n\u2022\u7981\u6B62PO\u6709\u5173\u5175\u5E93\u5317\u7684\u4E32\uFF08\u8F6C\u6076\u641E\uFF09<br />\r\n\u2022\u672C\u7248\u53D1\u6587\u95F4\u969415\u79D2", "<p>&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002</p>\r\n", "\u6B22\u8FCE\u8BA8\u8BBA\u5404\u7C7B\u7535\u5F71\u548C\u7535\u89C6\u8282\u76EE<br/>\u7981\u6B62\u53D1\u5E03\u4E8E\u8BE2\u95EEAV\u76F8\u5173\u5185\u5BB9\uFF0C\u8FDD\u8005\u5C01+\u5220\u4E32<br/>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u2022\u7981\u6B62\u6D89\u53CA\u4EFB\u4F55\u653F\u6CBB\u8BDD\u9898<br/>\u2022\u8BF7\u4FDD\u6301\u7406\u6027\u8BA8\u8BBA<br/>\u2022\u8FDD\u53CD\u4E0A\u8FF0\u7248\u89C4\u4E00\u5F8B\u780D\u6389+\u5C01IP\u3002<br/>\u2022\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002", "\u6709\u597D\u7684\u8EAB\u8EAF\u624D\u80FD\u627E\u5230\u597D\u57FA\u53CB\uFF0C\u597D\u597D\u953B\u70BC\u5427\uFF01<br/>\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2", "<p>&bull;\u672C\u7248\u53D1\u6587\u95F4\u9694\u4E3A15\u79D2\u3002<br />\r\n&bull;\u8BF7\u5728\u6B64\u4E3E\u62A5\u4E0D\u826F\u5185\u5BB9\uFF0C\u5E76\u9644\u4E0A\u4E32\u5730\u5740\u4EE5\u53CA\u53D1\u8A00\u8005ID\u3002\u5982\u679C\u662F\u56DE\u590D\u4E32\uFF0C\u8BF7\u9644\u4E0A&ldquo;\u56DE\u5E94&rdquo;\u94FE\u63A5\u7684\u5730\u5740\uFF0C\u683C\u5F0F\u4E3A&gt;&gt;No.\u4E32ID\u6216&gt;&gt;No.\u56DE\u590DID<br />\r\n&bull;\u8BF7\u5C3D\u91CF\u5199\u6E05\u695A\u7406\u7531\uFF0C\u5426\u5219\u7EA2\u540D\u5C06\u6309\u7167\u4E3B\u89C2\u5224\u65AD\u8FDB\u884C\u5904\u7406\u6216\u4E0D\u5904\u7406\u3002<br />\r\n&bull;\u5DF2\u5904\u7406\u7684\u4E3E\u62A5\u5C06SAGE\u3002</p>\r\n", };

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

        for (int i = 0; i < AC_FORUM_NAME_ARRAY.length; i++) {
            ACForumRaw raw = new ACForumRaw();
            raw.setPriority(i);
            raw.setForumid(AC_FORUM_ID_ARRAY[i]);
            raw.setDisplayname(AC_FORUM_NAME_ARRAY[i]);
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

    public static void addACForums(List<ACForum> list) {
        ACForumDao dao = sDaoSession.getACForumDao();

        List<ACForumRaw> currentList = sDaoSession.getACForumDao().queryBuilder()
                .orderAsc(ACForumDao.Properties.Priority).list();
        List<ACForum> addList = new ArrayList<>();
        addList.addAll(list);
        for (int i = 0, n = addList.size(); i < n; i++) {
            ACForum forum = addList.get(i);
            for (int j = 0, m = currentList.size(); j < m; j++) {
                ACForumRaw raw = currentList.get(j);
                if (ObjectUtils.equal(forum.id, raw.getForumid())) {
                    addList.remove(i);
                    i--;
                    n--;
                    break;
                }
            }
        }

        int i = getACForumMaxPriority() + 1;
        List<ACForumRaw> insertList = new ArrayList<>();
        for (ACForum forum : addList) {
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

    private static int getACForumMaxPriority() {
        List<ACForumRaw> list = sDaoSession.getACForumDao().queryBuilder()
                .orderDesc(ACForumDao.Properties.Priority).limit(1).list();
        if (list.isEmpty()) {
            return -1;
        } else {
            return list.get(0).getPriority();
        }
    }

    public static void addACForums(String name, String id) {
        ACForumRaw raw = new ACForumRaw();
        raw.setDisplayname(name);
        raw.setForumid(id);
        raw.setPriority(getACForumMaxPriority() + 1);
        raw.setVisibility(true);
        raw.setMsg("None");
        sDaoSession.getACForumDao().insert(raw);
    }

    public static ACForumRaw getACForumForForumid(String id) {
        List<ACForumRaw> list = sDaoSession.getACForumDao().queryBuilder()
                .where(ACForumDao.Properties.Forumid.eq(id)).limit(1).list();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public static void removeACForum(ACForumRaw raw) {
        sDaoSession.getACForumDao().delete(raw);
    }

    public static void updateACForum(ACForumRaw raw) {
        sDaoSession.getACForumDao().update(raw);
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
