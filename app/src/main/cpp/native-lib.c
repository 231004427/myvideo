#include <jni.h>
#include "mybit.h"
#include "base.h"
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jstring JNICALL
Java_com_sunlin_myvideo_Mybit_stringFromJNI(
        JNIEnv *env,
        jobject obj) {
    LOGE("hello log!!!!!!");
    return (*env)->NewStringUTF(env, "Hello from C");
}
JNIEXPORT jint JNICALL
Java_com_sunlin_myvideo_Mybit_buildData(
        JNIEnv *env, jobject obj,
        jbyteArray buffer, jint t,jint from, jint to, jbyteArray data) {

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
#ifdef __cplusplus
}
#endif
