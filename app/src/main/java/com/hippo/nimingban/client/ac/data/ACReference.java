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
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.data.Reply;

import java.text.ParseException;
import java.util.Date;

public class ACReference extends Reply {

    public String id = "";
    public String postId = "";
    public String time = "";
    public String title = "";
    public String user = "";
    public String userId = "";
    public boolean admin;
    public String email = "";
    public String content = "";
    public String thumb = "";
    public String image = "";

    private int mSite;

    private long mTime;
    private String mTimeStr;
    private CharSequence mUser;
    private CharSequence mContent;
    private String mThumb;
    private String mImage;

    @Override
    public String toString() {
        return "id = " + id + ", postId = " + postId + ", time = " + time + ", title = " + title +
                ", user = " + user + ", userId = " + userId + ", admin = " + admin +
                ", email = " + email + ", content = " + content + ", thumb = " + thumb +
                ", image = " + image;
    }

    public void generate(int site) {
        mSite = site;

        try {
            Date date;
            synchronized (ACPost.sDateFormatLock) {
                date = ACPost.DATE_FORMAT.parse(ACReply.removeDayOfWeek(time));
            }
            mTime = date.getTime();
            mTimeStr = Reply.generateTimeString(date);
        } catch (ParseException e) {
            // Can't parse date, may be the format has changed
            mTimeStr = time;
        }

        if (admin) {
            Spannable spannable = new SpannableString(userId);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, userId.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mUser = spannable;
        } else {
            mUser = Html.fromHtml(userId);
        }

        mContent = ACPost.generateContent(content);

        // Make it could hit cache
        if (!TextUtils.isEmpty(thumb)) {
            mThumb = ACUrl.HOST + thumb.replaceAll("/+", "/");
        }
        if (!TextUtils.isEmpty(image)) {
            mImage = ACUrl.HOST + image.replaceAll("/+", "/");
        }
    }

    @Override
    public int getNMBSite() {
        return mSite;
    }

    @Override
    public String getNMBId() {
        return id;
    }

    @Override
    public String getNMBPostId() {
        return postId;
    }

    @Override
    public long getNMBTime() {
        return mTime;
    }

    @Override
    public CharSequence getNMBDisplayTime() {
        return mTimeStr;
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
        dest.writeString(this.postId);
        dest.writeString(this.time);
        dest.writeString(this.title);
        dest.writeString(this.user);
        dest.writeString(this.userId);
        dest.writeByte(admin ? (byte) 1 : (byte) 0);
        dest.writeString(this.email);
        dest.writeString(this.content);
        dest.writeString(this.thumb);
        dest.writeString(this.image);
        dest.writeInt(this.mSite);
    }

    public ACReference() {
    }

    protected ACReference(Parcel in) {
        this.id = in.readString();
        this.postId = in.readString();
        this.time = in.readString();
        this.title = in.readString();
        this.user = in.readString();
        this.userId = in.readString();
        this.admin = in.readByte() != 0;
        this.email = in.readString();
        this.content = in.readString();
        this.thumb = in.readString();
        this.image = in.readString();
        this.mSite = in.readInt();
    }

    public static final Creator<ACReference> CREATOR = new Creator<ACReference>() {
        public ACReference createFromParcel(Parcel source) {
            return new ACReference(source);
        }

        public ACReference[] newArray(int size) {
            return new ACReference[size];
        }
    };
}
