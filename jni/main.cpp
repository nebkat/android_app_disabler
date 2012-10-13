#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
#include <binder/Parcel.h>

#include <jni.h>

using namespace android;

int main(int argc, char *argv[]) {

    if (argc < 5) return 0;

    int parcelSize = atoi(argv[4]);
    Parcel transferParcel;

    char *data = argv[3];

    uint8_t* buffer = new uint8_t[strlen(data)/2];
    char temp[2];
    for (size_t i = 0; i < strlen(data); i++) {
        temp[0] = data[i * 2];
        temp[1] = data[i * 2 + 1];
        buffer[i] = (uint8_t) strtol(temp, NULL, 16);
    }

    transferParcel.setData(buffer, parcelSize);

    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder;

    binder = sm->getService(String16(argv[1]));
    if (binder == NULL) return 0;

    Parcel ret;
    binder->transact(atoi(argv[2]), transferParcel, &ret);

    const uint8_t* retBuffer = ret.data();
    char* hex = new char[ret.dataSize() * 2];
    char* tempHex = new char[2];
    for (size_t i = 0; i < ret.dataSize(); i++) {
        sprintf(tempHex, "%X", retBuffer[i]);
        if (strlen(tempHex) == 1) {
            strcat(hex, "0");
        }
        strcat(hex, tempHex);
    }

    printf("%s", hex);
}