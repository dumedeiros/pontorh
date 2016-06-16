package utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {

    public static <T> void copyAllNonNullFields(T to, T from) {
        Class<T> clazz = (Class<T>) to.getClass();
        List<Field> fields = getAllModelFields(clazz);

        if (fields != null) {
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object obj = field.get(from);
                    if (obj != null) {
                        field.set(to, obj);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Field> getAllModelFields(Class aClass) {
        List<Field> fields = new ArrayList();
        do {
            Collections.addAll(fields, aClass.getDeclaredFields());
            aClass = aClass.getSuperclass();
        } while (aClass != null);
        return fields;
    }

}
