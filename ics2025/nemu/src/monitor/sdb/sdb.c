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

#include <isa.h>
#include <cpu/cpu.h>
#include <readline/readline.h>
#include <readline/history.h>
#include <memory/paddr.h>
#include "sdb.h"

static int is_batch_mode = false;

void init_regex();
void init_wp_pool();

/* We use the `readline' library to provide more flexibility to read from stdin. */
static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(nemu) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}

void ErrInvalidParameters() {
  printf("Invalid parameters.\n");
}

void ErrUnknownOptions(char* cmd) {
  printf("Unknown option(s): %s\n", cmd);
}
void ErrInvalidHexadecimalNumber() {
  printf("Invalid hexadecimal number();\n");
}

static int cmd_c(char *args) {
  cpu_exec(-1);
  return 0;
}


static int cmd_q(char *args) {
  return -1;
}

static int cmd_si(char *args) {
  int n = 1;
  if (args != NULL) {
    int len = strlen(args);
    for (int i = 0; i < len; i++) {
      n = n * 10 + (args[i] - '0');
    }
  }

  cpu_exec(n);

  return 0;
}

static int cmd_info(char *args) {
  if (args == NULL) {
    ErrInvalidParameters();
    return 0;
  }
  
  char* opt = strtok(args, " ");
  char* extra = strtok(NULL, " ");
  
  if (extra != NULL) {
    ErrInvalidParameters();
    return 0;
  }

  if (strcmp(opt, "r") == 0) {
    isa_reg_display();
  } else if (strcmp(opt, "w") == 0) {
    printf("Todo.\n");
    // printf("info w\n");
  } else {
    ErrUnknownOptions(opt);
    return 0;
  }
  
  return 0;
}

static int cmd_p(char* args) {
  if (args == NULL) {
    ErrInvalidParameters();
  }

  bool success = false;
  word_t result = expr(args, &success);

  printf("%d\n", result);

  return 0;
}

static int cmd_x(char *args) {
  if (args == NULL) {
    ErrInvalidParameters();
  }

  char* nStr = strtok(args, " ");
  char* addrStr = strtok(NULL, " ");
  int nLen = strlen(nStr);
  // int addrLen = strlen(addrStr);

  int n = 0;
  for (int i = 0; i < nLen; i++) {
    n = n * 10 + nStr[i] - 48;
  }

  bool success;
  paddr_t addr = expr(addrStr, &success);

  for (int i = 0; i < n; i++) {
    word_t val = paddr_read(addr + i * 4, 4);
    printf(FMT_PADDR ": " FMT_WORD "\n", addr + i * 4, val);
  }
  return 0;
}

static int cmd_help(char *args);

static struct {
  const char *name;
  const char *description;
  int (*handler) (char *);
} cmd_table [] = {
  { "help", "Display information about all supported commands", cmd_help },
  { "c", "Continue the execution of the program", cmd_c },
  { "q", "Exit NEMU", cmd_q },
  { "si", "Execute N steps", cmd_si },
  { "info", "Print the information of the program. \n\
       info r - registor information\n\
       info w - monitor information", cmd_info },
  { "p", "Calculate the value of expr.", cmd_p },
  { "x", "Scan memory.", cmd_x },
  /* TODO: Add more commands */

};

#define NR_CMD ARRLEN(cmd_table)

static int cmd_help(char *args) {
  /* extract the first argument */
  char *arg = strtok(NULL, " ");
  int i;

  if (arg == NULL) {
    /* no argument given */
    for (i = 0; i < NR_CMD; i ++) {
      printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
    }
  }
  else {
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(arg, cmd_table[i].name) == 0) {
        printf("%s - %s\n", cmd_table[i].name, cmd_table[i].description);
        return 0;
      }
    }
    printf("Unknown command '%s'\n", arg);
  }
  return 0;
}

void sdb_set_batch_mode() {
  is_batch_mode = true;
}

void sdb_mainloop() {
  if (is_batch_mode) {
    cmd_c(NULL);
    return;
  }

  for (char *str; (str = rl_gets()) != NULL; ) {
    char *str_end = str + strlen(str);

    /* extract the first token as the command */
    char *cmd = strtok(str, " ");
    if (cmd == NULL) { continue; }

    /* treat the remaining string as the arguments,
     * which may need further parsing
     */
    char *args = cmd + strlen(cmd) + 1;
    if (args >= str_end) {
      args = NULL;
    }

#ifdef CONFIG_DEVICE
    extern void sdl_clear_event_queue();
    sdl_clear_event_queue();
#endif

    int i;
    for (i = 0; i < NR_CMD; i ++) {
      if (strcmp(cmd, cmd_table[i].name) == 0) {
        if (cmd_table[i].handler(args) < 0) { return; }
        break;
      }
    }

    if (i == NR_CMD) { printf("Unknown command '%s'\n", cmd); }
  }
}

// #define CONFIG_EXPR_TEST
// #ifdef CONFIG_EXPR_TEST
// static void expr_test() {
//   char line[65536];
//   int total = 0, fail = 0;

//   while (fgets(line, sizeof(line), stdin) != NULL) {
//     uint32_t expected;
//     char expr_buf[65536];

//     if (sscanf(line, "%u %[^\n]", &expected, expr_buf) != 2) {
//       continue;
//     }
//     // printf("the expr is = %s\n", expr_buf);

//     bool success = true;
//     word_t got = expr(expr_buf, &success);
//     total++;

//     // printf("ans = %d, but got is = %d\n", expected, got);

//     if (!success || got != expected) {
//       printf("FAIL: expr=%s expected=%u got=" FMT_WORD "\n",
//               expr_buf, expected, got);
//       fail++;
//     }
//   }

//   printf("expr test: %d/%d passed\n", total - fail, total);
//   exit(fail != 0);
// }
// #endif

void init_sdb() {
  /* Compile the regular expressions. */
  init_regex();

  // #ifdef CONFIG_EXPR_TEST
  //   expr_test();
  // #endif

  /* Initialize the watchpoint pool. */
  init_wp_pool();
}


