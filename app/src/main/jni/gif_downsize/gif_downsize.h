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

#ifndef GIF_DOWNSIZE_GIF_DOWNSIZE_H
#define GIF_DOWNSIZE_GIF_DOWNSIZE_H

#include <stdio.h>

#include "gif_lib.h"

bool compress(FILE* input_file, FILE* output_file, int sample_size);
bool compress_custom(void* input_data, InputFunc input_func,
    void* output_data, OutputFunc output_func, int sample_size);

#endif // GIF_DOWNSIZE_GIF_DOWNSIZE_H
