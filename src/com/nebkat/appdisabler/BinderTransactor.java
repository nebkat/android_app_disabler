package com.nebkat.appdisabler;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Parcel;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class BinderTransactor {

    private static final String TAG = "BinderTransactor";
    private static final boolean LOGD = true;

    private static final String BINDER_TRANSACTOR = "binder_transact";

    public static final int SUCCESS = 1;
    public static final int ERROR_UNKNOWN = -1;
    public static final int ERROR_NO_ROOT = -2;
    public static final int ERROR_ROOT = -3;

    public static int transact(Context context, String service, Parcel parcel, int transaction) {
        if (LOGD) Log.d(TAG, "Starting binder transaction");
        if (LOGD) Log.d(TAG, "service="+service+" transaction="+transaction);
        try {
            if (!RootUtils.hasRoot()) {
                Log.e(TAG, "No root detected");
                return ERROR_NO_ROOT;
            }

            String fileDir = context.getFilesDir().getAbsolutePath();

            if (LOGD) Log.d(TAG, "Copying binder transactor");
            int result;
            result = copyBinderTransactor(context);
            if (result != SUCCESS) {
                Log.e(TAG, "Error copying binder transactor: "+result);
                return result;
            }
            if (fileDir == null) {
                Log.e(TAG, "Unknown file dir");
                return ERROR_UNKNOWN;
            }

            if (LOGD) Log.d(TAG, "Starting su process");

            Process su = Runtime.getRuntime().exec("su");

            DataOutputStream out = new DataOutputStream(su.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(su.getInputStream()));

            if (out == null || in == null) {
                Log.e(TAG, "Error getting input/output stream");
                return ERROR_ROOT;
            }

            out.writeBytes("id\n");
            out.flush();

            String uid = in.readLine();
            if (uid == null) {
                Log.e(TAG, "UID returned null");
                return ERROR_ROOT;
            }

            if (!uid.contains("uid=0")) {
                Log.e(TAG, "Error gaining root");
                out.writeBytes("exit\n");
                out.flush();
                return ERROR_ROOT;
            }

            if (LOGD) Log.d(TAG, "Setting permissions for binder transactor");

            out.writeBytes("cd " + fileDir + "\n");
            out.writeBytes("chmod 755 " + BINDER_TRANSACTOR + "\n");
            out.flush();

            if (LOGD) Log.d(TAG, "Marshalling parcel");

            parcel.setDataPosition(0);
            byte[] data = parcel.marshall();

            String command = String.format("./%s %s %d %s %d", BINDER_TRANSACTOR, service, transaction, byteToString(data), parcel.dataAvail());

            if (LOGD) Log.d(TAG, "Running binder transactor: "+command);

            out.writeBytes(command + "\n");
            out.flush();

            parcel.recycle();

            out.writeBytes("exit\n");
            out.flush();

            su.waitFor();

            if (LOGD) Log.d(TAG, "Transaction successful");

            return SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Error running binder transaction", e);
            return ERROR_UNKNOWN;
        }
    }

    private static int copyBinderTransactor(Context context) {
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(BINDER_TRANSACTOR);

            File dir = context.getFilesDir();
            File outFile = new File(dir, BINDER_TRANSACTOR);
            if (outFile.exists() && outFile.length() == is.available()) {
                return SUCCESS;
            }

            OutputStream os = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                os.write(buffer, 0, read);
            }

            if (!outFile.setExecutable(true)) {
                return ERROR_UNKNOWN;
            }

            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            return ERROR_UNKNOWN;
        }
        return SUCCESS;
    }

    private static String byteToString(byte[] bytes) {
        String hexString = "";
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            hexString = hexString + hex;
        }
        return hexString;
    }

}