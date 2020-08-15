package com.mei.util;

import java.util.Collection;
import java.util.Map;

public class ObjectUtil {

    public static boolean isAnyEmpty(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object instanceof String && ((String) object).trim().isEmpty()) {
                return true;
            } else if (object instanceof Collection && ((Collection) object).isEmpty()) {
                return true;
            } else if (object instanceof Map && ((Map) object).isEmpty()) {
                return true;
            } else if (object == null) {
                return true;
            }
        }
        return false;
    }
}
