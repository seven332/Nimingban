/*
 * Copyright 2016 Hippo Seven
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

import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;

import com.hippo.nimingban.client.ReferenceSpan;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.client.data.ACSite;
import com.hippo.nimingban.util.Settings;
import com.hippo.text.Html;
import com.hippo.yorozuya.StringUtils;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class ACItemUtils {
    private ACItemUtils() {}

    private static final Pattern REFERENCE_PATTERN = Pattern.compile(">>?(?:No.)?(\\d+)");
    private static final Pattern URL_PATTERN = Pattern.compile("(http|https)://[a-z0-9A-Z%-]+(\\.[a-z0-9A-Z%-]+)+(:\\d{1,5})?(/[a-zA-Z0-9-_~:#@!&',;=%/\\*\\.\\?\\+\\$\\[\\]\\(\\)]+)?/?");
    private static final Pattern AC_PATTERN = Pattern.compile("ac\\d+");

    private static final String NO_TITLE = "无标题";
    private static final String NO_NAME = "无名氏";

    private static final int COUNT_NUMBER = '9' - '0' + 1;
    private static final int COUNT_UPPERCASE_LETTER = 'z' - 'a' + 1;
    private static final int COUNT_LOWERCASE_LETTER = 'Z' - 'A' + 1;
    private static final int COUNT_SUM = COUNT_NUMBER + COUNT_UPPERCASE_LETTER + COUNT_LOWERCASE_LETTER;

    private static final byte[] IV = new byte[] {0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};

    private static final ACHtmlTagHandler AC_HTML_TAG_HANDLER = new ACHtmlTagHandler();

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

            URLSpan urlSpan = new URLSpan("http://www.acfun.cn/v/" + m.group(0));
            spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable == null ? content : spannable;
    }

    public static CharSequence generateContent(String content) {
        CharSequence charSequence;
        charSequence = Html.fromHtml(content, null, AC_HTML_TAG_HANDLER);
        charSequence = handleReference(charSequence);
        charSequence = handleTextUrl(charSequence);
        charSequence = handleAcUrl(charSequence);

        return charSequence;
    }

    public static CharSequence generateContent(String content, String sage, String title, String name) {
        StringBuilder sb = new StringBuilder(44 + 11 + StringUtils.length(title) +
                11 + StringUtils.length(name) +
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
        sb.append(content);

        return generateContent(sb.toString());
    }

    private static char getReadableChar(byte b) {
        int i = (b & 0xFF) % COUNT_SUM;
        if (i < COUNT_NUMBER) {
            return (char) ('0' + i);
        } else if (i < COUNT_NUMBER + COUNT_UPPERCASE_LETTER) {
            return (char) ('a' + i - COUNT_NUMBER);
        } else if (i < COUNT_NUMBER + COUNT_UPPERCASE_LETTER + COUNT_LOWERCASE_LETTER) {
            return (char) ('A' + i - COUNT_NUMBER - COUNT_UPPERCASE_LETTER);
        } else {
            return '?';
        }
    }

    private static String toReadableString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b: bytes) {
            sb.append(getReadableChar(b));
        }
        return sb.toString();
    }

    private static boolean isSimpleUser(@Nullable String userID) {
        if (null == userID) {
            return false;
        }

        for (int i = 0, n = userID.length(); i < n; i++) {
            char ch = userID.charAt(i);
            if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z')) {
                return false;
            }
        }

        return true;
    }

    public static CharSequence handleUser(CharSequence user, String postId, String id) {
        String key;

        switch (Settings.getChaosLevel()) {
            case 1: // Relatively chaotic
                key = postId;
                break;
            case 2: // Absolutely chaotic
                key = id;
                break;
            default:
                return user;
        }

        String userStr = user.toString();

        if (!isSimpleUser(userStr)) {
            return user;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes("utf-8"));
            byte[] keyBytes = digest.digest();
            byte[] input = userStr.getBytes("utf-8");

            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
            int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
            cipher.doFinal(cipherText, ctLength);

            return toReadableString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            return user;
        }
    }

    private static class ACHtmlTagHandler implements Html.TagHandler {
        @Override
        public boolean handleTag(boolean opening, String tag,
                SpannableStringBuilder output, XMLReader xmlReader, Attributes attributes) {
            if (tag.equalsIgnoreCase("a")) {
                if (opening) {
                    // Add ac nmb host
                    String href = attributes.getValue("", "href");
                    if (!href.startsWith("http")) {
                        if (href.startsWith("/")){
                            href = ACUrl.getHost() + href;
                        } else {
                            href = ACUrl.getHost() + '/' + href;
                        }
                    }
                    int len = output.length();
                    output.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
                } else {
                    int len = output.length();
                    Object obj = getLast(output, Href.class);
                    int where = output.getSpanStart(obj);
                    output.removeSpan(obj);
                    if (where != len) {
                        Href h = (Href) obj;
                        if (h.mHref != null) {
                            output.setSpan(new URLSpan(h.mHref), where, len,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private static Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }
}
