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

import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.DisplayForum;
import com.hippo.nimingban.dao.ACForumDao;
import com.hippo.nimingban.dao.ACForumRaw;
import com.hippo.nimingban.dao.DaoMaster;
import com.hippo.nimingban.dao.DaoSession;
import com.hippo.nimingban.dao.DraftDao;
import com.hippo.nimingban.dao.DraftRaw;
import com.hippo.yorozuya.AssertUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.LazyList;

public final class DB {

    private static DaoSession sDaoSession;

    public static class DBOpenHelper extends DaoMaster.OpenHelper {

        private boolean mFirstTime;

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            super.onCreate(db);
            mFirstTime = true;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public boolean isFirstTime() {
            return mFirstTime;
        }

        public void clearFirstTime() {
            mFirstTime = false;
        }
    }

    public static void initialize(Context context) {
        DBOpenHelper helper = new DBOpenHelper(
                context.getApplicationContext(), "nimingban", null);

        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);

        sDaoSession = daoMaster.newSession();

        if (helper.isFirstTime()) {
            helper.clearFirstTime();
            // Add default value
            insertDefaultACForums();
        }
    }

    private static void insertDefaultACForums() {
        ACForumDao dao = sDaoSession.getACForumDao();

        int size = 59;
        String[] ids = {"4", "20", "11", "30", "32", "40", "35", "56", "103", "17", "98",
                "102", "97", "89", "27", "81", "14", "12", "99", "90", "87", "19", "64", "6", "5",
                "93", "101", "2", "73", "72", "86", "22", "70", "95", "10", "34", "51", "44", "23",
                "45", "80", "28", "38", "29", "24", "25", "92", "16", "100", "13", "55", "39", "31",
                "54", "33", "37", "75", "88", "18"};
        String[] names = {"综合版1", "欢乐恶搞", "推理", "技术宅", "料理", "貓版", "音乐", "考试", "文学",
                "二次创作", "姐妹1", "女性向", "女装", "日记", "WIKI", "都市怪谈", "动画", "漫画", "国漫",
                "美漫", "轻小说", "小说", "GALGAME", "VOCALOID", "东方Project", "舰娘", "LoveLive",
                "游戏", "EVE", "DNF", "战争雷霆", "LOL", "DOTA", "GTA5", "Minecraft", "MUG", "WOT",
                "WOW", "D3", "卡牌桌游", "炉石传说", "怪物猎人", "口袋妖怪", "AC大逃杀", "索尼", "任天堂",
                "日麻", "AKB", "SNH48", "COSPLAY", "声优", "模型", "影视", "摄影", "体育", "军武",
                "数码", "天台", "值班室"};
        AssertUtils.assertEquals("ids.size must be size", size, ids.length);
        AssertUtils.assertEquals("names.size must be size", size, names.length);

        for (int i = 0; i < size; i++) {
            ACForumRaw raw = new ACForumRaw();
            raw.setPriority(i);
            raw.setForumid(ids[i]);
            raw.setDisplayname(names[i]);
            raw.setVisibility(true);
            dao.insert(raw);
        }
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
            result.add(dForum);
        }

        return result;
    }

    public static List<DisplayForum> getForums(int site, boolean onlyVisible) {
        // TODO
        return null;
    }

    public static LazyList<DraftRaw> getDraftLazyList() {
        return sDaoSession.getDraftDao().queryBuilder().orderDesc(DraftDao.Properties.Time).listLazy();
    }

    public static void addDraft(String content) {
        DraftRaw raw = new DraftRaw();
        raw.setContent(content);
        raw.setTime(System.currentTimeMillis());
        sDaoSession.getDraftDao().insert(raw);
    }

    public static void removeDraft(long id) {
        sDaoSession.getDraftDao().deleteByKey(id);
    }
}
