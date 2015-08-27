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

package com.hippo.nimingban.client.ac.data;

import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.hippo.nimingban.client.data.Post;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ACPost extends Post {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault());
    private static final Object sDateFormatLock = new Object();

    private String id = "";
    private String img = "";
    private String ext = "";
    private String now = "";
    private String userid = "";
    private String name = "";
    private String email = "";
    private String title = "";
    private String content = "";
    private String admin = "";
    private String replyCount = "";
    private List<ACReply> replys;

    private long mTime;
    private String mTimeStr;
    private CharSequence mUser;
    private CharSequence mContent;


    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }

    public void setReplys(List<ACReply> replys) {
        this.replys = replys;
    }

    @Override
    public String toString() {
        return "id = " + id + ", img = " + img + ", ext = " + ext + ", now = " + now +
                ", userid = " + userid + ", name = " + name + ", email = " + email +
                ", title = " + title + ", content = " + content + ", admin = " + admin +
                ", replyCount = " + replyCount + ", replys = " + replys;
    }

    private String removeDayOfWeek(String time) {
        StringBuilder sb = new StringBuilder(time.length() - 3);
        boolean inBrackets = false;
        for (int i = 0, n = time.length(); i < n; i++) {
            char c = time.charAt(i);
            if (inBrackets) {
                if (c == ')') {
                    inBrackets = false;
                } else {
                    // Skip
                }
            } else {
                if (c == '(') {
                    inBrackets = true;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public void generate() {
        // The object is from JSON, so make it valid to avoid exception

        try {
            Date date;
            synchronized (sDateFormatLock) {
                date = DATE_FORMAT.parse(removeDayOfWeek(now));
            }
            mTime = date.getTime();
            mTimeStr = Post.generateTimeString(date);
        } catch (ParseException e) {
            // Can't parse date, may be the format has changed
            mTimeStr = now;
        }

        if ("1".equals(admin)) {

            Log.d("TAG", "\"1\".equals(admin)");

            Spannable spannable = new SpannableString(userid);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, userid.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mUser = spannable;
        } else {
            mUser = userid;
        }

        mContent = Html.fromHtml(content);
    }

    @Override
    public String getNMBId() {
        return id;
    }

    @Override
    public CharSequence getNMBTime() {
        return mTimeStr;
    }

    @Override
    public CharSequence getNMBUser() {
        return mUser;
    }

    @Override
    public CharSequence getNMBContent() {
        return mContent;
    }

    @Override
    public CharSequence getNMBReplyCount() {
        return replyCount;
    }

    @Override
    public String getNMBThumbUrl() {
        return null;
    }

    @Override
    public String getNMBImageUrl() {
        return null;
    }
}
