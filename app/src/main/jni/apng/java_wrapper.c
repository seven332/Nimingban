//
// Created by Hippo on 11/28/2015.
//


#include <jni.h>
#include <stdlib.h>
#include <android/bitmap.h>

#include <input_stream.h>
#include <png.h>

#include "../log.h"
#include "apng.h"

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

static void stream_read_data(png_structp png_ptr, png_bytep data, png_size_t length)
{
  InputStream* stream = png_get_io_ptr(png_ptr);
  JNIEnv *env = getEnv();

  if (env == NULL) {
    LOGE(EMSG("Can't get JNIEnv"));
  }

  readInputStream(env, stream, data, 0, length);
}


static jobject createAPngDrawable(JNIEnv* env, APNG* ptr, int width, int height, int frameCount)
{
  jclass clazz;
  jmethodID constructor;

  clazz = (*env)->FindClass(env, "com/hippo/drawable/APngDrawable");
  constructor = (*env)->GetMethodID(env, clazz, "<init>", "(JIII)V");

  if (constructor == NULL) {
    LOGE(EMSG("Can't find APngDrawable constructor"));
    return NULL;
  } else {
    return (*env)->NewObject(env, clazz, constructor,
        (jlong) (uintptr_t) ptr, (jint) width, (jint) height, (jint) frameCount);
  }
}

static void copyToBitmap(APNG* apng, void* ptr)
{
  void *pixels = NULL;
  JNIEnv *env = getEnv();
  if (env == NULL) {
    LOGE(EMSG("Can't get JNIEnv"));
  }

  AndroidBitmap_lockPixels(env, (jobject) ptr, &pixels);
  memcpy(pixels, apng->buffer, apng->width * apng->height * 4);
  AndroidBitmap_unlockPixels(env, (jobject) ptr);
}

JNIEXPORT jobject JNICALL
Java_com_hippo_drawable_APngDrawable_nativeDecode
    (JNIEnv* env, jclass clazz, jobject is)
{
  APNG* apng = NULL;
  jobject obj = NULL;
  InputStream* input_stream = createInputStream(env, is);
  if (input_stream == NULL) {
    return NULL;
  }

  apng = decodeAPNG(input_stream, &stream_read_data);
  if (apng == NULL) {
    return NULL;
  }

  obj = createAPngDrawable(env, apng, apng->width, apng->height, apng->frame_count);
  if (obj == NULL) {
    closeAPNG(apng);
    apng = NULL;
    return NULL;
  } else {
    return obj;
  }
}

JNIEXPORT void JNICALL
Java_com_hippo_drawable_APngDrawable_nativeNext
    (JNIEnv* env, jclass clazz, jlong native_ptr, jobject bitmap)
{
  advanceAPNG((APNG *) (intptr_t) native_ptr, bitmap, &copyToBitmap);
}

JNIEXPORT jint JNICALL
Java_com_hippo_drawable_APngDrawable_nativeCurrentDelay
    (JNIEnv* env, jclass clazz, jlong native_ptr)
{
  APNG* apng = (APNG *) (intptr_t) native_ptr;
  int index = apng->buffer_index;
  if (index < 0 || index >= apng->frame_count) {
    return -1;
  } else {
    return apng->frame_infos[index].delay;
  }
}

JNIEXPORT void JNICALL
Java_com_hippo_drawable_APngDrawable_nativeRecycle
    (JNIEnv* env, jclass clazz, jlong native_ptr)
{
  closeAPNG((APNG *) (intptr_t) native_ptr);
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
