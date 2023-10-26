package woodenfish.hook.frida;

import java.io.File;
import java.io.FileOutputStream;

public class Hook {
    public static Object value;
    public static String js;

    public static void frida(String filePath, String content) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file, false);
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void set(Object set) {
    value = set;
    }

    public static Object get() {
    return value;
    }


    public static String str() {
        return js;
    }

}
