#include <am.h>
#include <nemu.h>

#define KEYDOWN_MASK 0x8000

void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint32_t key = inl(KBD_ADDR);
  if (!key) {
    kbd->keydown = 0;
    kbd->keycode = AM_KEY_NONE;
    return ;
  }

  kbd->keydown = (key & KEYDOWN_MASK) != 0;
  kbd->keycode = (key & (~KEYDOWN_MASK));
}
