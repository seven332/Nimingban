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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.hippo.nimingban.R;
import com.hippo.nimingban.widget.GalleryPage;
import com.hippo.widget.viewpager.PagerHolder;
import com.hippo.widget.viewpager.RecyclerPagerAdapter;

// TODO show all image in post
public class GalleryActivity2 extends AppCompatActivity {

    public static final String ACTION_SINGLE_IMAGE = "com.hippo.nimingban.ui.GalleryActivity.action.SINGLE_IMAGE";

    public static final String KEY_SITE = "site";
    public static final String KEY_ID = "id";
    public static final String KEY_IMAGE = "image";

    private ViewPager mViewPager;
    private GalleryAdapter mGalleryAdapter;

    private boolean handlerIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (ACTION_SINGLE_IMAGE.equals(action)) {
            int site = intent.getIntExtra(KEY_SITE, -1);
            String id = intent.getStringExtra(KEY_ID);
            String image = intent.getStringExtra(KEY_IMAGE);
            if (site != -1 && id != null && image != null) {
                mGalleryAdapter = new SingleImageAdapter(site, id, image);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!handlerIntent(getIntent())) {
            finish();
            return;
        }

        setContentView(R.layout.activity_gallery_2);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(mGalleryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unload all pager
        for (int i = 0, n = mViewPager.getChildCount(); i < n; i++) {
            View child = mViewPager.getChildAt(i);
            if (child instanceof GalleryPage) {
                ((GalleryPage) child).unload();
            }
        }
    }

    private class GalleryHolder extends PagerHolder {

        public GalleryPage galleryPage;

        public GalleryHolder(View itemView) {
            super(itemView);

            galleryPage = (GalleryPage) itemView;
        }
    }

    private abstract class GalleryAdapter extends RecyclerPagerAdapter<GalleryHolder> {

        @NonNull
        @Override
        public GalleryHolder createPagerHolder(ViewGroup container) {

            return new GalleryHolder(GalleryActivity2.this.getLayoutInflater()
                    .inflate(R.layout.item_gallery, container, false));
        }

        public abstract void saveCurrentImage();
    }

    private class SingleImageAdapter extends GalleryAdapter {

        private int mSite;
        private String mId;
        private String mImage;

        public SingleImageAdapter(int site, String id, String image) {
            mSite = site;
            mId = id;
            mImage = image;
        }

        @Override
        public void bindPagerHolder(GalleryHolder holder, int position) {
            holder.galleryPage.load(mId, mImage);
        }

        @Override
        public void unbindPagerHolder(GalleryHolder holder, int position) {
            holder.galleryPage.unload();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public void saveCurrentImage() {
            // TODO
        }
    }
}
