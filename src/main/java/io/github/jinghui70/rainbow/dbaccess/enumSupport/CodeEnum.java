package io.github.jinghui70.rainbow.dbaccess.enumSupport;

import java.util.Arrays;
import java.util.Objects;

/**
 * 标记枚举值在存到数据库的时候，保存其 code
 */
public interface CodeEnum {

    String code();

    static <T extends Enum<T> & CodeEnum> T codeToEnum(Class<T> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> Objects.equals(code, e.code()))
                .findAny().orElse(null);
    }
}
