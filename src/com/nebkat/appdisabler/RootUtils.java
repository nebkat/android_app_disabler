package com.nebkat.appdisabler;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtils {
    private static boolean sHasCheckedRoot = false;
    private static boolean sHasRoot = false;

    public static boolean hasRoot() {
        return hasRoot(true);
    }

    public static boolean hasRoot(boolean cached) {
        if (cached && sHasCheckedRoot) return sHasRoot;

        sHasRoot = false;
        sHasCheckedRoot = true;

        if (new File("/system/bin/su").isFile()) {
            sHasRoot = true;
            return sHasRoot;
        }

        try {
            Process hasRoot = Runtime.getRuntime().exec("/system/xbin/which su");
            BufferedReader in = new BufferedReader(new InputStreamReader(hasRoot.getInputStream()));
            String su = in.readLine();
            in.close();
            hasRoot.destroy();
            if (su != null) {
                sHasRoot = true;
                return sHasRoot;
            }
        } catch (Exception e) {
            sHasRoot = false;
            return sHasRoot;
        }

        String buildTags = Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            sHasRoot = true;
            return sHasRoot;
        }

        return sHasRoot;
    }
}