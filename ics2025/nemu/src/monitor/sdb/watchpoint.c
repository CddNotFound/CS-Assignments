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

#include "sdb.h"

#define NR_WP 32

static WP wp_pool[NR_WP] = {};
static WP *head = NULL, *free_ = NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
    wp_pool[i].currentValue = 0;
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */

WP* new_wp() {
  if (free_ == NULL) {
    assert(0);
  }

  WP* res = free_;
  free_ = res -> next;

  if (head == NULL) {
    res -> next = NULL;
  } else {
    res -> next = head;
  }
  head = res;

  return res;
}

void free_wp(int id, bool *ok) {
  if (head == NULL) {
    return ;
  }

  WP* result = NULL;
  if (head -> NO == id) {
    result = head;
    *ok = true;
    head = head -> next;
  } else {
    WP *cur = head, *prePoint = head;
    cur = cur -> next;
    while (cur != NULL) {
      if (cur -> NO != id) {
        cur = cur -> next;
        prePoint = prePoint -> next;
        continue;
      }
      result = cur;
      *ok = true;
      prePoint -> next = cur -> next;
      break;
    }
  }
  if (*ok) {
    result -> next = free_;
    free_ = result;
  }
}
WP* getHead() {
  return head;
}
WP* getFree() {
  return free_;
}

void printWatchpoints() {
  printf("%-5s%-30s%-10s\n", "NO", "EXPR", "VALUE");
  WP *cur = head;
  while (cur != NULL) {
    printf("%-5d%-30s%-10u\n", cur -> NO, cur -> expr, cur -> currentValue);

    cur = cur -> next;
  }

}