package dds.service.cache;

import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class CacheFactory {

    public enum Option {
        INFINISPAN("infinispan.cache.InfinispanCacheService");
        public final String className;

        Option(String className) {
            this.className = className;
        }
    }

    public static <V> ICacheService<String, V> provideCache(Option opt, Class<V> clazz, String namespace) {
        try {
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(opt.className);
            Constructor<?> constructor = aClass.getConstructor(String.class, Function.class, Function.class);
            Gson gson = new Gson();
            Function<V, byte[]> serialize = o -> gson.toJson(o).getBytes();
            Function<byte[], V> deserialize = o -> gson.fromJson(new String(o), clazz);
            Object o = constructor.newInstance(namespace, serialize, deserialize);
            return ((ICacheService<String, V>) o);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}