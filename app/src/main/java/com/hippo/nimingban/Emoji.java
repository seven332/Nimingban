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

package com.hippo.nimingban;

import android.content.Context;

import com.hippo.yorozuya.AssertUtils;

public final class Emoji {

    public static final int COUNT = 93;

    public static final int INDEX_IDEOGRAPHIC_SPACE = 92;

    public static final String[] EMOJI_NAME = {
            "|∀ﾟ",          "(´ﾟДﾟ`)",      "(;´Д`)",
            "(｀･ω･)",    "(=ﾟωﾟ)=",       "| ω・´)",
            "|-` )",       "|д` )",        "|ー` )",
            "|∀` )",      "(つд⊂)",       "(ﾟДﾟ≡ﾟДﾟ)",
            "(＾o＾)ﾉ",    "(|||ﾟДﾟ)",     "( ﾟ∀ﾟ)",
            "( ´∀`)",     "(*´∀`)",        "(*ﾟ∇ﾟ)",
            "(*ﾟーﾟ)",     "(　ﾟ 3ﾟ)",      "( ´ー`)",
            "( ・_ゝ・)",  "( ´_ゝ`)",      "(*´д`)",
            "(・ー・)",    "(・∀・)",       "(ゝ∀･)",
            "(〃∀〃)",     "( ﾟ∀。)",      "( `д´)",      // 30
            "(`ε´ )",     "(`ヮ´ )",       "σ`∀´)",
            " ﾟ∀ﾟ)σ",     "ﾟ ∀ﾟ)ノ",        "(╬ﾟдﾟ)",
            "(|||ﾟдﾟ)",   "( ﾟдﾟ)",        "Σ( ﾟдﾟ)",
            "( ;ﾟдﾟ)",     "( ;´д`)",      "(　д ) ﾟ ﾟ",
            "( ☉д⊙)",    "(((　ﾟдﾟ)))",   "( ` ・´)",
            "( ´д`)",       "( -д-)",        "(>д<)",
            "･ﾟ( ﾉд`ﾟ)",    "( TдT)",         "(￣∇￣)",
            "(￣3￣)",      "(￣ｰ￣)",       "(￣ . ￣)",
            "(￣皿￣)",     "(￣艸￣)",      "(￣︿￣)",
            "(￣︶￣)",     "ヾ(´ωﾟ｀)",      "(*´ω`*)",      // 60
            "(・ω・)",      "( ´・ω)",     "(｀・ω)",
            "(´・ω・`)",     "(`・ω・´)",   "( `_っ´)",
            "( `ー´)",       "( ´_っ`)",     "( ´ρ`)",
            "( ﾟωﾟ)",       "(oﾟωﾟo)",    "(　^ω^)",
            "(｡◕∀◕｡)",      "/( ◕‿‿◕ )\\", "ヾ(´ε`ヾ)",
            "(ノﾟ∀ﾟ)ノ",     "(σﾟдﾟ)σ",    "(σﾟ∀ﾟ)σ",
            "|дﾟ )",        "┃電柱┃",        "ﾟ(つд`ﾟ)",
            "ﾟÅﾟ )　",       "⊂彡☆))д`)",  "⊂彡☆))д´)",
            "⊂彡☆))∀`)",    "(´∀((☆ミつ",  "（<ゝω・）☆",
            "¯\\_(ツ)_/¯",   "☎110",        "⚧",        // 90
            "☕",            "(`ε´ (つ*⊂)",  "ideographic_space"
    };

    public static final String[] EMOJI_VALUE = {
            "|∀ﾟ",          "(´ﾟДﾟ`)",      "(;´Д`)",
            "(｀･ω･)",    "(=ﾟωﾟ)=",       "| ω・´)",
            "|-` )",       "|д` )",        "|ー` )",
            "|∀` )",      "(つд⊂)",       "(ﾟДﾟ≡ﾟДﾟ)",
            "(＾o＾)ﾉ",    "(|||ﾟДﾟ)",     "( ﾟ∀ﾟ)",
            "( ´∀`)",     "(*´∀`)",        "(*ﾟ∇ﾟ)",
            "(*ﾟーﾟ)",     "(　ﾟ 3ﾟ)",      "( ´ー`)",
            "( ・_ゝ・)",  "( ´_ゝ`)",      "(*´д`)",
            "(・ー・)",    "(・∀・)",       "(ゝ∀･)",
            "(〃∀〃)",     "( ﾟ∀。)",      "( `д´)",      // 30
            "(`ε´ )",     "(`ヮ´ )",       "σ`∀´)",
            " ﾟ∀ﾟ)σ",     "ﾟ ∀ﾟ)ノ",        "(╬ﾟдﾟ)",
            "(|||ﾟдﾟ)",   "( ﾟдﾟ)",        "Σ( ﾟдﾟ)",
            "( ;ﾟдﾟ)",     "( ;´д`)",      "(　д ) ﾟ ﾟ",
            "( ☉д⊙)",    "(((　ﾟдﾟ)))",   "( ` ・´)",
            "( ´д`)",       "( -д-)",        "(>д<)",
            "･ﾟ( ﾉд`ﾟ)",    "( TдT)",         "(￣∇￣)",
            "(￣3￣)",      "(￣ｰ￣)",       "(￣ . ￣)",
            "(￣皿￣)",     "(￣艸￣)",      "(￣︿￣)",
            "(￣︶￣)",     "ヾ(´ωﾟ｀)",      "(*´ω`*)",      // 60
            "(・ω・)",      "( ´・ω)",     "(｀・ω)",
            "(´・ω・`)",     "(`・ω・´)",   "( `_っ´)",
            "( `ー´)",       "( ´_っ`)",     "( ´ρ`)",
            "( ﾟωﾟ)",       "(oﾟωﾟo)",    "(　^ω^)",
            "(｡◕∀◕｡)",      "/( ◕‿‿◕ )\\", "ヾ(´ε`ヾ)",
            "(ノﾟ∀ﾟ)ノ",     "(σﾟдﾟ)σ",    "(σﾟ∀ﾟ)σ",
            "|дﾟ )",        "┃電柱┃",        "ﾟ(つд`ﾟ)",
            "ﾟÅﾟ )　",       "⊂彡☆))д`)",  "⊂彡☆))д´)",
            "⊂彡☆))∀`)",    "(´∀((☆ミつ",  "（<ゝω・）☆",
            "¯\\_(ツ)_/¯",   "☎110",        "⚧",        // 90
            "☕",            "(`ε´ (つ*⊂)",  "\u3000"
    };

    static {
        AssertUtils.assertEquals("EMOJI_NAME.length should be COUNT", COUNT, EMOJI_NAME.length);
        AssertUtils.assertEquals("EMOJI_VALUE.length should be COUNT", COUNT, EMOJI_VALUE.length);
    }

    public static void initialize(Context context) {
        EMOJI_NAME[INDEX_IDEOGRAPHIC_SPACE] = context.getString(R.string.ideographic_space);
    }
}
