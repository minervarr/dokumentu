#include <jni.h>
#include <string>
#include <android/log.h>
#include "csv_manager.h"

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_dokumentu_app_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ with CSV support!";
    return env->NewStringUTF(hello.c_str());
}

// MainActivity native methods (kept for backward compatibility)
extern "C" JNIEXPORT jboolean JNICALL
Java_io_nava_dokumentu_app_MainActivity_loadCSVFile(
        JNIEnv* env,
        jobject /* this */,
        jstring filePath) {

    if (filePath == nullptr) {
        LOGE("File path is null");
        return JNI_FALSE;
    }

    const char* pathStr = env->GetStringUTFChars(filePath, nullptr);
    if (pathStr == nullptr) {
        LOGE("Failed to get UTF chars from file path");
        return JNI_FALSE;
    }

    std::string path(pathStr);
    env->ReleaseStringUTFChars(filePath, pathStr);

    bool success = CSVManager::getInstance().loadFile(path);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_MainActivity_getCSVHeaders(
        JNIEnv* env,
        jobject /* this */) {

    const auto& headers = CSVManager::getInstance().getHeaders();

    if (headers.empty()) {
        return nullptr;
    }

    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        LOGE("Failed to find String class");
        return nullptr;
    }

    jobjectArray result = env->NewObjectArray(headers.size(), stringClass, nullptr);
    if (result == nullptr) {
        LOGE("Failed to create object array");
        return nullptr;
    }

    for (size_t i = 0; i < headers.size(); i++) {
        jstring headerStr = env->NewStringUTF(headers[i].c_str());
        if (headerStr == nullptr) {
            LOGE("Failed to create string for header %zu", i);
            continue;
        }
        env->SetObjectArrayElement(result, i, headerStr);
        env->DeleteLocalRef(headerStr);
    }

    return result;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_MainActivity_getCSVRow(
        JNIEnv* env,
        jobject /* this */,
        jint rowIndex) {

    if (rowIndex < 0) {
        LOGE("Invalid row index: %d", rowIndex);
        return nullptr;
    }

    auto rowData = CSVManager::getInstance().getRow(static_cast<size_t>(rowIndex));

    if (rowData.empty()) {
        return nullptr;
    }

    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        LOGE("Failed to find String class");
        return nullptr;
    }

    jobjectArray result = env->NewObjectArray(rowData.size(), stringClass, nullptr);
    if (result == nullptr) {
        LOGE("Failed to create object array");
        return nullptr;
    }

    for (size_t i = 0; i < rowData.size(); i++) {
        jstring cellStr = env->NewStringUTF(rowData[i].c_str());
        if (cellStr == nullptr) {
            LOGE("Failed to create string for cell %zu", i);
            continue;
        }
        env->SetObjectArrayElement(result, i, cellStr);
        env->DeleteLocalRef(cellStr);
    }

    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_io_nava_dokumentu_app_MainActivity_getRowCount(
        JNIEnv* env,
        jobject /* this */) {

    return static_cast<jint>(CSVManager::getInstance().getRowCount());
}

extern "C" JNIEXPORT jint JNICALL
Java_io_nava_dokumentu_app_MainActivity_getColumnCount(
        JNIEnv* env,
        jobject /* this */) {

    return static_cast<jint>(CSVManager::getInstance().getColumnCount());
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_dokumentu_app_MainActivity_getCellValue(
        JNIEnv* env,
        jobject /* this */,
        jint rowIndex,
        jint columnIndex) {

    if (rowIndex < 0 || columnIndex < 0) {
        return nullptr;
    }

    std::string value = CSVManager::getInstance().getCellValue(
            static_cast<size_t>(rowIndex),
            static_cast<size_t>(columnIndex)
    );

    return env->NewStringUTF(value.c_str());
}

// CSVDataBridge static native methods (new shared interface)
extern "C" JNIEXPORT jboolean JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_loadCSVFile(
        JNIEnv* env,
        jclass /* clazz */,
        jstring filePath) {

    if (filePath == nullptr) {
        LOGE("File path is null");
        return JNI_FALSE;
    }

    const char* pathStr = env->GetStringUTFChars(filePath, nullptr);
    if (pathStr == nullptr) {
        LOGE("Failed to get UTF chars from file path");
        return JNI_FALSE;
    }

    std::string path(pathStr);
    env->ReleaseStringUTFChars(filePath, pathStr);

    bool success = CSVManager::getInstance().loadFile(path);
    return success ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_getCSVHeaders(
        JNIEnv* env,
        jclass /* clazz */) {

    const auto& headers = CSVManager::getInstance().getHeaders();

    if (headers.empty()) {
        return nullptr;
    }

    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        LOGE("Failed to find String class");
        return nullptr;
    }

    jobjectArray result = env->NewObjectArray(headers.size(), stringClass, nullptr);
    if (result == nullptr) {
        LOGE("Failed to create object array");
        return nullptr;
    }

    for (size_t i = 0; i < headers.size(); i++) {
        jstring headerStr = env->NewStringUTF(headers[i].c_str());
        if (headerStr == nullptr) {
            LOGE("Failed to create string for header %zu", i);
            continue;
        }
        env->SetObjectArrayElement(result, i, headerStr);
        env->DeleteLocalRef(headerStr);
    }

    return result;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_getCSVRow(
        JNIEnv* env,
        jclass /* clazz */,
        jint rowIndex) {

    if (rowIndex < 0) {
        LOGE("Invalid row index: %d", rowIndex);
        return nullptr;
    }

    auto rowData = CSVManager::getInstance().getRow(static_cast<size_t>(rowIndex));

    if (rowData.empty()) {
        return nullptr;
    }

    jclass stringClass = env->FindClass("java/lang/String");
    if (stringClass == nullptr) {
        LOGE("Failed to find String class");
        return nullptr;
    }

    jobjectArray result = env->NewObjectArray(rowData.size(), stringClass, nullptr);
    if (result == nullptr) {
        LOGE("Failed to create object array");
        return nullptr;
    }

    for (size_t i = 0; i < rowData.size(); i++) {
        jstring cellStr = env->NewStringUTF(rowData[i].c_str());
        if (cellStr == nullptr) {
            LOGE("Failed to create string for cell %zu", i);
            continue;
        }
        env->SetObjectArrayElement(result, i, cellStr);
        env->DeleteLocalRef(cellStr);
    }

    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_getRowCount(
        JNIEnv* env,
        jclass /* clazz */) {

    return static_cast<jint>(CSVManager::getInstance().getRowCount());
}

extern "C" JNIEXPORT jint JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_getColumnCount(
        JNIEnv* env,
        jclass /* clazz */) {

    return static_cast<jint>(CSVManager::getInstance().getColumnCount());
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_dokumentu_app_CSVDataBridge_getCellValue(
        JNIEnv* env,
        jclass /* clazz */,
        jint rowIndex,
        jint columnIndex) {

    if (rowIndex < 0 || columnIndex < 0) {
        return nullptr;
    }

    std::string value = CSVManager::getInstance().getCellValue(
            static_cast<size_t>(rowIndex),
            static_cast<size_t>(columnIndex)
    );

    return env->NewStringUTF(value.c_str());
}