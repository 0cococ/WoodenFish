package woodenfish.shell;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

public class PluginContextUtil {
    public static Resources getPluginResources(Context context, String packageName) {
        try {
            // 获取插件应用的包信息
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);

            // 创建插件应用的上下文
            Context pluginAppContext = context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            // 获取插件应用的 LoadedApk
            Object loadedApk = getLoadedApk(pluginAppContext);

            if (loadedApk != null) {
                // 获取插件应用的 Resources
                Resources pluginResources = getLoadedApkResources(loadedApk);
                return pluginResources;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getLoadedApk(Context context) {
        try {
            // 获取 ContextImpl 的 mPackageInfo 字段
            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            Object packageInfo = getField(contextImplClass, context, "mPackageInfo");
            return packageInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Resources getLoadedApkResources(Object loadedApk) {
        try {
            // 获取 LoadedApk 的 getResources 方法
            Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
            Object resources = invokeMethod(loadedApkClass, loadedApk, "getResources");
            if (resources instanceof Resources) {
                return (Resources) resources;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getField(Class<?> clazz, Object object, String fieldName) throws Exception {
        java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private static Object invokeMethod(Class<?> clazz, Object object, String methodName) throws Exception {
        java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(object);
    }
}
