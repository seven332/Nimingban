package com.hippo.nimingban.util;

import com.hippo.nimingban.client.data.Forum;
import com.hippo.nimingban.dao.ACForumRaw;

import de.greenrobot.dao.query.LazyList;

public class ForumAutoSortingUtils {

    private static final int FREQUENCY_UPPER_BOUND = 1 << 15;

    private static Integer setPinned(Integer freq) {
        return freq | FREQUENCY_UPPER_BOUND;
    }

    private static Integer setUnpinned(Integer freq) {
        return freq & (FREQUENCY_UPPER_BOUND - 1);
    }

    private static boolean isPinned(Integer freq) {
        return (freq & FREQUENCY_UPPER_BOUND) != 0;
    }

    private static Integer incrementFrequency(Integer freq) {
        if (freq == null) {
            return 1;
        }
        boolean pinned = isPinned(freq);
        int result = pinned ? setUnpinned(freq) : freq;

        if (result + 1 < FREQUENCY_UPPER_BOUND) { // make sure freq be in bound
            result++;
        }

        if (pinned) {
            result = setPinned(result);
        }
        return result;
    }

    private static Integer decrementFrequency(Integer freq) {
        if (freq == null) {
            return 0;
        }
        boolean pinned = isPinned(freq);
        int result = pinned ? setUnpinned(freq) : freq;

        if (result > 1) { // bypass freq == 1 so that visited forums are always before unvisited ones.
            result /= 2;
        }

        if (pinned) {
            result = setPinned(result);
        }
        return result;
    }

    public static void addACForumFrequency(Forum forum) {
        ACForumRaw raw = DB.getACForumForForumid(forum.getNMBId());
        Integer freq = raw.getFrequency();
        DB.setACForumFrequency(raw, incrementFrequency(freq));
    }

    public static void ageACForumFrequency() {
        LazyList<ACForumRaw> forums = DB.getACForumLazyList(false);
        for (ACForumRaw raw: forums) {
            Integer freq = raw.getFrequency();
            DB.setACForumFrequency(raw, decrementFrequency(freq));
        }
        DB.updateACForum(forums);
    }

    public static void pinACForum(ACForumRaw raw) {
        Integer freq = raw.getFrequency();
        DB.setACForumFrequency(raw, setPinned(freq));
    }

    public static void unpinACForum(ACForumRaw raw) {
        Integer freq = raw.getFrequency();
        DB.setACForumFrequency(raw, setUnpinned(freq));
    }

    public static boolean isACForumPinned(ACForumRaw raw) {
        Integer freq = raw.getFrequency();
        return isPinned(freq);
    }
}
