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

import com.hippo.conaco.BitmapPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TiledBitmapDrawable extends Drawable {

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
            Bitmap bitmap = tile.bitmap;
            if (bitmap != null) {
                pool.addReusableBitmap(bitmap);
                tile.bitmap = null;
            }
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

        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(is, true);

            Rect rect = new Rect();
            for (int x = 0; x < width; x += TILE_SIZE) {
                int w = Math.min(TILE_SIZE, width - x);
                for (int y = 0; y < height; y += TILE_SIZE) {
                    int h = Math.min(TILE_SIZE, height - y);
                    rect.set(x, y, x + w, y + h);

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    options.inSampleSize = 1;
                    options.inBitmap = pool.getInBitmap(options);
                    Bitmap bitmap = decoder.decodeRegion(rect, options);

                    if (bitmap != null) {
                        Tile tile = new Tile();
                        tile.bitmap = bitmap;
                        tile.width = w;
                        tile.height = h;
                        tile.x = x;
                        tile.y = y;
                        tiles.add(tile);
                    }
                }
            }
            return new TiledBitmapDrawable(tiles, width, height);
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
