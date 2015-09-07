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
import com.hippo.widget.recyclerview.EasyRecyclerView;
import com.hippo.yorozuya.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

public final class RightDrawer extends EasyRecyclerView implements EasyRecyclerView.OnItemClickListener {

    private ForumAdapter mAdapter;

    private List<Forum> mForums;

    private OnSelectForumListener mOnSelectForumListener;

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
        // TODO add tip for empty list
        mForums.clear();
        mForums.addAll(forums);
        mAdapter.notifyDataSetChanged();
    }

    public void setOnSelectForumListener(OnSelectForumListener listener) {
        mOnSelectForumListener = listener;
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        if (mOnSelectForumListener != null) {
            mOnSelectForumListener.onSelectForum(mForums.get(position));
        }
        return true;
    }

    private class ForumHolder extends RecyclerView.ViewHolder {

        public ForumHolder(View itemView) {
            super(itemView);
        }
    }

    private class ForumAdapter extends RecyclerView.Adapter<ForumHolder> {

        @Override
        public ForumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ForumHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_forum_drawer, parent, false));
        }

        @Override
        public void onBindViewHolder(ForumHolder holder, int position) {
            ((TextView) holder.itemView).setText(mForums.get(position).getNMBDisplayname());
        }

        @Override
        public int getItemCount() {
            return mForums.size();
        }
    }

    public interface OnSelectForumListener {
        void onSelectForum(Forum forum);
    }
}
