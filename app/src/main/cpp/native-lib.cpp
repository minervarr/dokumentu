#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_dokumentu_app_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
// Add these methods to native-lib.cpp
extern "C" JNIEXPORT jboolean JNICALL
Java_io_nava_dokumentu_app_MainActivity_loadCSVFile(JNIEnv* env, jobject, jstring filePath);

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_MainActivity_getCSVHeaders(JNIEnv* env, jobject);

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_MainActivity_getCSVRow(JNIEnv* env, jobject, jint rowIndex);

extern "C" JNIEXPORT jint JNICALL
Java_io_nava_dokumentu_app_MainActivity_getRowCount(JNIEnv* env, jobject);