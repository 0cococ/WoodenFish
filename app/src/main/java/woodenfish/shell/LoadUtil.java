package woodenfish.shell;

import static woodenfish.shell.MyApplication.apkPath;
import static woodenfish.shell.MyApplication.myApplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadUtil {
    public static Context mContext;
    private static Resources mResources;
    public static void loadClass(Context context) {

        /**
         * 宿主dexElements = 宿主dexElements + 插件dexElements
         *
         * 1.获取宿主dexElements
         * 2.获取插件dexElements
         * 3.合并两个dexElements
         * 4.将新的dexElements 赋值到 宿主dexElements
         *
         * 目标：dexElements  -- DexPathList类的对象 -- BaseDexClassLoader的对象，类加载器
         *
         * 获取的是宿主的类加载器  --- 反射 dexElements  宿主
         *
         * 获取的是插件的类加载器  --- 反射 dexElements  插件
         */
        try {
            Class<?> clazz = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = clazz.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            // 宿主的 类加载器
            ClassLoader pathClassLoader = context.getClassLoader();
            // DexPathList类的对象
            Object hostPathList = pathListField.get(pathClassLoader);
            // 宿主的 dexElements
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            // 插件的 类加载器
            ClassLoader dexClassLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(),
                    null, pathClassLoader);
            System.out.println("插件："+dexClassLoader);
            // DexPathList类的对象
            Object pluginPathList = pathListField.get(dexClassLoader);
            // 插件的 dexElements
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            // 宿主dexElements = 宿主dexElements + 插件dexElements
//            Object[] obj = new Object[]; // 不行

            // 创建一个新数组
            Object[] newDexElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, newDexElements,
                    0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, newDexElements,
                    hostDexElements.length, pluginDexElements.length);

            // 赋值
            // hostDexElements = newDexElements
            dexElementsField.set(hostPathList, newDexElements);
            System.out.println("获取成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static  void load(Context context){
        try {
            ClassLoader pathClassLoader = context.getClassLoader();
            ClassLoader myClassLoaderObj = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(),
                    null, pathClassLoader);
            Class<?> myBaseDexClassLoaderClass=Class.forName("dalvik.system.BaseDexClassLoader");
            Field myPathListField=myBaseDexClassLoaderClass.getDeclaredField("pathList");
            myPathListField.setAccessible(true);
//接着得到插件pathList的Obj
            Object myPathListObj=myPathListField.get(myClassLoaderObj);
            Class<?> myPathListCLass=myPathListObj.getClass();
            Field myDexElementField=myPathListCLass.getDeclaredField("dexElements");
            myDexElementField.setAccessible(true);

// 然后在从myPathListObj这个对象身上去dexElement数组
            Object myDexElementArray=myDexElementField.get(myPathListObj);


            // 开始获取系统的，对象就是我们的上下文的classLoader
            PathClassLoader sysClassLoader = (PathClassLoader) context.getClassLoader();
            Class<?> sysDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field sysPathListField = sysDexClassLoaderClass.getDeclaredField("pathList"); sysPathListField.setAccessible(true);

//拿到我们系统的pathList的对象
            Object sysPathListObj = sysPathListField.get(sysClassLoader);

            Class<?> sysPathListClazz = sysPathListObj.getClass();
            Field sysDexElements = sysPathListClazz.getDeclaredField("dexElements");
            sysDexElements.setAccessible(true);

//  然后在获取系统的Element数组
            Object sysElementsArray = sysDexElements.get(sysPathListObj);


            int myLength = Array.getLength(myDexElementArray);
            int sysLength = Array.getLength(sysElementsArray);
            int newLength = myLength + sysLength;// 这个就是咱们要融合的新数组的长度

// 然后我们创建一个新的数组，因为是Object类型，所以我们使用反射包下提供的工具类


// 这个拿到的就是系统dexElement数组中对象的class类型
            Class<?> sysElementClazz = sysElementsArray.getClass().getComponentType();
//创建一个新的数组 但是里面要穿一个数组里面的对象的class类型
            Object newElementArray = Array.newInstance(sysElementClazz, newLength);

// 新数组创建好了，那么我开始合并
            for (int i = 0; i < newLength; i++) {
                if (i < myLength) {
                    // 插件的数组
                    Array.set(newElementArray, i, Array.get(myDexElementArray, i));
                } else {
                    //系统的数组
                    Array.set(newElementArray, i, Array.get(sysElementsArray, i - myLength));
                }
            }

// 现在就将两个dexElement数组全部都放到我们的新数组中去了
            sysDexElements.set(sysPathListObj,newElementArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Resources getResources(Context context) {
        if (mResources == null) {
            mResources = loadResource(context);
        }
        return mResources;
    }
    public static Resources loadResource(Context context) {
//         assets.addAssetPath(key.mResDir)
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            // 让 这个 AssetManager对象 加载的 资源为插件的
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);
            Resources resources = context.getResources();
            // 加载插件的资源的 resources
            Resources r  = new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
            System.out.println("资源："+r);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void on(Activity a) {
        Log.e("on","我被调用了");

        Resources resources = LoadUtil.getResources(a.getApplication());

        mContext = new ContextThemeWrapper(a.getBaseContext(), 0);

        Class<? extends Context> clazz = mContext.getClass();
        try {
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(mContext, resources);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static void sl(Application proxyApplication){


        try {
            myApplication.createPackageContext("", Context.CONTEXT_IGNORE_SECURITY |
                    Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        Class<?> activityManagerClass = null;
    try {
        activityManagerClass = Class.forName("android.app.LoadedApk");
        Field singletonField = activityManagerClass.getDeclaredField("mResDir");
        singletonField.setAccessible(true);
//        Object singleton = singletonField.get(null);
        System.out.println("测试:"+singletonField);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }


}
    public static Context getmContext(){
        mContext=plucontext();
        return mContext;
    }

public static Context  plucontext(){

    try {
     return    myApplication.createPackageContext(MyApplication.pkg.packageName, Context.CONTEXT_IGNORE_SECURITY |
                Context.CONTEXT_INCLUDE_CODE);
    } catch (PackageManager.NameNotFoundException e) {
        throw new RuntimeException(e);
    }
}


}
