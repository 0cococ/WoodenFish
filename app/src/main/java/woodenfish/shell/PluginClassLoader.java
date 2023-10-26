package woodenfish.shell;

import dalvik.system.DexClassLoader;


public class PluginClassLoader extends DexClassLoader {

    public PluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }


}
