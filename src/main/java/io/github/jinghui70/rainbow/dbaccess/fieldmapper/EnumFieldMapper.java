package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.util.EnumUtil;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.CodeEnum;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.OrdinalEnum;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class EnumFieldMapper<T extends Enum<T>> extends FieldMapper<T> {

    private final Class<T> enumClass;

    private final boolean isCode;

    private final boolean isOrdinal;

    public EnumFieldMapper(Class<T> enumClass) {
        this.enumClass = enumClass;
        isCode = CodeEnum.class.isAssignableFrom(enumClass);
        isOrdinal = OrdinalEnum.class.isAssignableFrom(enumClass);
    }

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        if (isOrdinal) {
            int value = rs.getInt(index);
            if (rs.wasNull()) return null;
            return EnumUtil.getEnumAt(enumClass, value);
        }
        String value = rs.getString(index);
        if (value == null) return null;
        if (isCode) {
            for (T t : enumClass.getEnumConstants()) {
                if (Objects.equals(value, ((CodeEnum) t).code()))
                    return t;
            }
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        if (isOrdinal)
            ps.setInt(paramIndex, ((Enum<?>) value).ordinal());
        else if (isCode)
            ps.setString(paramIndex, ((CodeEnum) value).code());
        else
            ps.setString(paramIndex, ((Enum<?>) value).name());
    }

    public static <T extends Enum<T>> EnumFieldMapper<T> of(Class<T> enumClass) {
        return new EnumFieldMapper<>(enumClass);
    }
}
