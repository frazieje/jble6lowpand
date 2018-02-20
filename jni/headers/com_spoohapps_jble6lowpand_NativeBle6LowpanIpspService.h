/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService */

#ifndef _Included_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
#define _Included_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
 * Method:    scanIpspDevicesInternal
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_scanIpspDevicesInternal
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
 * Method:    connectIpspDevice
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_connectIpspDevice
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
 * Method:    disconnectIpspDevice
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_disconnectIpspDevice
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService
 * Method:    getConnectedIpspDevices
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_getConnectedIpspDevices
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
