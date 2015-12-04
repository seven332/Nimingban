//
// Created by Hippo on 11/28/2015.
//

#include <stdlib.h>
#include <stdbool.h>
#include <png.h>
#include <utils.h>

#include "apng.h"
#include "../log.h"

static void user_error_fn(png_structp png_ptr,
    png_const_charp error_msg)
{
  LOGE(EMSG("%s"), error_msg);
}

static void user_warn_fn(png_structp png_ptr,
    png_const_charp error_msg)
{
  LOGW(EMSG("%s"), error_msg);
}

static void read_frame(png_structp png_ptr, png_infop info_ptr, FRAME_INFO* frameInfo)
{
  unsigned int x;
  unsigned int y;
  unsigned int w;
  unsigned int h;
  unsigned short delay_num;
  unsigned short delay_den;
  unsigned char dop;
  unsigned char bop;
  unsigned int delay;
  unsigned int stride;
  unsigned char* frame_data;
  unsigned int i;

  png_read_frame_head(png_ptr, info_ptr);
  png_get_next_frame_fcTL(png_ptr, info_ptr, &w, &h, &x, &y, &delay_num, &delay_den, &dop, &bop);
  stride = w * 4;

  delay = 1000u * delay_num / delay_den;

  frameInfo->width = w;
  frameInfo->height = h;
  frameInfo->x = x;
  frameInfo->y = y;
  frameInfo->delay = delay;
  frameInfo->dop = dop;
  frameInfo->bop = bop;

  frame_data = (unsigned char*) malloc(h * stride);
  if (frame_data != NULL) {
    // Read frame data
    unsigned char* buffer_ptrs[h];
    buffer_ptrs[0] = frame_data;
    for (i = 1; i < h; i++) {
      buffer_ptrs[i] = buffer_ptrs[i - 1] + stride;
    }
    png_read_image(png_ptr, buffer_ptrs);
  }

  frameInfo->frame_data = frame_data;
}

APNG* decodeAPNG(void* io_ptr, png_rw_ptr read_data_fn)
{
  APNG *apng = NULL;
  png_structp png_ptr = NULL;
  png_infop info_ptr = NULL;
  unsigned char* buffer = NULL;
  int frame_count = 0;
  bool hide_first_frame = false;
  unsigned int width;
  unsigned int height;
  unsigned int stride;
  int color_type;
  int bit_depth;
  FRAME_INFO* frame_infos;
  FRAME_INFO* frame_info_ptr;
  int i;

  apng = (APNG *) malloc(sizeof(APNG));
  if (apng == NULL) {
    LOGE(EMSG("Out of memory"));
    return NULL;
  }

  png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, &user_error_fn, &user_warn_fn);
  if (png_ptr == NULL) {
    LOGE(EMSG("png_create_read_struct return NULL"));
    free(apng);
    apng = NULL;
    return NULL;
  }

  info_ptr = png_create_info_struct(png_ptr);
  if (info_ptr == NULL) {
    LOGE(EMSG("png_create_info_struct return NULL"));
    free(apng);
    apng = NULL;
    png_destroy_read_struct(&png_ptr, NULL, NULL);
    return NULL;
  }

  if (setjmp(png_jmpbuf(png_ptr))) {
    LOGE(EMSG("png decode error"));
    free(frame_infos);
    frame_infos = NULL;
    free(buffer);
    buffer = NULL;
    free(apng);
    apng = NULL;
    png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
    return NULL;
  }

  // Set custom read function
  png_set_read_fn(png_ptr, io_ptr, read_data_fn);

  // Get png info
  png_read_info(png_ptr, info_ptr);

  // Check apng
  if (!png_get_valid(png_ptr, info_ptr, PNG_INFO_acTL)) {
    // Wow, it is not apng
    free(apng);
    apng = NULL;
    png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
    return NULL;
  }

  // PNG info
  width = png_get_image_width(png_ptr, info_ptr);
  height = png_get_image_height(png_ptr, info_ptr);
  color_type = png_get_color_type(png_ptr, info_ptr);
  bit_depth = png_get_bit_depth(png_ptr, info_ptr);
  stride = width * 4;

  buffer = (unsigned char*) malloc(stride * height);
  if (buffer == NULL) {
    free(apng);
    apng = NULL;
    png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
    return NULL;
  }

  // Frame count
  frame_count = png_get_num_frames(png_ptr, info_ptr);
  hide_first_frame = png_get_first_frame_is_hidden(png_ptr, info_ptr);
  if (hide_first_frame) {
    frame_count--;
  }

  frame_infos = (FRAME_INFO*) malloc(frame_count * sizeof(FRAME_INFO));
  if (frame_infos == NULL) {
    free(buffer);
    buffer = NULL;
    free(apng);
    apng = NULL;
    png_destroy_read_struct(&png_ptr, &info_ptr, NULL);
    return NULL;
  }
  // Fill default value to frame info
  for (i = 0; i < frame_count; i++) {
    frame_info_ptr = frame_infos + i;
    frame_info_ptr->frame_data = NULL;
    frame_info_ptr->width = 0;
    frame_info_ptr->height = 0;
    frame_info_ptr->x = 0;
    frame_info_ptr->y = 0;
    frame_info_ptr->delay = 0;
    frame_info_ptr->bop = PNG_BLEND_OP_SOURCE;
    frame_info_ptr->dop = PNG_DISPOSE_OP_NONE;
  }

  // Configure to ARGB
  png_set_expand(png_ptr);
  if (bit_depth == 16) {
    png_set_scale_16(png_ptr);
  }
  if (color_type == PNG_COLOR_TYPE_GRAY ||
      color_type == PNG_COLOR_TYPE_GRAY_ALPHA) {
    png_set_gray_to_rgb(png_ptr);
  }
  if (!(color_type & PNG_COLOR_MASK_ALPHA)) {
    png_set_add_alpha(png_ptr, 0xff, PNG_FILLER_AFTER);
  }

  if (hide_first_frame) {
    // Skip first frame
    unsigned char* buffer_ptrs[height];
    buffer_ptrs[0] = buffer;
    for (i = 1; i < height; i++) {
      buffer_ptrs[i] = buffer_ptrs[i - 1] + stride;
    }
    png_read_image(png_ptr, buffer_ptrs);
  }
  // Get frame data
  for (i = 0; i < frame_count; i++) {
    read_frame(png_ptr, info_ptr, frame_infos + i);
  }
  // FIX dop
  if (frame_infos->dop == PNG_DISPOSE_OP_PREVIOUS) {
    frame_infos->dop = PNG_DISPOSE_OP_BACKGROUND;
  }
  frame_infos->bop = PNG_BLEND_OP_SOURCE;

  // Close
  png_read_end(png_ptr, info_ptr);
  png_destroy_read_struct(&png_ptr, &info_ptr, NULL);

  // Fill apng
  apng->width = width;
  apng->height = height;
  apng->buffer = buffer;
  apng->buffer_index = -1;
  apng->backup = NULL;
  apng->frame_count = frame_count;
  apng->frame_infos = frame_infos;

  return apng;
}

static void backup(APNG* apng)
{
  if (apng->backup == NULL) {
    apng->backup = (unsigned char*) malloc(apng->width * apng->height * 4);
    if (apng->backup == NULL) {
      return;
    }
  }

  memcpy(apng->backup, apng->buffer, apng->width * apng->height * 4);
}

static void restore(APNG* apng)
{
  if (apng->backup != NULL) {
    memcpy(apng->buffer, apng->backup, apng->width * apng->height * 4);
  }
}

static void blendOver(void* dst, const void* src, size_t len)
{
  unsigned int i;
  int u, v, al;
  const unsigned char* sp = src;
  unsigned char* dp = dst;

  for (i = 0; i < len; i += 4, sp += 4, dp += 4) {
    if (sp[3] == 255) {
      memcpy(dp, sp, 4);
    } else if (sp[3] != 0) {
      if (dp[3] != 0) {
        u = sp[3]*255;
        v = (255-sp[3])*dp[3];
        al = u + v;
        dp[0] = (unsigned char) ((sp[0]*u + dp[0]*v)/al);
        dp[1] = (unsigned char) ((sp[1]*u + dp[1]*v)/al);
        dp[2] = (unsigned char) ((sp[2]*u + dp[2]*v)/al);
        dp[3] = (unsigned char) (al/255);
      } else {
        memcpy(dp, sp, 4);
      }
    }
  }
}

static void blend(unsigned char* src, int src_width, int src_height, int offset_x, int offset_y,
    unsigned char* dst, int dst_width, int dst_height, bool source_or_over)
{
  int i;
  unsigned char* src_ptr;
  unsigned char* dst_ptr;
  size_t len;
  int copyWidth = MIN(dst_width - offset_x, src_width);
  int copyHeight = MIN(dst_height - offset_y, src_height);

  for (i = 0; i < copyHeight; i++) {
    src_ptr = src + (i * src_width * 4);
    dst_ptr = dst + (((offset_y + i) * dst_width + offset_x) * 4);
    len = (size_t) (copyWidth * 4);

    if (source_or_over) {
      blendOver(dst_ptr, src_ptr, len);
    } else {
      memcpy(dst_ptr, src_ptr, len);
    }
  }
}

void advanceAPNG(APNG* apng, void* custom, BlendOver blend_over)
{
  int index = (apng->buffer_index + 1) % apng->frame_count;
  FRAME_INFO* frame_info = apng->frame_infos + index;

  // Clear buffer if it is first frame
  if (index == 0) {
    memset(apng->buffer, '\0', apng->width * apng->height * 4);
  }

  if (frame_info->dop == PNG_DISPOSE_OP_PREVIOUS) {
    backup(apng);
  }

  blend(frame_info->frame_data, frame_info->width, frame_info->height, frame_info->x, frame_info->y,
      apng->buffer, apng->width, apng->height, frame_info->bop == PNG_BLEND_OP_OVER);

  if (blend_over != NULL) {
    blend_over(apng, custom);
  }

  switch (frame_info->dop) {
    case PNG_DISPOSE_OP_PREVIOUS:
      restore(apng);
      break;
    case PNG_DISPOSE_OP_BACKGROUND:
      memset(apng->buffer, '\0', apng->width * apng->height * 4);
      break;
    default:
    case PNG_DISPOSE_OP_NONE:
      // Nothing
      break;
  }

  apng->buffer_index = index;
}

void closeAPNG(APNG* apng)
{
  FRAME_INFO* frame_infos = NULL;
  FRAME_INFO* frame_info_ptr = NULL;
  int i;

  if (apng == NULL) {
    return;
  }

  // Free buffer
  free(apng->buffer);
  apng->buffer = NULL;

  // Free backup
  free(apng->backup);
  apng->backup = NULL;

  // Free frame
  frame_infos = apng->frame_infos;
  if (frame_infos != NULL) {
    for (i = 0; i < apng->frame_count; i++) {
      frame_info_ptr = frame_infos + i;
      free(frame_info_ptr->frame_data);
      frame_info_ptr->frame_data = NULL;
    }
    free(frame_infos);
    apng->frame_infos = NULL;
  }
}
