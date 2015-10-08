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

package com.hippo.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hippo.conaco.BitmapPool;
import com.hippo.yorozuya.MathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TiledBitmapDrawable extends Drawable {

    private static final String TAG = TiledBitmapDrawable.class.getSimpleName();

    private static final int TILE_SIZE = 256;

    private List<Tile> mTiles;
    private int mWidth;
    private int mHeight;

    private TiledBitmapDrawable(@NonNull List<Tile> tiles, int width, int height) {
        mTiles = tiles;
        mWidth = width;
        mHeight = height;
    }

    public void recycle(BitmapPool pool) {
        if (mTiles == null) {
            return;
        }

        for (Tile tile : mTiles) {
            if (pool != null) {
                Bitmap bitmap = tile.bitmap;
                if (bitmap != null) {
                    pool.addReusableBitmap(bitmap);
                }
            }
            tile.bitmap = null;
        }

        mTiles = null;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        // TODO
    }

    @Override
    public void draw(Canvas canvas) {
        if (mTiles == null) {
            return;
        }

        for (Tile tile : mTiles) {
            canvas.drawBitmap(tile.bitmap, tile.x, tile.y, null);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // TODO
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public int getByteCount() {
        if (mTiles == null) {
            return 0;
        } else {
            int size = 0;
            for (Tile tile : mTiles) {
                size += tile.bitmap.getByteCount();
            }
            return size;
        }
    }

    public static TiledBitmapDrawable from(InputStream is, int width, int height, BitmapPool pool) {
        List<Tile> tiles = new ArrayList<>();

        int scale;
        if (width / height >= 5 || height / width >= 5) {
            // It might be slender image, keep it
            scale = 1;
        } else {
            scale = MathUtils.previousPowerOf2(Math.max((width * height / 1536 / 1536) + 1, 1));
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inSampleSize = scale;

        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(is, true);

            Rect rect = new Rect();
            for (int x = 0; x < width; x += TILE_SIZE) {
                int w = Math.min(TILE_SIZE, width - x);
                for (int y = 0; y < height; y += TILE_SIZE) {
                    int h = Math.min(TILE_SIZE, height - y);
                    boolean bottom = y + TILE_SIZE >= height;
                    boolean tryCutBottom = false;
                    Bitmap bitmap;

                    while (true) {
                        rect.set(x, y, x + w, y + h);
                        //options.inBitmap = pool.getInBitmap(options);

                        try {
                            bitmap = decoder.decodeRegion(rect, options);

                            if (bitmap == null && bottom && !tryCutBottom && h > 1) {
                                tryCutBottom = true;
                                h--;
                            } else {
                                break;
                            }
                        } catch (OutOfMemoryError e) {
                            Log.e(TAG, "Out of memory");
                            bitmap = null;
                            break;
                        }
                    }

                    if (bitmap != null) {
                        Tile tile = new Tile();
                        tile.bitmap = bitmap;
                        tile.width = w / scale;
                        tile.height = h / scale;
                        tile.x = x / scale;
                        tile.y = y / scale;
                        tiles.add(tile);
                    }
                }
            }
            return new TiledBitmapDrawable(tiles, width / scale, height / scale);
        } catch (IOException e) {
            // Recycle
            for (Tile tile : tiles) {
                Bitmap bitmap = tile.bitmap;
                if (bitmap != null) {
                    pool.addReusableBitmap(bitmap);
                    tile.bitmap = null;
                }
            }
            return null;
        } finally {
            if (decoder != null) {
                decoder.recycle();
            }
        }
    }

    private static class Tile {
        Bitmap bitmap;
        int width;
        int height;
        int x;
        int y;
    }
}
