package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ObjectFieldMapper<T> extends FieldMapper<T> {

    private final ObjectCodec<T> codec;

    private ObjectFieldMapper(ObjectCodec<T> codec) {
        this.codec = codec;
    }

    public ObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.codec = new ObjectCodec<>(fieldClass, field);
    }

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        return codec.parse(str);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        String str = codec.toJson(value);
        ps.setString(paramIndex, str);
    }

    public static <T> ObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new ObjectFieldMapper<>(ObjectCodec.of(fieldClass));
    }

    public static <T> ObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofArray(componentClass));
    }

    public static <T> ObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofList(componentClass));
    }

    public static <T> ObjectFieldMapper<Map<String, T>> ofMap(Class<T> valueClass) {
        return new ObjectFieldMapper<>(ObjectCodec.ofMap(valueClass));
    }

    public static <T> ObjectFieldMapper<T> ofMap(Type complexType) {
        return new ObjectFieldMapper<>(ObjectCodec.ofType(complexType));
    }
}
