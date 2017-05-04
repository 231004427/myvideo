#include <jni.h>
#include "mybit.h"
#include "base.h"
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jstring JNICALL
Java_com_sunlin_myvideo_MLM_MLMlib_stringFromJNI(
        JNIEnv *env,
        jobject obj) {
    LOGE("hello log!!!!!!");
    return (*env)->NewStringUTF(env, "Hello from C");
}
JNIEXPORT jint JNICALL
Java_com_sunlin_myvideo_MLM_MLMlib_buildData(
        JNIEnv *env, jobject obj,
        jbyteArray buffer, jint t,jlong from, jlong to, jbyteArray data) {

    jsize data_size = (*env)->GetArrayLength(env,data);
    jbyte *jdata = (*env)->GetByteArrayElements(env, data, NULL);
    jbyte *jbuffer = (*env)->GetByteArrayElements(env, buffer, NULL);

    int return_size=0;
    LOGI("type:%d,length:%d,from:%d,to:%d",(uint8_t)t,data_size,(uint16_t)from,(uint32_t)to);

    return_size=mybit_buildData(jbuffer,1,(uint8_t)t,(uint16_t)data_size,(uint16_t)from,(uint32_t)to,jdata);

    (*env)->ReleaseByteArrayElements(env,data, jdata, 0);
    (*env)->ReleaseByteArrayElements(env,buffer, jbuffer, 0);
    return return_size;
}
JNIEXPORT jint JNICALL
        Java_com_sunlin_myvideo_MLM_MLMlib_getDataHead(
                JNIEnv *env, jobject obj,
                jbyteArray src_data,
                jobject head
        ) {

    jclass head_obj = (*env)->GetObjectClass(env, head);
    if( head_obj){

        jfieldID vId = (*env)->GetFieldID(env, head_obj, "v", "I");
        //jint v_j = (int)(*env)->GetIntField(env, head, vId);
        jfieldID tId = (*env)->GetFieldID(env, head_obj, "t", "I");
        jfieldID lId = (*env)->GetFieldID(env, head_obj, "l", "I");
        jfieldID fromId = (*env)->GetFieldID(env, head_obj, "from", "L");
        jfieldID toId = (*env)->GetFieldID(env, head_obj, "to", "L");


        jbyte *jdata = (*env)->GetByteArrayElements(env, src_data, NULL);
        struct myhead head;
        mybit_getDataHead(jdata,&head);

        // 设置返回值
        (*env)->SetIntField(env,head_obj,vId,head.v);
        (*env)->SetIntField(env,head_obj,tId,head.t);
        (*env)->SetIntField(env,head_obj,lId,head.l);
        (*env)->SetLongField(env,head_obj,fromId,head.from);
        (*env)->SetLongField(env,head_obj,toId,head.to);

        //释放指针
        (*env)->ReleaseByteArrayElements(env,src_data, jdata, 0);
        return 1;
        //const char *locstr = (*env)->GetStringUTFChars(env, str, &iscopy);
        //LOGD("str = %s", locstr);
        //(*env)->ReleaseStringUTFChars(env, str, locstr);
    }
    return -1;
}

#ifdef __cplusplus
}
#endif
