package com.nebkat.appdisabler;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtils {

    public static boolean hasRoot() {
        if (new File("/system/bin/su").isFile() || new File("/system/xbin/su").isFile()) {
            return true;
        }

        try {
            Process hasRoot = Runtime.getRuntime().exec("/system/xbin/which su");
            BufferedReader in = new BufferedReader(new InputStreamReader(hasRoot.getInputStream()));
            String su = in.readLine();
            in.close();
            hasRoot.destroy();
            if (su != null) {
                return true;
            }
        } catch (Exception e) {
            // continue
        }

        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true;
        }

        return false;
    }
}