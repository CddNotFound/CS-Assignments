#include <stddef.h>
#include "ll_cycle.h"

int ll_has_cycle(node *head) {
    /* your code here */
    if (head == NULL) {
        return 0;
    }

    node *first = head, *last = head;

    while (last -> next != NULL) {
        last = last -> next;
        if (last -> next == NULL) {
            return 0;
        }
        last = last -> next;
        first = first -> next;
        if (last == first) {
            return 1;
        }
    }

    return 0;
}