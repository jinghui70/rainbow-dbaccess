package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;

import java.util.function.Function;

public enum KeyType implements Function<String, String> {

    UPPER_CASE{
        @Override
        public String apply(String s) {
            return s.toUpperCase();
        }
    },
    LOWER_CASE{
        @Override
        public String apply(String s) {
            return s.toLowerCase();
        }
    },
    CAMEL_CASE{
        @Override
        public String apply(String s) {
            return StrUtil.toCamelCase(s);
        }
    }


}
