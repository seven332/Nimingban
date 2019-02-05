package com.hippo.nimingban.util;

import com.hippo.nimingban.client.data.Forum;
import com.hippo.nimingban.dao.ACForumRaw;

import de.greenrobot.dao.query.LazyList;

public class ForumAutoSortingUtils {

    public static void addACForumFrequency(Forum forum) {
        ACForumRaw raw = DB.getACForumForForumid(forum.getNMBId());
        Integer freq = raw.getFrequency();
        DB.setACForumFrequency(raw, freq == null ? 1 : freq + 1);
    }

    public static void ageACForumFrequency() {
        long lastForumAging = Settings.getLastForumAging();
        long time = System.currentTimeMillis();
        if (time - lastForumAging > 24 * 60 * 60 * 1000) { // 24 hr * 60 min * 60 sec * 1000 milli
            LazyList<ACForumRaw> forums = DB.getACForumLazyList();
            for (ACForumRaw raw: forums) {
                Integer freq = raw.getFrequency();
                if (freq == null) { // new forum or first run
                    freq = 0;
                } else if (freq >= 2) { // bypass freq == 1 so that visited forums are always before unvisited ones.
                    freq /= 2;
                }
                DB.setACForumFrequency(raw, freq);
            }
            Settings.setLastForumAging(time);
        }
    }
}
