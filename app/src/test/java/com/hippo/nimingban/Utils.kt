/*
 * Copyright 2017 Hippo Seven
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

package com.hippo.nimingban

import com.hippo.nimingban.client.data.Forum
import com.hippo.nimingban.client.data.ForumGroup
import java.math.BigInteger
import java.security.SecureRandom

/*
 * Created by Hippo on 6/17/2017.
 */

private val RANDOM = SecureRandom()

fun randomString() = BigInteger(128, RANDOM).toString(32)!!

fun randomForum() = Forum(
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    randomString())
    .also { it.init }

fun randomOfficeForum() = randomForum().also { it.official = true }

fun randomCustomForum() = randomForum()

fun randomForumGroup(vararg forums: Forum) = ForumGroup(
    randomString(),
    randomString(),
    randomString(),
    randomString(),
    forums.asList())
    .also { it.init }
