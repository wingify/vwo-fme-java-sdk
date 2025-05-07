/**
 * Copyright 2024-2025 Wingify Software Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vwo.utils;

import java.util.Date;
import java.util.function.Function;

public class DataTypeUtil {

    public static boolean isObject(Object val) {
        // return val != null && !(val instanceof Object[]) && !(val instanceof Function) && !(val instanceof String) && !(val instanceof RegExp) && !(val instanceof Promise) && !(val instanceof Date);
    	return val != null && !(val instanceof Object[]) && !(val instanceof Function) && !(val instanceof String) && !(val instanceof Date);
    }

    public static boolean isArray(Object val) {
        return val instanceof Object[];
    }

    public static boolean isNull(Object val) {
        return val == null;
    }

    public static boolean isUndefined(Object val) {
        return val == null;
    }

    public static boolean isDefined(Object val) {
        return val != null;
    }

    public static boolean isNumber(Object val) {
        return val instanceof Number;
    }

    public static boolean isInteger(Object val) {
        return val instanceof Integer;
    }

    public static boolean isString(Object val) {
        return val instanceof String;
    }

    public static boolean isBoolean(Object val) {
        return val instanceof Boolean;
    }

    public static boolean isNaN(Object val) {
        return val instanceof Double && ((Double) val).isNaN();
    }

    public static boolean isDate(Object val) {
        return val instanceof Date;
    }

    public static boolean isFunction(Object val) {
        return val instanceof Function;
    }

    /* public static boolean isRegex(Object val) {
        return val instanceof RegExp;
    }

    public static boolean isPromise(Object val) {
        return val instanceof Promise;
    }
    */

    public static String getType(Object val) {
        if (isObject(val)) {
            return "Object";
        } else if (isArray(val)) {
            return "Array";
        } else if (isNull(val)) {
            return "Null";
        } else if (isUndefined(val)) {
            return "Undefined";
        } else if (isNaN(val)) {
            return "NaN";
        } else if (isNumber(val)) {
            return "Number";
        } else if (isString(val)) {
            return "String";
        } else if (isBoolean(val)) {
            return "Boolean";
        } else if (isDate(val)) {
            return "Date";
        // } else if (isRegex(val)) {
            // return "Regex";
        } else if (isFunction(val)) {
            return "Function";
        // } else if (isPromise(val)) {
            // return "Promise";
        } else if (isInteger(val)) {
            return "Integer";
        } else {
            return "Unknown Type";
        }
    }
}
