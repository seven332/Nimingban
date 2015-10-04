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

package com.hippo.nimingban;

import com.hippo.yorozuya.Messenger;

public final class Constants {

    public static final int MESSENGER_ID_CHANGE_THEME;
    public static final int MESSENGER_ID_UPDATE_RECORD;
    public static final int MESSENGER_ID_REPLY;
    public static final int MESSENGER_ID_CREATE_POST;
    public static final int MESSENGER_ID_FAST_SCROLLER;

    static {
        Messenger messenger = Messenger.getInstance();
        MESSENGER_ID_CHANGE_THEME = messenger.newId();
        MESSENGER_ID_UPDATE_RECORD = messenger.newId();
        MESSENGER_ID_REPLY = messenger.newId();
        MESSENGER_ID_CREATE_POST = messenger.newId();
        MESSENGER_ID_FAST_SCROLLER = messenger.newId();
    }
}
