package mirror.android.content.pm;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;


import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

import Utils.compat.BuildCompat;
import woodenfish.hook.frida.Hook;

public class PackageParser {


    private static final int API_LEVEL = Build.VERSION.SDK_INT;

    public static android.content.pm.PackageParser.Package parserApk(String file) {
        try {
            android.content.pm.PackageParser parser = _new();
            android.content.pm.PackageParser.Package aPackage = parsePackage(parser, new File(file), 0);

            if (aPackage.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE")
                    && aPackage.mAppMetaData != null && aPackage.mAppMetaData.containsKey("fake-signature")) {
                String sig = aPackage.mAppMetaData.getString("fake-signature");
                aPackage.mSignatures = new Signature[]{new Signature(sig)};
                Log.d("aPackage", "Using fake-signature feature on : " + aPackage.packageName);
            } else {
                collectCertificates(parser, aPackage, 0);
            }
            return aPackage;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static android.content.pm.PackageParser.Package parsePackage(android.content.pm.PackageParser parser, File packageFile, int flags) throws Throwable {
        if (API_LEVEL >= M) {
            return  HookFw.hook_parsePackage1(parser,packageFile,flags);
        } else if (API_LEVEL >= LOLLIPOP_MR1) {
            return HookFw.hook_parsePackage1(parser,packageFile,flags);
        } else if (API_LEVEL >= LOLLIPOP) {
            return HookFw.hook_parsePackage1(parser,packageFile,flags);
        } else {
            return HookFw.hook_parsePackage2(parser,packageFile, null,
                    new DisplayMetrics(), flags);
        }
    }


    public static void  collectCertificates(android.content.pm.PackageParser parser,android.content.pm.PackageParser.Package p, int flags) throws Throwable {
        if (BuildCompat.isPie()) {
            HookFw.hook_collectCertificates2(p,true/*skipVerify*/);
        } else if (API_LEVEL >= N) {
            HookFw.hook_collectCertificates1(parser,p, flags);
        } else{
            HookFw.hook_collectCertificates1(parser,p, flags);
        }
    }

    public static android.content.pm.PackageParser _new(){
        return new android.content.pm.PackageParser();
    }

    public static final class HookFw{
        public static android.content.pm.PackageParser.Package hook_parsePackage1(android.content.pm.PackageParser parser, File packageFile, int flags){
            return null;
        }
        public static android.content.pm.PackageParser.Package hook_parsePackage2(android.content.pm.PackageParser parser, File packageFile,String destCodePath,DisplayMetrics metrics, int flags){
            return null;
        }
        public static void  hook_collectCertificates1(android.content.pm.PackageParser parser,android.content.pm.PackageParser.Package p, int flags) throws Throwable {

        }

        public static void  hook_collectCertificates2(android.content.pm.PackageParser.Package p, boolean skipVerify) throws Throwable {

        }
    }




}
