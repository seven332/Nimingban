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

package com.hippo.nimingban.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.hippo.app.ProgressDialogBuilder;
import com.hippo.io.UriInputStreamPipe;
import com.hippo.nimingban.NMBAppConfig;
import com.hippo.nimingban.R;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.ReadableTime;
import com.hippo.nimingban.widget.ColorPickerView;
import com.hippo.nimingban.widget.DoodleView;
import com.hippo.nimingban.widget.ThicknessPreviewView;
import com.hippo.rippleold.RippleSalon;
import com.hippo.util.AnimationUtils;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.SimpleImageView;
import com.hippo.widget.Slider;
import com.hippo.yorozuya.LayoutUtils;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.SimpleHandler;

import java.io.File;

public final class DoodleActivity extends AbsActivity implements View.OnClickListener, DoodleView.Helper {

    public static final int REQUEST_CODE_SELECT_IMAGE = 0;

    private DoodleView mDoodleView;

    private View mSide;
    private View mPalette;
    private View mThickness;
    private View mDrawAction;
    private View mImage;
    private View mUndo;
    private View mRedo;
    private View mClear;
    private View mOk;
    private SimpleImageView mMenu;

    private File mOutputFile;

    private Dialog mExitWaitingDialog;

    private ValueAnimator mSideAnimator;
    private boolean mShowSide = true;
    private Runnable mHideSideRunnable;

    @Override
    protected int getLightThemeResId() {
        return R.style.AppTheme_NoActionBar;
    }

    @Override
    protected int getDarkThemeResId() {
        return R.style.AppTheme_Dark_NoActionBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File dir = NMBAppConfig.getDoodleDir();
        if (dir != null) {
            String filename = "doodle-" + ReadableTime.getFilenamableTime(System.currentTimeMillis()) + ".png";
            mOutputFile = new File(dir, filename);
        }

        setContentView(R.layout.activity_doodle);

        mDoodleView = (DoodleView) findViewById(R.id.doodle_view);
        mSide = findViewById(R.id.side);
        mPalette = findViewById(R.id.palette);
        mThickness = findViewById(R.id.thickness);
        mDrawAction = findViewById(R.id.draw_action);
        mImage = findViewById(R.id.image);
        mUndo = findViewById(R.id.undo);
        mRedo = findViewById(R.id.redo);
        mClear = findViewById(R.id.clear);
        mOk = findViewById(R.id.ok);
        mMenu = (SimpleImageView) findViewById(R.id.menu);

        mDoodleView.setHelper(this);

        RippleSalon.addRipple(mPalette, true);
        RippleSalon.addRipple(mThickness, true);
        RippleSalon.addRipple(mDrawAction, true);
        RippleSalon.addRipple(mImage, true);
        RippleSalon.addRipple(mUndo, true);
        RippleSalon.addRipple(mRedo, true);
        RippleSalon.addRipple(mClear, true);
        RippleSalon.addRipple(mOk, true);
        RippleSalon.addRipple(mMenu, true);

        mSide.setOnClickListener(this);
        mPalette.setOnClickListener(this);
        mThickness.setOnClickListener(this);
        mDrawAction.setOnClickListener(this);
        mImage.setOnClickListener(this);
        mUndo.setOnClickListener(this);
        mRedo.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mOk.setOnClickListener(this);
        mMenu.setOnClickListener(this);

        int color = ResourcesUtils.getAttrColor(this, R.attr.colorPureInverse) & 0x00ffffff | 0x38000000;
        VectorDrawable menuDrawable = VectorDrawable.create(this, R.drawable.ic_menu);
        ((VectorDrawable.VFullPath) menuDrawable.getTargetByName("menu")).setFillColor(color);
        mMenu.setDrawable(menuDrawable);

        updateUndoRedo();

        if (mOutputFile == null) {
            Toast.makeText(this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
        }

        mSideAnimator = new ValueAnimator();
        mSideAnimator.setDuration(300);
        mSideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSide.setTranslationX((Float) animation.getAnimatedValue());
            }
        });

        mHideSideRunnable = new Runnable() {
            @Override
            public void run() {
                hideSide();
            }
        };
        SimpleHandler.getInstance().postDelayed(mHideSideRunnable, 3000);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mOutputFile != null) {
                        saveDoodle();
                    } else {
                        Toast.makeText(DoodleActivity.this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                    finish();
                }
            }
        };

        new AlertDialog.Builder(this).setMessage(R.string.save_doodle)
                .setPositiveButton(R.string.save, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.dont_save, listener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            try {
                Bitmap bitmap = BitmapUtils.decodeStream(new UriInputStreamPipe(this, uri),
                        mDoodleView.getWidth(), mDoodleView.getHeight());
                if (bitmap != null) {
                    mDoodleView.insertBitmap(bitmap);
                }
            } catch (OutOfMemoryError e) {
                // Ignore
            }
        }
    }

    private void showSide() {
        if (mHideSideRunnable != null) {
            SimpleHandler.getInstance().removeCallbacks(mHideSideRunnable);
            mHideSideRunnable = null;
        }

        if (!mShowSide) {
            mShowSide = true;
            mSideAnimator.cancel();

            mSideAnimator.setInterpolator(AnimationUtils.FAST_SLOW_INTERPOLATOR);
            mSideAnimator.setFloatValues(mSide.getTranslationX(), 0);
            mSideAnimator.start();
        }
    }

    private void hideSide() {
        if (mHideSideRunnable != null) {
            SimpleHandler.getInstance().removeCallbacks(mHideSideRunnable);
            mHideSideRunnable = null;
        }

        if (mShowSide) {
            mShowSide = false;
            mSideAnimator.cancel();

            mSideAnimator.setInterpolator(AnimationUtils.SLOW_FAST_INTERPOLATOR);
            mSideAnimator.setFloatValues(mSide.getTranslationX(), -mSide.getWidth());
            mSideAnimator.start();
        }
    }

    private void updateUndoRedo() {
        mUndo.setEnabled(mDoodleView.canUndo());
        mRedo.setEnabled(mDoodleView.canRedo());
    }

    private class PickColorDialogHelper implements DialogInterface.OnClickListener {

        private ColorPickerView mView;

        public PickColorDialogHelper() {
            mView = new ColorPickerView(DoodleActivity.this);
            mView.setColor(mDoodleView.getPaintColor());
        }

        public View getView() {
            return mView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                mDoodleView.setPaintColor(mView.getColor());
            }
        }
    }

    private void showPickColorDialog() {
        PickColorDialogHelper helper = new PickColorDialogHelper();
        new AlertDialog.Builder(DoodleActivity.this)
                .setView(helper.getView())
                .setPositiveButton(android.R.string.ok, helper)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class ThicknessDialogHelper implements DialogInterface.OnClickListener, Slider.OnSetProgressListener {

        private View mView;
        private ThicknessPreviewView mTpv;
        private Slider mSlider;

        @SuppressLint("InflateParams")
        public ThicknessDialogHelper() {
            mView = getLayoutInflater().inflate(R.layout.dialog_thickness, null);
            mTpv = (ThicknessPreviewView) mView.findViewById(R.id.thickness_preview_view);
            mSlider = (Slider) mView.findViewById(R.id.slider);

            mTpv.setThickness(mDoodleView.getPaintThickness());
            mTpv.setColor(mDoodleView.getPaintColor());
            mSlider.setProgress((int) LayoutUtils.pix2dp(DoodleActivity.this, mDoodleView.getPaintThickness()));
            mSlider.setOnSetProgressListener(this);
        }

        public View getView() {
            return mView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                mDoodleView.setPaintThickness(LayoutUtils.dp2pix(DoodleActivity.this, mSlider.getProgress()));
            }
        }

        @Override
        public void onSetProgress(Slider slider, int newProgress, int oldProgress, boolean byUser, boolean confirm) {
            mTpv.setThickness(LayoutUtils.dp2pix(DoodleActivity.this, newProgress));
        }
    }

    private void showThicknessDialog() {
        ThicknessDialogHelper helper = new ThicknessDialogHelper();
        new AlertDialog.Builder(DoodleActivity.this)
                .setView(helper.getView())
                .setPositiveButton(android.R.string.ok, helper)
                .show();
    }

    private void saveDoodle() {
        if (mExitWaitingDialog == null) {
            mExitWaitingDialog = new ProgressDialogBuilder(this)
                    .setTitle(R.string.please_wait)
                    .setMessage(R.string.saving)
                    .setCancelable(false)
                    .show();
            mDoodleView.save(mOutputFile);
        } else {
            // Wait here, it might be fast click back button
        }
    }

    @Override
    public void onClick(View v) {
        if (mSide == v) {
            hideSide();
        } else if (mPalette == v) {
            showPickColorDialog();
        } else if (mThickness == v) {
            showThicknessDialog();
        } else if (mDrawAction == v) {
            boolean newActivated = !mDrawAction.isActivated();
            mDrawAction.setActivated(newActivated);
            mDoodleView.setEraser(newActivated);
        } else if (mImage == v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.select_picture)), REQUEST_CODE_SELECT_IMAGE);
        } else if (mUndo == v) {
            mDoodleView.undo();
        } else if (mRedo == v) {
            mDoodleView.redo();
        } else if (mClear == v) {
            mDoodleView.clear();
        } else if (mOk == v) {
            if (mOutputFile != null) {
                saveDoodle();
            } else {
                Toast.makeText(this, R.string.cant_create_image_file, Toast.LENGTH_SHORT).show();
            }
        } else if (mMenu == v) {
            if (mShowSide) {
                hideSide();
            } else {
                showSide();
            }
        }
    }

    @Override
    public void onStoreChange(DoodleView view) {
        updateUndoRedo();
    }

    @Override
    public void onSavingFinished(boolean ok) {
        if (mExitWaitingDialog != null) {
            mExitWaitingDialog.dismiss();
            mExitWaitingDialog = null;
        }
        Intent intent = new Intent();
        intent.setData(Uri.fromFile(mOutputFile));
        setResult(RESULT_OK, intent);
        finish();
    }

    /*
    public static class DoodleLayout extends FrameLayout {

        public DoodleLayout(Context context) {
            super(context);
        }

        public DoodleLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public DoodleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int count = getChildCount();

            final int parentLeft = getPaddingLeft();
            final int parentRight = right - left - getPaddingRight();

            final int parentTop = getPaddingTop();
            final int parentBottom = bottom - top - getPaddingBottom();

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.START;
                    }

                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                    switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                    lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = parentLeft + lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = parentTop + lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                    lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = parentBottom - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = parentTop + lp.topMargin;
                    }

                    int offsetX = - (int) (lp.percent * width);
                    child.layout(childLeft + offsetX, childTop, childLeft + width, childTop + height);
                }
            }
        }

        @Override
        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new LayoutParams(getContext(), attrs);
        }

        @Override
        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return p instanceof LayoutParams;
        }

        @Override
        protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
            return new LayoutParams(p);
        }

        public static class LayoutParams extends FrameLayout.LayoutParams {

            public float percent = 0.0f;

            public LayoutParams(Context c, AttributeSet attrs) {
                super(c, attrs);
            }

            public LayoutParams(ViewGroup.LayoutParams source) {
                super(source);
            }
        }
    }
    */
}
