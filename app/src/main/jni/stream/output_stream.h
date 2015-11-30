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

//
// Created by Hippo on 10/19/2015.
//

#ifndef OUTPUT_STREAM_H
#define OUTPUT_STREAM_H

#include <jni.h>
#include <stddef.h>

typedef struct
{
  jobject os;
  jmethodID writeMID;
  jbyteArray buffer;
} OutputStream;

OutputStream* createOutputStream(JNIEnv* env, jobject os);
void destroyOutputStream(JNIEnv* env, OutputStream* outputStream);
size_t writeOutputStream(JNIEnv* env, OutputStream* outputStream, const unsigned char* buffer, int offset, size_t size);

#endif //OUTPUT_STREAM_H
