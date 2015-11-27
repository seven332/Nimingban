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
import android.text.style.URLSpan;

import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ReferenceSpan;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.client.data.Post;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.nimingban.client.data.Site;
import com.hippo.nimingban.util.ResImageGetter;
import com.hippo.text.Html;
import com.hippo.yorozuya.NumberUtils;
import com.hippo.yorozuya.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACPost extends Post {

    public static final Reply[] EMPTY_REPLY_ARRAY = new Reply[0];

    /**
     * Parse the time string from website
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.getDefault());
    private static final Object sDateFormatLock = new Object();

    private static final Pattern REFERENCE_PATTERN = Pattern.compile(">>?(?:No.)?(\\d+)");
    private static final Pattern URL_PATTERN = Pattern.compile("(http|https)://[a-z0-9A-Z%-]+(\\.[a-z0-9A-Z%-]+)+(:\\d{1,5})?(/[a-zA-Z0-9-_~:#@!&',;=%/\\*\\.\\?\\+\\$\\[\\]\\(\\)]+)?/?");
    private static final Pattern AC_PATTERN = Pattern.compile("ac\\d+");

    private static final String NO_TITLE = "无标题";
    private static final String NO_NAME = "无名氏";

    static {
        // The website use GMT+08:00
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    }

    public String id = "";
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
    // Ingore when writeToParcel
    public List<ACReply> replys;

    private Site mSite;

    private long mTime;
    private CharSequence mUser;
    private int mReplyCount;
    private CharSequence mContent;
    private String mThumb;
    private String mImage;
    private Reply[] mReplies;

    @Override
    public String toString() {
        return "id = " + id + ", img = " + img + ", ext = " + ext + ", now = " + now +
                ", userid = " + userid + ", name = " + name + ", email = " + email +
                ", title = " + title + ", content = " + content + ", admin = " + admin +
                ", replyCount = " + replyCount + ", replys = " + replys;
    }

    public static CharSequence fixURLSpan(CharSequence content) {
        if (!(content instanceof Spanned)) {
            return content;
        }

        Spannable spannable;
        if (content instanceof Spannable) {
            spannable = (Spannable) content;
        } else {
            spannable = new SpannableString(content);
        }

        URLSpan[] urlSpans = spannable.getSpans(0, content.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = spannable.getSpanStart(urlSpan);
            int end = spannable.getSpanEnd(urlSpan);
            String url = urlSpan.getURL();
            String newUrl;
            if (url.startsWith("http")) {
                newUrl = url;
            } else if (url.startsWith("/")){
                newUrl = ACUrl.HOST + url;
            } else {
                newUrl = ACUrl.HOST + '/' + url;
            }

            //noinspection StringEquality
            if (newUrl != url) {
                spannable.removeSpan(urlSpan);
                spannable.setSpan(new URLSpan(newUrl), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return content;
    }

    public static CharSequence handleReference(CharSequence content) {
        Matcher m = REFERENCE_PATTERN.matcher(content);

        Spannable spannable = null;
        while (m.find()) {
            // Ensure spannable
            if (spannable == null) {
                if (content instanceof Spannable) {
                    spannable = (Spannable) content;
                } else {
                    spannable = new SpannableString(content);
                }
            }

            int start = m.start();
            int end = m.end();

            ReferenceSpan referenceSpan = new ReferenceSpan(ACSite.getInstance(), m.group(1));
            spannable.setSpan(referenceSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable == null ? content : spannable;
    }

    public static CharSequence handleTextUrl(CharSequence content) {
        Matcher m = URL_PATTERN.matcher(content);

        Spannable spannable = null;
        while (m.find()) {
            // Ensure spannable
            if (spannable == null) {
                if (content instanceof Spannable) {
                    spannable = (Spannable) content;
                } else {
                    spannable = new SpannableString(content);
                }
            }

            int start = m.start();
            int end = m.end();

            URLSpan[] links = spannable.getSpans(start, end, URLSpan.class);
            if (links.length > 0) {
                // There has been URLSpan already, leave it alone
                continue;
            }

            URLSpan urlSpan = new URLSpan(m.group(0));
            spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable == null ? content : spannable;
    }

    public static CharSequence handleAcUrl(CharSequence content) {
        Matcher m = AC_PATTERN.matcher(content);

        Spannable spannable = null;
        while (m.find()) {
            // Ensure spannable
            if (spannable == null) {
                if (content instanceof Spannable) {
                    spannable = (Spannable) content;
                } else {
                    spannable = new SpannableString(content);
                }
            }

            int start = m.start();
            int end = m.end();

            URLSpan[] links = spannable.getSpans(start, end, URLSpan.class);
            if (links.length > 0) {
                // There has been URLSpan already, leave it alone
                continue;
            }

            URLSpan urlSpan = new URLSpan("http://www.acfun.tv/v/" + m.group(0));
            spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable == null ? content : spannable;
    }

    public static CharSequence generateContent(String content) {
        CharSequence charSequence;
        charSequence = Html.fromHtml(StringUtils.replace(content, "#滑稽", "<img src=\"" + R.drawable.emoji_huaji + "\">"),
                new ResImageGetter(), null);
        charSequence = fixURLSpan(charSequence);
        charSequence = handleReference(charSequence);
        charSequence = handleTextUrl(charSequence);
        charSequence = handleAcUrl(charSequence);

        return charSequence;
    }

    public static CharSequence generateContent(String content, String sage, String title, String name, String email) {
        StringBuilder sb = new StringBuilder(44 + 11 + StringUtils.length(title) +
                11 + StringUtils.length(name) + 11 + StringUtils.length(email) +
                StringUtils.length(content));
        if ("1".equals(sage)) {
            sb.append("<font color=\"red\"><b>SAGE</b></font><br><br>");
        }
        if (!TextUtils.isEmpty(title) && !NO_TITLE.equals(title)) {
            sb.append("<b>").append(title).append("</b><br>");
        }
        if (!TextUtils.isEmpty(name) && !NO_NAME.equals(name)) {
            sb.append("<b>").append(name).append("</b><br>");
        }
        if (!TextUtils.isEmpty(email)) {
            sb.append("<b>").append(email).append("</b><br>");
        }
        sb.append(content);

        return generateContent(sb.toString());
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
            mUser = Html.fromHtml(userid);
        }

        mReplyCount = NumberUtils.parseIntSafely(replyCount, -1);

        mContent = generateContent(content, sage, title, name, email);

        if (!TextUtils.isEmpty(img)) {
            String ext2 = ext;
            if (".jpe".equals(ext2)) {
                ext2 = ".jpeg";
            }
            mThumb = ACUrl.HOST + "/Public/Upload/thumb/" + img + ext2;
            mImage = ACUrl.HOST + "/Public/Upload/image/" + img + ext2;
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
                reply.generate(site);
                reply.mPostId = id;
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
        dest.writeString(this.replyCount);
        dest.writeInt(this.mSite.getId());
    }

    public ACPost() {
    }

    // Need to call generate
    protected ACPost(Parcel in) {
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
