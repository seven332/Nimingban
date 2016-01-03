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

package com.hippo.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hippo.image.Image;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.SimpleHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageDrawable extends Drawable implements Animatable, Runnable {

    private static final String TAG = ImageDrawable.class.getSimpleName();

    private static final int TILE_SIZE = 512;

    private Image mImage;
    private Paint mPaint;

    private List<Tile> mTileList;

    private boolean mRunning = false;

    private static class Tile {
        Bitmap bitmap;
        int w;
        int h;
        int x;
        int y;
    }

    private ImageDrawable(@NonNull  Image image) {
        mImage = image;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTileList = createTileArray();

        // Render first frame
        render();
    }

    private List<Tile> createTileArray() {
        int width = mImage.getWidth();
        int height = mImage.getHeight();
        int capacity = MathUtils.clamp(MathUtils.ceilDivide(width, TILE_SIZE) *
                MathUtils.ceilDivide(height, TILE_SIZE), 0, 100);
        List<Tile> tiles = new ArrayList<>(capacity);

        for (int x = 0; x < width; x += TILE_SIZE) {
            int w = Math.min(TILE_SIZE, width - x);
            for (int y = 0; y < height; y += TILE_SIZE) {
                int h = Math.min(TILE_SIZE, height - y);
                Tile tile = new Tile();
                tile.x = x;
                tile.y = y;
                tile.w = w;
                tile.h = h;
                try {
                    tile.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "Out of memory");
                }
                tiles.add(tile);
            }
        }

        return tiles;
    }

    private void render() {
        Image image = mImage;
        List<Tile> tiles = mTileList;
        for (int i = 0, length = tiles.size(); i < length; i++) {
            Tile tile = tiles.get(i);
            if (tile.bitmap != null) {
                image.render(tile.x, tile.y, tile.bitmap, 0, 0, tile.w, tile.h, false, 0);
            }
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mImage.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mImage.getHeight();
    }

    public boolean isLarge() {
        return mImage.getWidth() * mImage.getHeight() > 256 * 256;
    }

    @Override
    public void draw(Canvas canvas) {
        List<Tile> tiles = mTileList;
        for (int i = 0, length = tiles.size(); i < length; i++) {
            Tile tile = tiles.get(i);
            if (tile.bitmap != null) {
                canvas.drawBitmap(tile.bitmap, tile.x, tile.y, mPaint);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void recycle() {
        // Stop animte
        stop();

        // Free tile's bitmap
        List<Tile> tiles = mTileList;
        for (int i = 0, length = tiles.size(); i < length; i++) {
            Tile tile = tiles.get(i);
            if (tile.bitmap != null) {
                tile.bitmap.recycle();
                tile.bitmap = null;
            }
        }

        mImage.recycle();
    }

    public boolean isRecycled() {
        return mImage.isRecycled();
    }

    public int getByteCount() {
        int size = 0;
        List<Tile> tiles = mTileList;
        for (int i = 0, length = tiles.size(); i < length; i++) {
            Tile tile = tiles.get(i);
            if (tile.bitmap != null) {
                size += tile.bitmap.getByteCount();
            }
        }
        return size;
    }

    @Override
    public void start() {
        if (mImage.isRecycled() || mImage.getFrameCount() <= 1 || mRunning) {
            return;
        }

        mRunning = true;

        SimpleHandler.getInstance().postDelayed(this, Math.max(0, mImage.getDelay()));
    }

    @Override
    public void stop() {
        mRunning = false;
        SimpleHandler.getInstance().removeCallbacks(this);
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void run() {
        // Check recycled
        if (mImage.isRecycled()) {
            mRunning = false;
            return;
        }

        mImage.advance();
        render();
        invalidateSelf();

        if (mRunning) {
            SimpleHandler.getInstance().postDelayed(this, Math.max(0, mImage.getDelay()));
        }
    }

    public static ImageDrawable decode(InputStream is) {
        Image image = Image.decode(is, false);
        if (image != null) {
            return new ImageDrawable(image);
        } else {
            return null;
        }
    }
}
