package com.nebkat.appdisabler;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;


import java.lang.reflect.Field;

public class AppDisabler {

    private static final String TAG = "AppDisabler";

    public static final int SUCCESS = BinderTransactor.SUCCESS;
    public static final int ERROR_UNKNOWN = BinderTransactor.ERROR_UNKNOWN;
    public static final int ERROR_ROOT = BinderTransactor.ERROR_ROOT;
    public static final int ERROR_NO_ROOT = BinderTransactor.ERROR_NO_ROOT;

    public static int setApplicationEnabledSetting(Context context, String packageName, boolean enabled) {
        return setApplicationEnabledSetting(context, packageName, enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED, 0);
    }

    public static int setApplicationEnabledSetting(Context context, String packageName, int setting, int flags) {
        try {
            Parcel app = Parcel.obtain();

            Class packageManager = Class.forName("android.content.pm.IPackageManager$Stub");
            Field setApplicationEnabledSetting = packageManager.getDeclaredField("TRANSACTION_setApplicationEnabledSetting");
            Field descriptor = packageManager.getDeclaredField("DESCRIPTOR");
            setApplicationEnabledSetting.setAccessible(true);
            descriptor.setAccessible(true);
            String token = (String) descriptor.get(packageManager);
            int transaction = setApplicationEnabledSetting.getInt(packageManager);

            app.writeInterfaceToken(token);
            app.writeString(packageName);
            app.writeInt(setting);
            app.writeInt(flags);

            int result = BinderTransactor.transact(context, "package", app, transaction);
            if (result != SUCCESS) {
                return result;
            }

            if (context.getPackageManager().getApplicationEnabledSetting(packageName) != setting) {
                return ERROR_UNKNOWN;
            }

            return SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Error setting app enabled setting", e);
            return ERROR_UNKNOWN;
        }
    }

}