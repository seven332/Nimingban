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

package com.hippo.nimingban.content;

/*
 * Created by Hippo on 11/11/2016.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.hippo.nimingban.util.Settings;
import com.hippo.unifile.UniFile;
import java.util.List;

public class ImageProvider extends SimpleFileProvider {

    private static final String AUTHORITY = "com.hippo.nimingban.image";

    @NonNull
    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    @Override
    public UniFile getFile(Uri uri) {
        // Get dir
        UniFile dir = Settings.getImageSaveLocation();
        if (dir == null) {
            return null;
        }

        // Get filename
        List<String> path = uri.getPathSegments();
        if (path == null || path.size() != 1) {
            return null;
        }

        return dir.findFile(path.get(0));
    }

    public static Uri buildUri(String filename) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(filename)
                .build();
    }
}
