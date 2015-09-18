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

package com.hippo.nimingban.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hippo.nimingban.R;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Stack;

public class DoodleView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private Path mPath;
    private Paint mPaint;

    private int mBgColor;

    private int mColor;
    private int mWidth;

    private float mX, mY;

    private boolean mIsDot;

    private boolean mEraser = false;

    private Rect mSrc = new Rect();
    private Rect mDst = new Rect();

    private Recycler mRecycler;

    private Helper mHelper;

    private SaveTask mSaveTask;

    public DoodleView(Context context) {
        super(context);
        init(context);
    }

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DoodleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mBgColor = ResourcesUtils.getAttrColor(context, R.attr.colorPure);

        mColor = ResourcesUtils.getAttrColor(context, R.attr.colorPureInverse);
        mWidth = LayoutUtils.dp2pix(context, 4);

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRecycler = new Recycler();
    }

    public int getPaintColor() {
        return mColor;
    }

    public void setPaintColor(int color) {
        mColor = color;
    }

    public int getPaintThickness() {
        return mWidth;
    }

    public void setPaintThickness(int thickness) {
        mWidth = thickness;
    }

    public void setEraser(boolean eraser) {
        mEraser = eraser;
    }

    public void setHelper(Helper helper) {
        mHelper = helper;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        drawStore(canvas, mPaint);

        mPaint.setColor(mEraser ? mBgColor : mColor);
        mPaint.setStrokeWidth(mWidth);
        if (mIsDot) {
            canvas.drawPoint(mX, mY, mPaint);
        } else {
            canvas.drawPath(mPath, mPaint);
        }
    }

    private boolean isLocked() {
        return mSaveTask != null;
    }

    private void touch_start(float x, float y) {
        mIsDot = true;

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mIsDot = false;
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);

        DrawInfo drawInfo = mRecycler.obtain();
        if (drawInfo == null) {
            drawInfo = new DrawInfo();
        }
        drawInfo.set(mEraser ? mBgColor : mColor, mWidth, mPath, mX, mY, mIsDot);
        DrawInfo legacy = push(drawInfo);

        // Draw legacy
        if (legacy != null) {
            legacy.draw(mCanvas, mPaint);
            mRecycler.release(legacy);
        }

        // Rest path
        mIsDot = false;
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLocked()) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        if (isLocked()) {
            return;
        }

        mCanvas.drawColor(mBgColor);
        mPath.reset();
        clearStore();
        invalidate();
    }

    public void insertBitmap(@NonNull Bitmap bitmap) {
        flush();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int width = getWidth();
        int height = getHeight();
        float bitmapScale = (float) bitmapWidth / (float) bitmapHeight;
        float scale = (float) width / (float) height;
        int outWidth;
        int outHeight;
        if (bitmapScale > scale) {
            outWidth = width;
            outHeight = (int) (outWidth / bitmapScale);
        } else {
            outHeight = height;
            outWidth = (int) (outHeight * bitmapScale);
        }

        mSrc.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        mDst.set(0, 0, outWidth, outHeight);
        mCanvas.drawBitmap(bitmap, mSrc, mDst, null);
    }

    public void flush() {
        drawStore(mCanvas, mPaint);

        mPaint.setColor(mEraser ? mBgColor : mColor);
        mPaint.setStrokeWidth(mWidth);
        if (mIsDot) {
            mCanvas.drawPoint(mX, mY, mPaint);
        } else {
            mCanvas.drawPath(mPath, mPaint);
        }

        clearStore();
    }

    public void save(@NonNull File file) {
        if (isLocked()) {
            return;
        }

        mSaveTask = new SaveTask(file);
        mSaveTask.execute();
    }

    private static final int CAPACITY = 20;

    private int mStop = 0;
    private int mSize = 0;
    private DrawInfo[] mData = new DrawInfo[CAPACITY];

    public boolean canUndo() {
        return mStop > 0;
    }

    public boolean canRedo() {
        return mStop < mSize;
    }

    public void undo() {
        if (isLocked()) {
            return;
        }

        if (mStop > 0) {
            mStop--;
            invalidate();

            if (mHelper != null) {
                mHelper.onStoreChange(this);
            }
        }
    }

    public void redo() {
        if (isLocked()) {
            return;
        }

        if (mStop < mSize) {
            mStop++;
            invalidate();

            if (mHelper != null) {
                mHelper.onStoreChange(this);
            }
        }
    }

    private void drawStore(Canvas canvas, Paint paint) {
        for (int i = 0; i < mStop; i++) {
            mData[i].draw(canvas, paint);
        }
    }

    private DrawInfo push(DrawInfo drawInfo) {
        DrawInfo[] data = mData;

        if (mStop != mSize) {
            // Release from mStop to mSize
            for (int i = mStop; i < mSize; i++) {
                mRecycler.release(data[i]);
                data[i] = null;
            }

            data[mStop] = drawInfo;

            mStop++;
            mSize = mStop;

            if (mHelper != null) {
                mHelper.onStoreChange(this);
            }

            return null;
        } else if (mSize == CAPACITY) {
            // It is Full
            DrawInfo legacy = data[0];
            System.arraycopy(data, 1, data, 0, CAPACITY - 1);
            data[CAPACITY - 1] = drawInfo;
            return legacy;
        } else {
            data[mStop] = drawInfo;
            mStop++;
            mSize++;

            if (mHelper != null) {
                mHelper.onStoreChange(this);
            }

            return null;
        }
    }

    private void clearStore() {
        DrawInfo[] data = mData;
        for (int i = 0; i < mSize; i++) {
            mRecycler.release(data[i]);
            data[i] = null;
        }
        mStop = 0;
        mSize = 0;

        if (mHelper != null) {
            mHelper.onStoreChange(this);
        }
    }

    private static class DrawInfo {
        private int mColor;
        private float mWidth;
        private Path mPath;
        private float mStartX;
        private float mStartY;
        private boolean mIsDot;

        public DrawInfo() {
            mPath = new Path();
        }

        public void set(int color, float width, Path path, float startX, float startY, boolean isDot) {
            mColor = color;
            mWidth = width;
            mPath.set(path);
            mStartX = startX;
            mStartY = startY;
            mIsDot = isDot;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(mColor);
            paint.setStrokeWidth(mWidth);
            if (mIsDot) {
                canvas.drawPoint(mStartX, mStartY, paint);
            } else {
                canvas.drawPath(mPath, paint);
            }
        }
    }

    private static class Recycler {

        private int mSize = 0;

        private Stack<DrawInfo> mStack = new Stack<>();

        @Nullable
        private DrawInfo obtain() {
            if (mSize != 0) {
                mSize--;
                return mStack.pop();
            } else {
                return null;
            }
        }

        public void release(@Nullable DrawInfo item) {
            if (item == null) {
                return;
            }

            if (mSize < CAPACITY) {
                mSize++;
                mStack.push(item);
            }
        }
    }

    public interface Helper {

        void onStoreChange(DoodleView view);

        void onSavingFinished(boolean ok);
    }

    private class SaveTask extends AsyncTask<Void, Void, Boolean> {

        private File mFile;

        public SaveTask(File file) {
            mFile = file;
        }

        @Override
        protected void onPreExecute() {
            drawStore(mCanvas, mPaint);
            clearStore();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            OutputStream os = null;
            try {
                os = new FileOutputStream(mFile);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(os);
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mSaveTask = null;
            if (mHelper != null) {
                mHelper.onSavingFinished(aBoolean);
            }
        }
    }
}
