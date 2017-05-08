/*
 * Copyright 2017 Hippo Seven
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
 * Created by Hippo on 5/8/2017.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.unifile.UniFile;
import java.io.File;

public class UniversalProvider extends SimpleFileProvider {

  private static final String AUTHORITY = "com.hippo.nimingban.universal";

  @NonNull
  @Override
  public String getAuthority() {
    return AUTHORITY;
  }

  @Nullable
  @Override
  public UniFile getFile(Uri uri) {
    File file = new File(uri.getPath());
    return UniFile.fromFile(file);
  }

  public static Uri buildUri(String filePath) {
    return new Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(filePath)
        .build();
  }
}
