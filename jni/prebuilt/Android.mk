LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := forcetv
LOCAL_SRC_FILES := libforcetv.so

include $(PREBUILT_SHARED_LIBRARY)
