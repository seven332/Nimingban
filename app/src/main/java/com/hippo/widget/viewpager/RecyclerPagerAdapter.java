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

package com.hippo.widget.viewpager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class RecyclerPagerAdapter<E extends PagerHolder> extends PagerAdapter {

    static final int INVALID_POSITION = -1;

    private Set<E> mAttachedHolder = new HashSet<>();

    private Recycler mRecycler = new Recycler();

    @NonNull
    public abstract E createPagerHolder(ViewGroup container);

    public abstract void bindPagerHolder(E holder, int position);

    public abstract void unbindPagerHolder(E holder, int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        E holder = mRecycler.obtain();
        if (holder == null) {
            holder = createPagerHolder(container);
        }

        holder.oldPosition = INVALID_POSITION;
        holder.position = position;

        bindPagerHolder(holder, position);
        container.addView(holder.itemView);

        mAttachedHolder.add(holder);

        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //noinspection unchecked
        E holder = (E) object;
        container.removeView(holder.itemView);
        unbindPagerHolder(holder, position);

        holder.oldPosition = INVALID_POSITION;
        holder.position = INVALID_POSITION;
        mAttachedHolder.remove(holder);

        mRecycler.release(holder);
    }

    @Override
    public void notifyDataSetChanged() {
        // Invalid all attached holder
        for (E holder : mAttachedHolder) {
            holder.oldPosition = holder.position;
            holder.position = INVALID_POSITION;
        }
        super.notifyDataSetChanged();
    }

    public final void notifyItemChanged(int position) {
        notifyItemRangeChanged(position, 1);
    }

    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
        for (E holder : mAttachedHolder) {
            int position = holder.position;
            holder.oldPosition = position;
            int positionEnd = positionStart + itemCount;
            if (position == INVALID_POSITION) {
                // WTF ?
                Log.e("TAG", "In notifyItemMoved, a attached hold postion is " + INVALID_POSITION);
                holder.position = INVALID_POSITION;
            } else if (position >= positionStart && position < positionEnd) {
                holder.position = INVALID_POSITION;
            }
        }
        super.notifyDataSetChanged();
    }

    public final void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
        for (E holder : mAttachedHolder) {
            int position = holder.position;
            holder.oldPosition = position;
            if (position == INVALID_POSITION) {
                // WTF ?
                Log.e("TAG", "In notifyItemRangeChanged, a attached hold postion is " + INVALID_POSITION);
                holder.position = INVALID_POSITION;
            } else if (position >= positionStart) {
                holder.position = position + itemCount;
            }
        }
        super.notifyDataSetChanged();
    }

    public final void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        for (E holder : mAttachedHolder) {
            int position = holder.position;
            holder.oldPosition = position;
            int positionEnd = positionStart + itemCount;
            if (position == INVALID_POSITION) {
                // WTF ?
                Log.e("TAG", "In notifyItemMoved, a attached hold postion is " + INVALID_POSITION);
                holder.position = INVALID_POSITION;
            } else if (position >= positionStart && position < positionEnd) {
                holder.position = INVALID_POSITION;
            } else if (position >= positionEnd) {
                holder.position = position - itemCount;
            }
        }
        super.notifyDataSetChanged();
    }

    public final void notifyItemMoved(int fromPosition, int toPosition) {
        for (E holder : mAttachedHolder) {
            int position = holder.position;
            holder.oldPosition = position;
            if (position == INVALID_POSITION) {
                // WTF ?
                Log.e("TAG", "In notifyItemMoved, a attached hold postion is " + INVALID_POSITION);
                holder.position = INVALID_POSITION;
            } else if (position == fromPosition) {
                holder.position = toPosition;
            } else if (position == toPosition) {
                holder.position = fromPosition;
            }
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        //noinspection unchecked
        E holder = (E) object;
        int oldPosition = holder.oldPosition;
        int position = holder.position;
        if (oldPosition == INVALID_POSITION || position == INVALID_POSITION) {
            return POSITION_NONE;
        } else if (oldPosition == position) {
            return POSITION_UNCHANGED;
        } else {
            return position;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        //noinspection unchecked
        return view == ((E) object).itemView;
    }

    private class Recycler {

        private int mSize = 0;

        private Stack<E> mStack = new Stack<>();

        @Nullable
        private E obtain() {
            if (mSize != 0) {
                mSize--;
                return mStack.pop();
            } else {
                return null;
            }
        }

        public void release(@Nullable E page) {
            if (page == null) {
                return;
            }

            if (mSize < 3) { // 3 is max size
                mSize++;
                mStack.push(page);
            }
        }
    }
}
