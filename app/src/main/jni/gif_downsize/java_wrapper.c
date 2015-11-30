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
// Created by Hippo on 11/15/2015.
//

#include <jni.h>

#include "gif_downsize.h"
#include "input_stream.h"
#include "output_stream.h"
#include "../log.h"

static JavaVM *g_jvm;

static JNIEnv *getEnv()
{
  JNIEnv *env;

  if ((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) == JNI_OK) {
    return env;
  } else {
    return NULL;
  }
}

static int streamInputFunc(GifFileType* gif, GifByteType* bytes, int size) {
  InputStream* stream = gif->UserData;
  JNIEnv *env = getEnv();

  if (env == NULL) {
    LOGE(EMSG("Can't get JNIEnv"));
    return 0;
  }

  return readInputStream(env, stream, bytes, 0, (size_t) size);
}

static int streamOutputFunc(GifFileType* gif, const GifByteType* bytes, int size)
{
  OutputStream* stream = gif->UserData;
  JNIEnv *env = getEnv();

  if (env == NULL) {
    LOGE(EMSG("Can't get JNIEnv"));
    return 0;
  }

  return writeOutputStream(env, stream, bytes, 0, (size_t) size);
}

JNIEXPORT jboolean JNICALL
Java_com_hippo_gif_GifDownloadSize_nativeCompress
    (JNIEnv* env, jclass clazz, jstring in_path, jstring out_path, jint samlpe_size)
{
  const char *native_in_path = (*env)->GetStringUTFChars(env, in_path, 0);
  const char *native_out_path = (*env)->GetStringUTFChars(env, out_path, 0);
  FILE* in = fopen(native_in_path, "rb");
  FILE* out = fopen(native_out_path, "wb");
  jboolean result = JNI_FALSE;

  if (in != NULL && out != NULL) {
    result = compress(in, out, samlpe_size);
  }

  if (in != NULL) {
    fclose(in);
    in = NULL;
  }
  if (out != NULL) {
    fclose(out);
    out = NULL;
  }
  (*env)->ReleaseStringUTFChars(env, in_path, native_in_path);
  (*env)->ReleaseStringUTFChars(env, out_path, native_out_path);

  return result;
}

JNIEXPORT jboolean JNICALL
Java_com_hippo_gif_GifDownloadSize_nativeCompressCustom
    (JNIEnv* env, jclass clazz, jobject is, jobject os, jint samlpe_size)
{
  InputStream* inputStream = createInputStream(env, is);
  OutputStream* outputStream = createOutputStream(env, os);
  if (inputStream == NULL || outputStream == NULL) {
    destroyInputStream(env, inputStream);
    destroyOutputStream(env, outputStream);
  }

  return (jboolean) compress_custom(inputStream, &streamInputFunc, outputStream, &streamOutputFunc, samlpe_size);
}

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
  JNIEnv* env;
  if ((*vm)->GetEnv(vm, (void**) (&env), JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }
  g_jvm = vm;
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {}
