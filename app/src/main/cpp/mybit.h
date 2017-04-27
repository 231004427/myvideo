//
//  mybit.h
//  TestChat
//
//  Created by 孙林 on 2017/4/10.
//  Copyright © 2017年 sunlin. All rights reserved.
//

#ifndef mybit_h
#define mybit_h
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <netinet/in.h>
struct myhead{
    uint8_t v;
    uint8_t t;
    uint16_t l;
    uint32_t from;
    uint32_t to;
};
int mybit_buildData(char*,uint8_t,uint8_t,uint16_t,uint32_t,uint32_t,char*);
int mybit_getDataHead(char * src_data,struct myhead * head);
#endif /* mybit_h */
