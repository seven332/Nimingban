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

import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.client.data.Site;
import com.hippo.text.Html;
import com.hippo.yorozuya.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ACPost extends Post {

    public static final Reply[] EMPTY_REPLY_ARRAY = new Reply[0];

    /**
     * Parse the time string from website
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault());
    private static final Object sDateFormatLock = new Object();

    static {
        // The website use GMT+08:00
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    }

    public String id = "";
    public String fid = "";
    public String img = "";
    public String ext = "";
    public String now = "";
    public String userid = "";
    public String name = "";
    public String email = "";
    public String title = "";
    public String content = "";
    public String sage = "";
    public String admin = "";
    public String replyCount = "";
    // Ignore when writeToParcel
    public List<ACReply> replys;

    private Site mSite;

    private long mTime;
    private CharSequence mUser;
    private int mReplyCount;
    private CharSequence mContent;
    private String mThumbKey;
    private String mImageKey;
    private String mThumbUrl;
    private String mImageUrl;
    private Reply[] mReplies;

    @Override
    public String toString() {
        return "id = " + id + ", img = " + img + ", ext = " + ext + ", now = " + now +
                ", userid = " + userid + ", name = " + name + ", email = " + email +
                ", title = " + title + ", content = " + content + ", admin = " + admin +
                ", replyCount = " + replyCount + ", replys = " + replys;
    }

    private static String removeDayOfWeek(String time) {
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

    public static long parseTime(String time) {
        try {
            synchronized (sDateFormatLock) {
                Date date = DATE_FORMAT.parse(removeDayOfWeek(time));
                return date.getTime();
            }
        } catch (ParseException e) {
            return 0;
        }
    }

    @Override
    public void generate(Site site) {
        mSite = site;

        mTime = parseTime(now);

        if ("1".equals(admin)) {
            Spannable spannable = new SpannableString(userid);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, userid.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mUser = spannable;
        } else {
            mUser = ACItemUtils.handleUser(Html.fromHtml(userid), getNMBPostId(), getNMBId());
        }

        mReplyCount = NumberUtils.parseIntSafely(replyCount, -1);

        mContent = ACItemUtils.generateContent(content, sage, title, name);

        if (!TextUtils.isEmpty(img)) {
            String ext2 = ext;
            if (".jpe".equals(ext2)) {
                ext2 = ".jpeg";
            }
            String key = img + ext2;
            mThumbKey = "thumb/" + key;
            mImageKey = "image/" + key;
            ACSite acSite = ACSite.getInstance();
            mThumbUrl = acSite.getPictureUrl(mThumbKey);
            mImageUrl = acSite.getPictureUrl(mImageKey);
        }

        List<ACReply> replyList = replys;
        if (replyList != null && replyList.size() > 0) {
            int n = replyList.size();
            Reply[] replies = new Reply[n];
            mReplies = replies;
            for (int i = 0; i < n; i++) {
                replies[i] = replyList.get(i);
            }
        } else {
            mReplies = EMPTY_REPLY_ARRAY;
        }
    }

    public void generateSelfAndReplies(Site site) {
        generate(site);

        // generate replies
        if (replys == null) {
            // Can't get replise
            replys = new ArrayList<>(0);
        } else {
            for (ACReply reply : replys) {
                reply.mPostId = id;
                reply.generate(site);
            }
        }
    }

    @Override
    public Site getNMBSite() {
        return mSite;
    }

    @Override
    public String getNMBId() {
        return id;
    }

    @Override
    public String getNMBFid() {
        return fid;
    }

    @Override
    public String getNMBPostId() {
        return id;
    }

    @Override
    public long getNMBTime() {
        return mTime;
    }

    @Override
    public CharSequence getNMBDisplayUsername() {
        return mUser;
    }

    @Override
    public CharSequence getNMBDisplayContent() {
        return mContent;
    }

    @Override
    public int getNMBReplyCount() {
        return mReplyCount;
    }

    @Override
    public CharSequence getNMBReplyDisplayCount() {
        return replyCount;
    }

    @Override
    public Reply[] getNMBReplies() {
        return mReplies;
    }

    @Override
    public String getNMBThumbKey() {
        return mThumbKey;
    }

    @Override
    public String getNMBImageKey() {
        return mImageKey;
    }

    @Override
    public String getNMBThumbUrl() {
        return mThumbUrl;
    }

    @Override
    public String getNMBImageUrl() {
        return mImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.fid);
        dest.writeString(this.img);
        dest.writeString(this.ext);
        dest.writeString(this.now);
        dest.writeString(this.userid);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeString(this.admin);
        dest.writeString(this.replyCount);
        dest.writeInt(this.mSite.getId());
    }

    public ACPost() {
    }

    // Need to call generate
    protected ACPost(Parcel in) {
        this.id = in.readString();
        this.fid = in.readString();
        this.img = in.readString();
        this.ext = in.readString();
        this.now = in.readString();
        this.userid = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.title = in.readString();
        this.content = in.readString();
        this.admin = in.readString();
        this.replyCount = in.readString();
        this.mSite = Site.fromId(in.readInt());
    }

    public static final Creator<ACPost> CREATOR = new Creator<ACPost>() {

        @Override
        public ACPost createFromParcel(Parcel source) {
            return new ACPost(source);
        }

        @Override
        public ACPost[] newArray(int size) {
            return new ACPost[size];
        }
    };
}
