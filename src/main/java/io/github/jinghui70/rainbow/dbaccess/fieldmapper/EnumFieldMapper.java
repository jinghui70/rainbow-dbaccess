package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import io.github.jinghui70.rainbow.dbaccess.CodeEnum;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class EnumFieldMapper<T extends Enum<T>> extends FieldMapper<T> {

    private final Class<T> enumClass;

    private final boolean isCode;

    public EnumFieldMapper(Class<T> enumClass) {
        this.enumClass = enumClass;
        isCode = CodeEnum.class.isAssignableFrom(enumClass);
    }

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
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
        if (isCode)
            ps.setString(paramIndex, ((CodeEnum) value).code());
        else
            ps.setString(paramIndex, ((Enum<?>) value).name());
    }

    public static <T extends Enum<T>> EnumFieldMapper<T> of(Class<T> enumClass) {
        return new EnumFieldMapper<>(enumClass);
    }
}
