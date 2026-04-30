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

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

enum {
  TK_NOTYPE = 256,
  TK_PLUS, TK_MINUS, TK_MUL, TK_DIV, 
  TK_EQ, TK_NEQ,
  TK_AND, 
  TK_NUMBER, TK_NUMBERU, 
  TK_REGISTER, 
  TK_LPAREN, TK_RPAREN,
  // TK_GETVALUE

  /* TODO: Add more token types */

};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", TK_PLUS},     // plus
  {"\\-", TK_MINUS},    // minus
  {"\\*", TK_MUL},      // mul
  {"\\/", TK_DIV},      // div
  {"==", TK_EQ},        // equal
  {"!=", TK_NEQ},       // not equal
  {"&&", TK_AND},       // logical and
  {"\\(", TK_LPAREN},   // left paren
  {"\\)", TK_RPAREN},   // right paren
  {"\\$[_a-zA-Z][_0-9a-zA-Z]*", TK_REGISTER},      // register name
  // {"[1-9][0-9]*", TK_NUMBER}           // number
  {"0x[0-9a-fA-F]*u", TK_NUMBERU},         // number
  {"0x[0-9a-fA-F]*", TK_NUMBER}          // number
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[65536] __attribute__((used)) = {};
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);
            
            /* TODO: Now a new token is recognized with rules[i]. Add codes
            * to record the token in the array `tokens'. For certain types
            * of tokens, some extra actions should be performed.
            */
        
        switch (rules[i].token_type) {
          default: 
            tokens[nr_token].type = rules[i].token_type;
            memcpy(tokens[nr_token].str, e + position, substr_len);
            tokens[nr_token].str[substr_len] = '\0';
            ++nr_token;
            break;
        }

        position += substr_len;

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }
  
  
  int realCnt = 0;
  for (int i = 0; i < nr_token; i++) {
    if (tokens[i].type == TK_NOTYPE) {
      continue;
    }
    tokens[realCnt] = tokens[i];
    ++realCnt;
  }
  nr_token = realCnt;
  
  // for (int i = 0; i < nr_token; i++) {
  //   printf("%d:%s\n", tokens[i].type, tokens[i].str);
  // }

  return true;
}

bool check_parentheses(int p, int q) {
  if (tokens[p].type != TK_LPAREN || tokens[q].type != TK_RPAREN) {
    return false;
  }

  // printf("check %d %d\n", p, q);

  int parenCnt = 0;
  for (int i = p; i <= q; i++) {
    if (tokens[i].type == TK_LPAREN) {
      ++parenCnt;
    }
    if (tokens[i].type == TK_RPAREN) {
      --parenCnt;
    }

    // printf("?? %d %d\n", parenCnt, i);

    if (parenCnt < 0) { // invalid
      return false;
    }
    
    if (parenCnt == 0 && i < q) { // not match
      return false;
    } 
  }

  return parenCnt == 0;  // match
}

int getOperatorPosition(int p, int q) {
  int minPriority = 3;
  int idx = -1;
  int paraCnt = 0;
  // &&       : 0
  // == | !=  : 1
  // + | -    : 2
  // * | ÷    : 3

  for (int i = p; i <= q; i++) {
    int type = tokens[i].type;
    if (type == TK_LPAREN) { ++paraCnt; }
    if (type == TK_RPAREN) { --paraCnt; }
    if (paraCnt) { // in parens
      continue;
    }
    if (!(type == TK_PLUS || type == TK_MINUS || type == TK_MUL || type == TK_DIV || type == TK_EQ || type == TK_NEQ || type == TK_AND)) {
      continue;
    }

    int curPriority = 3;
    switch (type) {
      case TK_AND:
        curPriority = 0;
        break;
      case TK_EQ: case TK_NEQ:
        curPriority = 1;
        break;
      case TK_PLUS: case TK_MINUS:
        curPriority = 2;
        break;
      default:
        curPriority = 3;
    }

    if (curPriority <= minPriority) {
      minPriority = curPriority;
      idx = i;
    }
  }

  return idx;
}

void ErrDividedByZero() {
  printf("Divided by zero.\n");
}

word_t eval(int p, int q, bool *success) {
  *success = true;
  if (p > q) {
    *success = false;
    return 0;
  } else if (p == q) { // single number
    char* num = tokens[p].str;
    int len = strlen(num);
    int result = 0;
    for (int i = 2; i < len; i++) {
      if (num[i] == 'u') { continue; }
      if (num[i] >= '0' && num[i] <= '9') {
        result = (result << 4) + (num[i] - '0');
      } else if (num[i] >= 'a' && num[i] <= 'f') {
        result = (result << 4) + (num[i] - 'a' + 10);
      } else {
        result = (result << 4) + (num[i] - 'A' + 10);
      }
    }

    return result;
  } else if (check_parentheses(p, q)) {
    return eval(p + 1, q - 1, success);
  } else {
    int opIdx = getOperatorPosition(p, q);
    int opType = tokens[opIdx].type;
    bool leftBool, rightBool;
    word_t left = eval(p, opIdx - 1, &leftBool);
    word_t right = eval(opIdx + 1, q, &rightBool);
    // printf("%d %d", (int)left, (int)right);
    if (!leftBool || !rightBool) {  // invalid expr
      *success = false;
      return 0;
    }
    // printf("%d %d], : %d", p, q, (int)opType);

    if (opType == TK_PLUS) {
      return left + right;
    } else if (opType == TK_MINUS) {
      return left - right;
    } else if (opType == TK_MUL) {
      return left * right;
    } else if (opType == TK_EQ) {
      return left == right;
    } else if (opType == TK_NEQ) {
      return left != right;
    } else if (opType == TK_AND) {
      return left && right;
    } else {
      if (right == 0) {
        ErrDividedByZero();
        success = false;
        return 0;
      }
      // printf("???%d / %d \n", (int)left, (int)right);
      return left / right;
    }
  }


  return -1;
}

word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  // printf("%d\n", (int)eval(0, nr_token - 1));

  /* TODO: Insert codes to evaluate the expression. */
  // TODO();
  return eval(0, nr_token - 1, success);
  // return 0;
}
