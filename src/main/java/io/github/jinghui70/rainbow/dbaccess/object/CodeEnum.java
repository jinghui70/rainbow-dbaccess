package io.github.jinghui70.rainbow.dbaccess.object;

import java.util.Arrays;
import java.util.Objects;

/**
 * 标记枚举值在存到数据库的时候，保存其 code
 */
public interface CodeEnum {

    /**
     * 返回该枚举值存入数据库时使用的 code。
     *
     * @return 存库用的 code，需在同一枚举类内唯一
     */
    String code();

    /**
     * 按 code 反查枚举值，用于从数据库读出时还原枚举。
     *
     * @param enumClass 枚举类，须同时实现 {@link CodeEnum}
     * @param code      存库的 code
     * @param <T>       枚举类型
     * @return 匹配的枚举值，没有匹配返回 null
     */
    static <T extends Enum<T> & CodeEnum> T codeToEnum(Class<T> enumClass, String code) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> Objects.equals(code, e.code()))
                .findAny().orElse(null);
    }
}
