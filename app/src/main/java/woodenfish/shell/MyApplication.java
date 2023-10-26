package woodenfish.shell;


import android.app.Application;
import android.util.Log;


public class MyApplication extends Application {
    public final static String apkPath = "/storage/emulated/0/5.apk";
    public static android.content.pm.PackageParser.Package pkg;
    public static MyApplication myApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication=this;
        System.out.println("我是宿主的Application："+this);

    }





    public static android.content.pm.PackageParser.Package par(){
        android.content.pm.PackageParser.Package aPackage = mirror.android.content.pm.PackageParser.parserApk(apkPath);
        if (aPackage!=null){
            Log.e("pkg：",aPackage.packageName);
            Log.e("main_activitie：",aPackage.activities.get(0).className);
//            Log.e("applicationInfo：",aPackage.applicationInfo.className);


//            for (PackageParser.Activity fruit : aPackage.activities) {
//                System.out.println(fruit.className);
//            }
        }else{

            System.out.println("包名空");
        }
        return  aPackage;
    }



}
