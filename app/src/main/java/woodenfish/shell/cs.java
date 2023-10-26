package woodenfish.shell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class cs {

    private  static  final  String ta="eng";
    
    @SuppressLint("PrivateApi")
    public static void hookAms() {
        try {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            Field singletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field instanceField = singletonClass.getDeclaredField("mInstance");
            instanceField.setAccessible(true);
            Object originalInstance = instanceField.get(singleton);
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass},
                    (Object proxyObject, Method method, Object[] args) -> {
                        if ("startActivity".equals(method.getName())) {
                            for (int i = 0; i < args.length; i++) {
                                if (args[i] instanceof Intent) {
                                    Intent intent = (Intent) args[i];
                                    Intent newIntent = new Intent();
                                    newIntent.setClassName("pa.illusion", "pa.illusion.ProxyActivity");
                                    newIntent.putExtra(ta, intent);
                                    args[i] = newIntent;
                                    break;
                                }
                            }
                        }
                        return method.invoke(originalInstance, args);
                    });
            instanceField.set(singleton, proxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    public static void hookHandler() {
        try {
            // 获取 ActivityThread 类的 Class 对象
            Class<?> clazz = Class.forName("android.app.ActivityThread");

            // 获取 ActivityThread 对象
            Field activityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(null);

            // 获取 mH 对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            final Handler mH = (Handler) mHField.get(activityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            // 创建的 callback
            Handler.Callback callback = new Handler.Callback() {

                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    // 通过msg  可以拿到 Intent，可以换回执行插件的Intent
                    // 找到 Intent的方便替换的地方  --- 在这个类里面 ActivityClientRecord --- Intent intent 非静态
                    // msg.obj == ActivityClientRecord
                    switch (msg.what) {
                        case 100:
                            try {
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                // 启动代理Intent
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);
                                // 启动插件的 Intent
                                Intent intent = proxyIntent.getParcelableExtra(ta);
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            try {
                                // 获取 mActivityCallbacks 对象
                                Field mActivityCallbacksField = msg.obj.getClass()
                                        .getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);
                                List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);

                                for (int i = 0; i < mActivityCallbacks.size(); i++) {
                                    if (mActivityCallbacks.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = mActivityCallbacks.get(i);

                                        // 获取启动代理的 Intent
                                        Field mIntentField = launchActivityItem.getClass()
                                                .getDeclaredField("mIntent");
                                        mIntentField.setAccessible(true);
                                        Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);

                                        // 目标 intent 替换 proxyIntent
                                        Intent intent = proxyIntent.getParcelableExtra(ta);
                                        if (intent != null) {
                                            mIntentField.set(launchActivityItem, intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    // 必须 return false
                    return false;
                }
            };

            // 替换系统的 callBack
            mCallbackField.set(mH, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static Context createCustomContext(Context baseContext, String pluginPackageName) {
            try {
                // 获取插件应用的 APK 路径
                PackageManager pm = baseContext.getPackageManager();
                String pluginApkPath = pm.getApplicationInfo(pluginPackageName, 0).sourceDir;

                // 创建自定义 AssetManager
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                addAssetPathMethod.invoke(assetManager, pluginApkPath);

                // 创建自定义 Resources
                Resources baseResources = baseContext.getResources();
                Resources pluginResources = new Resources(assetManager, baseResources.getDisplayMetrics(),
                        baseResources.getConfiguration());

                // 创建自定义上下文
                Context customContext = new ContextThemeWrapper(baseContext, 0) {
                    @Override
                    public Resources getResources() {
                        return pluginResources;
                    }
                };

                return customContext;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }



}
