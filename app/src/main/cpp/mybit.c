//
//  mybit.c
//  TestChat
//
//  Created by 孙林 on 2017/4/10.
//  Copyright © 2017年 sunlin. All rights reserved.
//
#include "mybit.h"

int mybit_buildData(char* buffer,uint8_t v,uint8_t t,uint16_t l,uint32_t from,uint32_t to,char * data){

    struct myhead head;
    head.v=v;
    head.t=t;
    head.l=htons(l);
    head.from=htonl(from);
    head.to=htonl(to);
    int head_size;
    head_size=sizeof(struct myhead);
    int i;
    int num=head_size+l;
    for(i=0;i<num;i++)
    {
        if(i<head_size){
            buffer[i]=((char *)&head)[i];
        }else{
            buffer[i]=data[i-head_size];
        }
        //printf("%d ",buffer[i]);
    }
    return num;
}
int mybit_getDataHead(char * src_data,struct myhead * head)
{
    bcopy(src_data,(char *)head,sizeof(struct myhead));
    head->l=ntohs(head->l);
    head->from=ntohl(head->from);
    head->to=ntohl(head->to);

    return 1;
}
