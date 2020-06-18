package ley.anvil.modpacktools.util;

import com.moandjiezana.toml.Toml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

public class CustomToml extends Toml {
    @SuppressWarnings("unchecked")
    public <T> T getPath(Class<T> type, String... path) {
        LinkedList<String> paths = new LinkedList<>(Arrays.asList(path));
        String key = paths.get(paths.size() - 1);
        paths.remove(paths.size() - 1);
        Toml toml = this;
        for(String str : paths)
            toml = toml.getTable(str);
        return (T)get(toml, key);
    }

    public Object get(Toml clazz, String key) {
        try {
            //Getting Around things being private for no reason 101
            Method method = Toml.class.getDeclaredMethod("get", String.class);
            method.setAccessible(true);
            return method.invoke(clazz, key);
        }catch(IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.out.println("get fail");
            e.printStackTrace();
        }
        return null;
    }
}
