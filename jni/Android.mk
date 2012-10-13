LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := binder_transact

LOCAL_SRC_FILES := main.cpp

LOCAL_SHARED_LIBRARIES := \
        libutils \
        libbinder

LOCAL_C_INCLUDES += \
        $(JNI_H_INCLUDE)

include $(BUILD_EXECUTABLE)