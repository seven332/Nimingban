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

#ifndef INPUT_STREAM_PIPE
#define INPUT_STREAM_PIPE

#include <jni.h>

#include "input_stream.h"

typedef struct
{
  jobject isPipe;
  jmethodID obtainMID;
  jmethodID releaseMID;
  jmethodID openMID;
  jmethodID closeMID;
} InputStreamPipe;

InputStreamPipe* createInputStreamPipe(JNIEnv* env, jobject isPipe);
void destroyInputStreamPipe(JNIEnv* env, InputStreamPipe* inputStreamPipe);
void obtainInputStreamPipe(JNIEnv* env, InputStreamPipe* inputStreamPipe);
void releaseInputStreamPipe(JNIEnv* env, InputStreamPipe* inputStreamPipe);
InputStream* openInputStream(JNIEnv* env, InputStreamPipe* inputStreamPipe);
void closeInputStream(JNIEnv* env, InputStreamPipe* inputStreamPipe);

#endif //INPUT_STREAM_PIPE
