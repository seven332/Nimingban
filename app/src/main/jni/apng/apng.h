//
// Created by Hippo on 11/28/2015.
//

#ifndef APNG_H
#define APNG_H

typedef struct {
  void* frame_data;
  unsigned int x;
  unsigned int y;
  unsigned int width;
  unsigned int height;
  unsigned int delay; // ms
  unsigned char dop;
  unsigned char bop;
} FRAME_INFO;

typedef struct {
  unsigned int width;
  unsigned int height;
  unsigned char* buffer;
  int buffer_index;
  unsigned char* backup;
  int frame_count;
  FRAME_INFO* frame_infos;
} APNG;

typedef void (*BlendOver) (APNG*, void*);

APNG* decodeAPNG(void* io_ptr, png_rw_ptr read_data_fn);
void advanceAPNG(APNG* apng, void* custom, BlendOver blend_over);
void closeAPNG(APNG* apng);

#endif //APNG_H
