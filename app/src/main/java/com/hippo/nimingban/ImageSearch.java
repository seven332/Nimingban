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

package com.hippo.nimingban;

/*
 * Created by Hippo on 11/11/2016.
 */

import android.util.SparseArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class ImageSearch {
    private ImageSearch() {}

    private static final String IMAGE_SEARCH_URL_GOOGLE = "https://www.google.com/searchbyimage?image_url=";
    private static final String IMAGE_SEARCH_URL_BAIDU = "https://image.baidu.com/wiseshitu?queryImageUrl=";
    private static final String IMAGE_SEARCH_URL_SOGOU = "http://pic.sogou.com/ris?query=";
    private static final String IMAGE_SEARCH_URL_TINEYE = "http://tineye.com/search/?url=";
    private static final String IMAGE_SEARCH_URL_WHATANIME = "https://trace.moe/?url=";
    private static final String IMAGE_SEARCH_URL_SAUCENAO = "http://saucenao.com/search.php?db=999&url=";
    private static final String IMAGE_SEARCH_URL_IQDB = "http://www.iqdb.org/?url=";
    private static final String IMAGE_SEARCH_URL_IQDB_3D = "http://3d.iqdb.org/?url=";

    private static final SparseArray<String> mMap = new SparseArray<>();

    static {
        mMap.put(R.id.action_search_google, IMAGE_SEARCH_URL_GOOGLE);
        mMap.put(R.id.action_search_baidu, IMAGE_SEARCH_URL_BAIDU);
        mMap.put(R.id.action_search_sogou, IMAGE_SEARCH_URL_SOGOU);
        mMap.put(R.id.action_search_tineye, IMAGE_SEARCH_URL_TINEYE);
        mMap.put(R.id.action_search_whatanime, IMAGE_SEARCH_URL_WHATANIME);
        mMap.put(R.id.action_search_saucenao, IMAGE_SEARCH_URL_SAUCENAO);
        mMap.put(R.id.action_search_iqdb, IMAGE_SEARCH_URL_IQDB);
        mMap.put(R.id.action_search_iqdb_3d, IMAGE_SEARCH_URL_IQDB_3D);
    }

    public static String getImageSearchUrl(int id, String url) {
        String prefix = mMap.get(id);
        if (prefix == null) {
            return null;
        }
        try {
            return prefix + URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
