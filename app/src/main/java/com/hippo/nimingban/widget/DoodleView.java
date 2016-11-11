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

    @Nullable
    private Bitmap mBitmap;
    @Nullable
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private Path mPath;
    private Paint mPaint;

    @Nullable
    private Bitmap mInsertBitmap;
    private int mOffsetX;
    private int mOffsetY;

    private int mBgColor;

    private int mColor;
    private int mWidth;

    private boolean mIsDot;
    private boolean mPathDone;
    private int mPointCount;
    private float mX, mY;

    private boolean mEraser = false;

    private final Rect mDst = new Rect();

    private Recycler mRecycler;

    @Nullable
    private Helper mHelper;

    @Nullable
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
        mBitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

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

    public void setHelper(@Nullable Helper helper) {
        mHelper = helper;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        onResize(w, h);
    }

    private void onResize(int width, int height) {
        clearStore();

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        int bitmapWidth;
        int bitmapHeight;
        if (mInsertBitmap == null) {
            bitmapWidth = width;
            bitmapHeight = height;
            mOffsetX = 0;
            mOffsetY = 0;
        } else {
            int insertWidth = mInsertBitmap.getWidth();
            int insertHeight = mInsertBitmap.getHeight();
            float insertScale = (float) insertWidth / (float) insertHeight;
            float scale = (float) width / (float) height;
            if (insertScale > scale) {
                bitmapWidth = width;
                bitmapHeight = (int) (bitmapWidth / insertScale);
            } else {
                bitmapHeight = height;
                bitmapWidth = (int) (bitmapHeight * insertScale);
            }
            mOffsetX = (width - bitmapWidth) / 2;
            mOffsetY = (height - bitmapHeight) / 2;
        }

        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        if (mInsertBitmap == null) {
            mCanvas.drawColor(mBgColor);
        } else {
            mDst.set(0, 0, bitmapWidth, bitmapHeight);
            mCanvas.drawBitmap(mInsertBitmap, null, mDst, mBitmapPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }

        canvas.drawBitmap(mBitmap, mOffsetX, mOffsetY, mBitmapPaint);

        int saved = canvas.save();

        canvas.translate(mOffsetX, mOffsetY);
        canvas.clipRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

        drawStore(canvas, mPaint);

        mPaint.setColor(mEraser ? mBgColor : mColor);
        mPaint.setStrokeWidth(mWidth);
        if (mIsDot) {
            canvas.drawPoint(mX, mY, mPaint);
        } else {
            canvas.drawPath(mPath, mPaint);
        }

        canvas.restoreToCount(saved);
    }

    private boolean isLocked() {
        return mSaveTask != null;
    }

    private void motionToPath(MotionEvent event, Path path) {
        switch (event.getPointerCount()) {
            case 2: {
                final float x0 = event.getX(0) - mOffsetX;
                final float y0 = event.getY(0) - mOffsetY;
                final float x1 = event.getX(1) - mOffsetX;
                final float y1 = event.getY(1) - mOffsetY;
                path.reset();
                path.moveTo(x0, y0);
                path.lineTo(x1, y1);
                break;
            }
            case 3: {
                final float x0 = event.getX(0) - mOffsetX;
                final float y0 = event.getY(0) - mOffsetY;
                final float x1 = event.getX(1) - mOffsetX;
                final float y1 = event.getY(1) - mOffsetY;
                final float x2 = event.getX(2) - mOffsetX;
                final float y2 = event.getY(2) - mOffsetY;
                path.reset();
                path.moveTo(x0, y0);
                path.quadTo(x2, y2, x1, y1);
                break;
            }
            case 4: {
                final float x0 = event.getX(0) - mOffsetX;
                final float y0 = event.getY(0) - mOffsetY;
                final float x1 = event.getX(1) - mOffsetX;
                final float y1 = event.getY(1) - mOffsetY;
                final float x2 = event.getX(2) - mOffsetX;
                final float y2 = event.getY(2) - mOffsetY;
                final float x3 = event.getX(3) - mOffsetX;
                final float y3 = event.getY(3) - mOffsetY;
                path.reset();
                path.moveTo(x0, y0);
                path.cubicTo(x2, y2, x3, y3, x1, y1);
                break;
            }
        }
    }

    private void touch_down(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            mIsDot = true;
            final float x = event.getX() - mOffsetX;
            final float y = event.getY() - mOffsetY;
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        } else {
            mIsDot = false;
            motionToPath(event, mPath);
        }
    }

    private void touch_move(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            final float x = event.getX() - mOffsetX;
            final float y = event.getY() - mOffsetY;
            // Check mIsDot
            if (mIsDot) {
                final float dx = Math.abs(x - mX);
                final float dy = Math.abs(y - mY);
                mIsDot = dx < TOUCH_TOLERANCE && dy < TOUCH_TOLERANCE;
            }
            if (!mIsDot) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        } else {
            mIsDot = false;
            motionToPath(event, mPath);
        }
    }

    private void touch_up(int pointCount) {
        // Skip empty path
        if (mPath.isEmpty()) {
            return;
        }

        // End mPath for single finger
        if (pointCount == 1) {
            mPath.lineTo(mX, mY);
        }

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

        int pointCount = event.getPointerCount();
        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL ||
                actionMasked == MotionEvent.ACTION_POINTER_UP) {
            --pointCount;
        }
        final int oldPointCount = mPointCount;
        if (pointCount > oldPointCount ||
                event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mPathDone = false;
        }
        mPointCount = pointCount;
        if (mPathDone) {
            return true;
        }

        // If the user has drawn with finger before, not dot, save it now
        if (oldPointCount == 1 && pointCount > 1 && !mIsDot) {
            touch_up(1);
        }

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                touch_down(event);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(event);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                mPathDone = true;
                touch_up(event.getPointerCount());
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        if (isLocked() || mCanvas == null || mBitmap == null) {
            return;
        }

        if (mInsertBitmap == null) {
            mCanvas.drawColor(mBgColor);
        } else {
            mDst.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mCanvas.drawBitmap(mInsertBitmap, null, mDst, mBitmapPaint);
        }

        mPath.reset();
        clearStore();
        invalidate();
    }

    public boolean hasInsertBitmap() {
        return mInsertBitmap != null;
    }

    public void insertBitmap(@Nullable Bitmap bitmap) {
        if (mInsertBitmap != null) {
            mInsertBitmap.recycle();
        }
        mInsertBitmap = bitmap;
        onResize(getWidth(), getHeight());
        invalidate();
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
    private final DrawInfo[] mData = new DrawInfo[CAPACITY];

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
        private final Path mPath;
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

        private final Stack<DrawInfo> mStack = new Stack<>();

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

        private final File mFile;

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
            if (mBitmap == null) {
                return false;
            }

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
