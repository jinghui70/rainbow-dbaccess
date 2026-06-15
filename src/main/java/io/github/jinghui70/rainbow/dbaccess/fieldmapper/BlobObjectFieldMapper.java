package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class BlobObjectFieldMapper<T> extends BlobFieldMapper<T> {

    private final ObjectCodec<T> codec;

    private BlobObjectFieldMapper(ObjectCodec<T> codec) {
        this.codec = codec;
    }

    public BlobObjectFieldMapper(Class<T> fieldClass, Field field) {
        this.codec = new ObjectCodec<>(fieldClass, field);
    }

    @Override
    protected byte[] getBytes(Object value) {
        String json = codec.toJson(value);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected T parse(byte[] bytes) {
        String str = new String(bytes, StandardCharsets.UTF_8);
        return codec.parse(str);
    }

    public static <T> BlobObjectFieldMapper<T> of(Class<T> fieldClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.of(fieldClass));
    }

    public static <T> BlobObjectFieldMapper<T[]> ofArray(Class<T> componentClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofArray(componentClass));
    }

    public static <T> BlobObjectFieldMapper<List<T>> ofList(Class<T> componentClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofList(componentClass));
    }

    public static <T> BlobObjectFieldMapper<Map<String, T>> ofMap(Class<T> valueClass) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofMap(valueClass));
    }

    public static <T> BlobObjectFieldMapper<T> ofMap(Type complexType) {
        return new BlobObjectFieldMapper<>(ObjectCodec.ofType(complexType));
    }
}
