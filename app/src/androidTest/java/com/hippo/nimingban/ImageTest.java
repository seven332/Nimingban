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

import android.util.Log;

import com.hippo.image.Image;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageTest extends TestCase {

    public void testPngDecode() throws FileNotFoundException {
        int i = 1;
        while (i-- > 0) {

            if (i % 10 == 0) {
                Log.d("TAG", "" + (i / 10));
            }

            Image image = Image.decode(new FileInputStream("/sdcard/b.png"), false);
            if (image != null) {
                image.recycle();
            } else {
                Log.d("TAG", "image is null");
            }
        }
    }
}
