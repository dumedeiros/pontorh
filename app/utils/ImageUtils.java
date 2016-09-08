package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImageUtils {
    public static <T> List<List<T>> split(List<T> items,
                                          int count) {
        List<List<T>> splitItems = new ArrayList<List<T>>();
        if (!items.isEmpty()) {
            int index = 0;
            int size = items.size();
            while (index < size) {
                List<T> subList = new ArrayList<T>(items.subList(index,
                        index + count < size ? index + count : size));
                splitItems.add(subList);
                index += subList.size();
            }
        }
        return splitItems;
    }

    public static boolean isEmpty(Collection c) {
        return (c == null || c.size() == 0);
    }
}
