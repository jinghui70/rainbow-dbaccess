package io.github.jinghui70.rainbow.utils;

import java.util.Arrays;
import java.util.Objects;

public interface CodeEnum {

    String code();

    static <T extends Enum<T> & CodeEnum> T codeToEnum(Class<T> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> Objects.equals(code, e.code()))
                .findAny().orElse(null);
    }
}
