/***************************************************************************************
* Copyright (c) 2014-2024 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <common.h>
#include <device/map.h>
#include <SDL2/SDL.h>

enum {
  reg_freq,
  reg_channels,
  reg_samples,
  reg_sbuf_size,
  reg_init,
  reg_count,
  // reg_add_count,
  nr_reg
};

static uint8_t *sbuf = NULL;
static uint32_t *audio_base = NULL;
static uint32_t bufOffset = 0;

static bool audioInit = false;

void audioPlay(void *userdata, uint8_t * stream, int len) {
  int readCnt = 0;
  uint8_t *dataBuf = sbuf;
  
  int readSize = len < audio_base[reg_count] ? len : audio_base[reg_count];
  while (readCnt < readSize) {
    uint8_t data = *(dataBuf + bufOffset);
    *(stream + readCnt) = data;
    
    readCnt += 1;
    bufOffset = (bufOffset + 1) % audio_base[reg_sbuf_size];
  }
  
  if (len > readCnt) {
    memset(stream + readCnt, 0, len - readCnt);
  }
  
  audio_base[reg_count] -= readCnt;
}

static void audio_io_handler(uint32_t offset, int len, bool is_write) {
  if (!is_write) {
    return ;
  }

  int reg = offset / sizeof(uint32_t);
  if (reg == reg_init && !audioInit && audio_base[reg_init]) {
    SDL_AudioSpec s = {};

    s.format = AUDIO_S16SYS;
    s.userdata = NULL;
    s.freq = audio_base[reg_freq];
    s.channels = audio_base[reg_channels];
    s.samples = audio_base[reg_samples];
    s.callback = audioPlay;
    s.userdata = sbuf;
    SDL_InitSubSystem(SDL_INIT_AUDIO);
    SDL_OpenAudio(&s, NULL);
    SDL_PauseAudio(0);

    audioInit = true;
  }

  // if (reg == reg_add_count) {
  //   audio_base[reg_count] += audio_base[reg_add_count];
  //   audio_base[reg_add_count] = 0;
  // }
}

void init_audio() {
  uint32_t space_size = sizeof(uint32_t) * nr_reg;
  audio_base = (uint32_t *)new_space(space_size);
#ifdef CONFIG_HAS_PORT_IO
  add_pio_map ("audio", CONFIG_AUDIO_CTL_PORT, audio_base, space_size, audio_io_handler);
#else
  add_mmio_map("audio", CONFIG_AUDIO_CTL_MMIO, audio_base, space_size, audio_io_handler);
#endif

  sbuf = (uint8_t *)new_space(CONFIG_SB_SIZE);
  add_mmio_map("audio-sbuf", CONFIG_SB_ADDR, sbuf, CONFIG_SB_SIZE, NULL);

  audio_base[reg_sbuf_size] = CONFIG_SB_SIZE;
  audio_base[reg_count] = 0;
  // audio_base[reg_add_count] = 0;
}
