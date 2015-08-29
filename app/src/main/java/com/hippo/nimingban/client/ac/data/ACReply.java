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
import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.data.Reply;

import java.text.ParseException;
import java.util.Date;

public class ACReply extends Reply {

    public String id = "";
    public String img = "";
    public String ext = "";
    public String now = "";
    public String userid = "";
    public String name = "";
    public String email = "";
    public String title = "";
    public String content = "";
    public String admin = "";

    String mPostId;
    private long mTime;
    private String mTimeStr;
    private CharSequence mUser;
    private CharSequence mContent;
    private String mThumb;
    private String mImage;

    @Override
    public String toString() {
        return "id = " + id + ", img = " + img + ", ext = " + ext + ", now = " + now +
                ", userid = " + userid + ", name = " + name + ", email = " + email +
                ", title = " + title + ", content = " + content + ", admin = " + admin;
    }

    static String removeDayOfWeek(String time) {
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
        try {
            Date date;
            synchronized (ACPost.sDateFormatLock) {
                date = ACPost.DATE_FORMAT.parse(removeDayOfWeek(now));
            }
            mTime = date.getTime();
            mTimeStr = Reply.generateTimeString(date);
        } catch (ParseException e) {
            // Can't parse date, may be the format has changed
            e.printStackTrace();
            mTimeStr = now;
        }

        if ("1".equals(admin)) {
            Spannable spannable = new SpannableString(userid);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, userid.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mUser = spannable;
        } else {
            mUser = userid;
        }

        mContent = ACPost.generateContent(content);

        if (!TextUtils.isEmpty(img)) {
            mThumb = ACUrl.HOST + "/Public/Upload/thumb/" + img + ext;
            mImage = ACUrl.HOST + "/Public/Upload/image/" + img + ext;
        }
    }

    @Override
    public String getNMBId() {
        return id;
    }

    @Override
    public String getNMBPostId() {
        return mPostId;
    }

    @Override
    public long getNMBTime() {
        return mTime;
    }

    @Override
    public CharSequence getNMBTimeStr() {
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
    public String getNMBThumbUrl() {
        return mThumb;
    }

    @Override
    public String getNMBImageUrl() {
        return mImage;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.img);
        dest.writeString(this.ext);
        dest.writeString(this.now);
        dest.writeString(this.userid);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeString(this.admin);
    }

    public ACReply() {
    }

    protected ACReply(Parcel in) {
        this.id = in.readString();
        this.img = in.readString();
        this.ext = in.readString();
        this.now = in.readString();
        this.userid = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.title = in.readString();
        this.content = in.readString();
        this.admin = in.readString();
    }

    public static final Creator<ACReply> CREATOR = new Creator<ACReply>() {
        public ACReply createFromParcel(Parcel source) {
            return new ACReply(source);
        }

        public ACReply[] newArray(int size) {
            return new ACReply[size];
        }
    };
}
