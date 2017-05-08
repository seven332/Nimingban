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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;
import com.hippo.unifile.UniFile;
import java.io.FileNotFoundException;

public abstract class SimpleFileProvider extends ContentProvider {

  //private static final String AUTHORITY = "com.hippo.nimingban.image";

  private static final String[] COLUMNS = {
      OpenableColumns.DISPLAY_NAME,
      OpenableColumns.SIZE
  };

  @Override
  public boolean onCreate() {
    return true;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] projection,
      String selection, String[] selectionArgs, String sortOrder) {
    final UniFile file = getFileForUri(uri);
    if (file == null) {
      return null;
    }

    if (projection == null) {
      projection = COLUMNS;
    }

    String[] cols = new String[projection.length];
    Object[] values = new Object[projection.length];
    int i = 0;
    for (String col : projection) {
      if (OpenableColumns.DISPLAY_NAME.equals(col)) {
        cols[i] = OpenableColumns.DISPLAY_NAME;
        values[i++] = file.getName();
      } else if (OpenableColumns.SIZE.equals(col)) {
        cols[i] = OpenableColumns.SIZE;
        values[i++] = file.length();
      } else if (MediaStore.MediaColumns.DATA.equals(col)) {
        Uri originUri = file.getUri();
        if (ContentResolver.SCHEME_FILE.equals(originUri.getScheme())) {
          cols[i] = MediaStore.MediaColumns.DATA;
          values[i++] = file.getUri().getPath();
        } else {
          // TODO handle document tree url
        }
      }
    }

    cols = copyOf(cols, i);
    values = copyOf(values, i);

    final MatrixCursor cursor = new MatrixCursor(cols, 1);
    cursor.addRow(values);
    return cursor;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    final UniFile file = getFileForUri(uri);
    if (file != null) {
      final String name = file.getUri().getPath();
      if (name != null) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
          final String extension = name.substring(lastDot + 1);
          final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
          if (mime != null) {
            return mime;
          }
        }
      }
    }

    return "application/octet-stream";
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
    UniFile file = getFileForUri(uri);
    if (file != null) {
      ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(file.getUri(), mode);
      if (pfd != null) {
        return pfd;
      }
    }
    throw new FileNotFoundException("Can't find " + uri);
  }

  @NonNull
  public abstract String getAuthority();

  @Nullable
  public abstract UniFile getFile(Uri uri);

  @Nullable
  private UniFile getFileForUri(Uri uri) {
    // Check authority
    if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) &&
        getAuthority().equals(uri.getAuthority())) {
      return getFile(uri);
    } else {
      return null;
    }
  }

  private static String[] copyOf(String[] original, int newLength) {
    final String[] result = new String[newLength];
    System.arraycopy(original, 0, result, 0, newLength);
    return result;
  }

  private static Object[] copyOf(Object[] original, int newLength) {
    final Object[] result = new Object[newLength];
    System.arraycopy(original, 0, result, 0, newLength);
    return result;
  }
}
