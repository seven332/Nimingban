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

package com.hippo.nimingban.widget;

/*
 * Created by Hippo on 12/5/2016.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ac.data.ACItemUtils;
import com.hippo.nimingban.client.data.Reply;
import com.hippo.yorozuya.MathUtils;

public class MarqueeReplyView extends TextSwitcher {

    private Reply[] mReplies;
    private int mIndex;
    private boolean mCanUpdate;
    private boolean mShouldUpdate;

    private final Runnable mInvalidateTask = new Runnable() {
        @Override
        public void run() {
            mShouldUpdate = true;
            invalidate();
        }
    };

    private final ViewSwitcher.ViewFactory mReplyViewFactory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            return LayoutInflater.from(getContext()).inflate(
                    R.layout.item_list_reply, MarqueeReplyView.this, false);
        }
    };

    public MarqueeReplyView(Context context) {
        super(context);
        init();
    }

    public MarqueeReplyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setFactory(mReplyViewFactory);
    }

    private boolean canMarquee() {
        return mReplies != null && mReplies.length > 0;
    }

    private long getMarqueeInterval() {
        return MathUtils.random(3000, 5001);
    }

    private void nextMarquee() {
        stopMarquee();
        if (canMarquee()) {
            mCanUpdate = true;
            postDelayed(mInvalidateTask, getMarqueeInterval());
        }
    }

    private void stopMarquee() {
        mCanUpdate = false;
        removeCallbacks(mInvalidateTask);
    }

    public void setReplies(Reply[] replies) {
        mReplies = replies;
        mIndex = 0;
        mCanUpdate = false;
        mShouldUpdate = false;
        if (replies != null && replies.length > 0) {
            setCurrentText(ACItemUtils.updateContentText(replies[0].getNMBDisplayContent(), ((TextView)getCurrentView()).getTextColors().getDefaultColor()));
        } else {
            setCurrentText(null);
        }
        nextMarquee();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCanUpdate && mShouldUpdate && canMarquee()) {
            mIndex = (mIndex + 1) % mReplies.length;
            setText(ACItemUtils.updateContentText(mReplies[mIndex].getNMBDisplayContent(), ((TextView)getCurrentView()).getTextColors().getDefaultColor()));
            nextMarquee();
        }
        mShouldUpdate = false;
    }
}
