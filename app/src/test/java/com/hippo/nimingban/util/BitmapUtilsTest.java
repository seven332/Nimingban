/*
 * Copyright 2018 Hippo Seven
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

package com.hippo.nimingban.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BitmapUtilsTest {

    @Test
    public void testRotate() {
        int[] pixels = new int[] {
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12
        };
        int[] expected = new int[] {
                9, 5, 1,
                10, 6, 2,
                11, 7, 3,
                12, 8, 4
        };

        assertArrayEquals(BitmapUtils.rotate(pixels, 4, 3, null), expected);
    }

    @Test
    public void testRotateFit() {
        int[] pixels1 = new int[] {
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12
        };

        int[] pixels2 = BitmapUtils.rotate(pixels1, 4, 3, null);
        int[] pixels3 = BitmapUtils.rotate(pixels2, 3, 4, null);
        int[] pixels4 = BitmapUtils.rotate(pixels3, 4, 3, null);
        int[] pixels5 = BitmapUtils.rotate(pixels4, 3, 4, null);

        assertArrayEquals(pixels1, pixels5);
    }
}
