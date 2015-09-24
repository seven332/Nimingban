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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hippo.nimingban.R;
import com.hippo.nimingban.client.data.Forum;
import com.hippo.rippleold.RippleSalon;
import com.hippo.vector.VectorDrawable;
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

public final class RightDrawer extends EasyRecyclerView implements EasyRecyclerView.OnItemClickListener {

    private static final Object COMMOM_POSTS = new Object();

    private ForumAdapter mAdapter;

    private List<Object> mForums;

    private RightDrawerHelper mRightDrawerHelper;

    public RightDrawer(Context context) {
        super(context);
        init(context);
    }

    public RightDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RightDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mForums = new ArrayList<>();
        mAdapter = new ForumAdapter();
        setAdapter(mAdapter);
        setLayoutManager(new LinearLayoutManager(context));
        setOnItemClickListener(this);
        setSelector(RippleSalon.generateRippleDrawable(
                ResourcesUtils.getAttrBoolean(context, R.attr.dark)));
    }

    public void setForums(List<? extends Forum> forums) {
        mForums.clear();
        mForums.add(COMMOM_POSTS);
        mForums.addAll(forums);
        mAdapter.notifyDataSetChanged();
    }

    public void setRightDrawerHelper(RightDrawerHelper listener) {
        mRightDrawerHelper = listener;
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        if (mRightDrawerHelper != null) {
            Object data = mForums.get(position);
            if (data instanceof Forum) {
                mRightDrawerHelper.onSelectForum((Forum) data);
            } else {
                mRightDrawerHelper.onClickCommonPosts();
            }
        }

        return true;
    }

    private class ForumHolder extends RecyclerView.ViewHolder {

        public ForumHolder(View itemView) {
            super(itemView);
        }
    }

    private static final int TYPE_COMMOM_POSTS = 0;
    private static final int TYPE_FORUM = 1;

    private class ForumAdapter extends RecyclerView.Adapter<ForumHolder> {

        @Override
        public ForumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_COMMOM_POSTS) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_commom_posts, parent, false);
                Drawable tag = VectorDrawable.create(getContext(), R.drawable.ic_tag);
                tag.setBounds(0, 0, tag.getIntrinsicWidth(), tag.getIntrinsicHeight());
                TextView tv = (TextView) view;
                tv.setText(R.string.common_posts);
                tv.setCompoundDrawables(tag, null, null, null);
            } else {
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_forum_drawer, parent, false);
            }
            return new ForumHolder(view);
        }

        @Override
        public void onBindViewHolder(ForumHolder holder, int position) {
            Object data = mForums.get(position);
            if (data instanceof Forum && getItemViewType(position) == TYPE_FORUM) {
                ((TextView) holder.itemView).setText(((Forum) data).getNMBDisplayname());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_COMMOM_POSTS;
            } else {
                return TYPE_FORUM;
            }
        }

        @Override
        public int getItemCount() {
            return mForums.size();
        }
    }

    public interface RightDrawerHelper {

        void onClickCommonPosts();

        void onSelectForum(Forum forum);
    }
}
