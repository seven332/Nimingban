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
// Created by Hippo on 11/14/2015.
//

#include <stdio.h>
#include <stdlib.h>

#include "gif_downsize.h"
#include "../log.h"
#include "utils.h"

static int error_code = 0;

static int fileInputFunc(GifFileType* gif, GifByteType* bytes, int size)
{
  FILE* file = gif->UserData;
  return fread(bytes, 1, (size_t) size, file);
}

static int fileOutputFunc(GifFileType* gif, const GifByteType* bytes, int size)
{
  FILE* file = gif->UserData;
  return fwrite(bytes, 1, (size_t) size, file);
}

static bool compress_image(SavedImage* in_image, SavedImage* out_image, int sample_size)
{
  int in_left = in_image->ImageDesc.Left;
  int in_top = in_image->ImageDesc.Top;
  int in_width = in_image->ImageDesc.Width;
  int in_height = in_image->ImageDesc.Height;
  int offset_x = (in_left + sample_size - 1) / sample_size;
  int offset_y = (in_top + sample_size - 1) / sample_size;
  int in_width_offset = offset_x * sample_size - in_left;
  int in_height_offset = offset_y * sample_size - in_top;
  int width = MAX(0, (in_width - in_width_offset) / sample_size);
  int height = MAX(0, (in_height - in_height_offset) / sample_size);
  int i;
  int j;
  GifByteType* src;
  GifByteType* dst;

  if (width == 0 || height == 0) {
    offset_x = 0;
    offset_y = 0;
  }

  out_image->ExtensionBlockCount = in_image->ExtensionBlockCount;
  out_image->ExtensionBlocks = in_image->ExtensionBlocks;
  out_image->ImageDesc.Left = offset_x;
  out_image->ImageDesc.Top = offset_y;
  out_image->ImageDesc.Width = width;
  out_image->ImageDesc.Height = height;
  out_image->ImageDesc.Interlace = in_image->ImageDesc.Interlace;
  out_image->ImageDesc.ColorMap = in_image->ImageDesc.ColorMap;

  // Avoid free twice
  in_image->ExtensionBlockCount = 0;
  in_image->ExtensionBlocks = NULL;
  in_image->ImageDesc.ColorMap = NULL;

  if (width == 0 || height == 0) {
    out_image->RasterBits = NULL;
    return true;
  }

  out_image->RasterBits = (GifByteType*) malloc(width * height * sizeof(GifByteType));
  if (out_image->RasterBits == NULL) {
    LOGW(EMSG("Out of memory!"));
    return false;
  }

  dst = out_image->RasterBits;
  for (i = 0; i < height; i++) {
    src = in_image->RasterBits + (i * sample_size + in_height_offset) * in_width + in_width_offset;
    for (j = 0; j < width; j++) {
      *dst = *src;
      src += sample_size;
      dst++;
    }
  }

  return true;
}

static bool do_compress(GifFileType* input_gif, GifFileType* output_gif, int sample_size)
{
  int in_width = input_gif->SWidth;
  int in_height = input_gif->SHeight;
  int ss = MIN(sample_size, MIN(in_width, in_height));
  int image_count = input_gif->ImageCount;
  int i;
  output_gif->SWidth = in_width / ss;
  output_gif->SHeight = in_height / ss;
  output_gif->SColorResolution = input_gif->SColorResolution;
  output_gif->SBackGroundColor = input_gif->SBackGroundColor;
  output_gif->AspectByte = input_gif->AspectByte;
  output_gif->SColorMap = input_gif->SColorMap;
  output_gif->ImageCount = image_count;
  output_gif->ExtensionBlockCount = input_gif->ExtensionBlockCount;
  output_gif->ExtensionBlocks = input_gif->ExtensionBlocks;
  output_gif->SavedImages = (SavedImage *) malloc(image_count * sizeof(SavedImage));
  memset(output_gif->SavedImages, '\0', image_count * sizeof(SavedImage));

  // Avoid free twice
  input_gif->SColorMap = NULL;
  input_gif->ExtensionBlockCount = 0;
  input_gif->ExtensionBlocks = NULL;

  if (output_gif->SavedImages != NULL) {
    for (i = 0; i < image_count; i++) {
      if (!compress_image(input_gif->SavedImages + i, output_gif->SavedImages + i, ss)) {
        return false;
      }
    }
    return true;
  } else {
    LOGW(EMSG("Out of memory!"));
    return false;
  }
}

bool compress(FILE* input_file, FILE* output_file, int sample_size)
{
  return compress_custom(input_file, &fileInputFunc, output_file, &fileOutputFunc, sample_size);
}

bool compress_custom(void* input_data, InputFunc input_func,
    void* output_data, OutputFunc output_func, int sample_size)
{
  GifFileType* input_gif = NULL;
  GifFileType* output_gif = NULL;
  int result = GIF_ERROR;

  // Check sample
  if (sample_size <= 0) {
    return false;
  }

  input_gif = DGifOpen(input_data, input_func, &error_code);
  if (input_gif == NULL) {
    LOGE(EMSG("Can't open input gif"));
    return false;
  }

  DGifSlurp(input_gif);

  if (input_gif->ImageCount == 0) {
    LOGE(EMSG("Gif frame count is 0"));
    DGifCloseFile(input_gif, &error_code);
    return false;
  }

  // Save gif
  output_gif = EGifOpen(output_data, output_func, &error_code);
  if (output_gif == NULL) {
    LOGE(EMSG("Can't open output gif"));
    DGifCloseFile(input_gif, &error_code);
    return false;
  }

  if (do_compress(input_gif, output_gif, sample_size)) {
    result = EGifSpew(output_gif);
  }

  // Free
  GifFreeExtensions(&output_gif->ExtensionBlockCount, &output_gif->ExtensionBlocks);
  if (output_gif->SavedImages) {
    GifFreeSavedImages(output_gif);
    output_gif->SavedImages = NULL;
  }

  // Close gif
  DGifCloseFile(input_gif, &error_code);

  return result == GIF_OK;
}
