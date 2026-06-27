package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.PropDesc;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;
import io.github.jinghui70.rainbow.dbaccess.annotation.GenerationTiming;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.valuegen.GenerateContext;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bean属性信息，封装了属性的元数据和访问方法。
 * 用于数据库列与Bean属性之间的映射转换。
 */
public class PropInfo {

    private final String fieldName;
    private final PropDesc propDesc;
    private final FieldMapper<?> mapper;
    private final Id id;
    private final GeneratedValue generatedValue;

    /**
     * 获取字段名（数据库列名，下划线风格）。
     *
     * @return 数据库列名
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 获取Id注解信息。
     *
     * @return Id注解实例，如果没有则返回null
     */
    public Id getId() {
        return id;
    }

    /**
     * 获取字段映射器。
     *
     * @return FieldMapper实例，如果没有则返回null
     */
    public FieldMapper<?> getMapper() {
        return mapper;
    }

    /**
     * 构造函数。
     *
     * @param fieldName 数据库列名
     * @param propDesc  属性描述
     * @param mapper    字段映射器
     */
    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper<?> mapper) {
        this.fieldName = fieldName;
        this.propDesc = propDesc;
        this.mapper = mapper;
        this.id = propDesc.getField().getAnnotation(Id.class);
        this.generatedValue = propDesc.getField().getAnnotation(GeneratedValue.class);
    }

    /**
     * 获取属性名（Bean字段名，驼峰风格）。
     *
     * @return Bean属性名
     */
    public String getName() {
        return propDesc.getRawFieldName();
    }

    /**
     * 从对象中取值，准备用来保存到数据库中
     *
     * @param object 取值的对象
     * @return 对应属性值
     */
    public Object getValue(Object object) {
        Object value = propDesc.getValue(object);
        if (value == null) return null;
        return mapper == null ? value : new FieldValue(value, mapper);
    }

    /**
     * 从数据库中取值
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     */
    public Object getValue(ResultSet rs, int index) throws SQLException {
        if (mapper != null)
            return mapper.formDB(rs, index);
        Class<?> type = propDesc.getFieldClass();
        return JdbcUtils.getResultSetValue(rs, index, type);
    }

    /**
     * 保存一个值到对象对应的属性中
     *
     * @param object 对象
     * @param value  需要保存的值
     */
    public void setValue(Object object, Object value) {
        propDesc.setValue(object, value);
    }

    /**
     * 判断是否为自增主键。
     *
     * @return 如果是自增主键返回true，否则返回false
     */
    public boolean isAutoIncrement() {
        Id id = getId();
        return id != null && id.autoIncrement();
    }

    /**
     * 获取用于插入语句的属性值。
     * <p>
     * 处理逻辑如下：
     * <ol>
     *   <li>若对象当前值非 {@code null}，直接返回（用户手动赋值优先）。</li>
     *   <li>若当前值为 {@code null} 且存在 {@link GeneratedValue} 注解，调用生成器生成新值。</li>
     *   <li>生成后，会将新值回填到入参对象（{@code row}）中，以便插入后获取该值。</li>
     *   <li>若既无值也无注解，返回 {@code null}。</li>
     * </ol>
     *
     * @param dba 数据库访问对象，传递给生成器供其使用
     * @param row 当前行对象
     * @return 用于插入的值
     */
    public Object getInsertValue(Dba dba, Object row) {
        Object value = getValue(row);
        // 1. 用户已设值，直接使用，不进行自动生成
        if (value != null) return value;

        // 2. 无注解且无值，返回 null
        if (generatedValue == null) return null;

        // 3. 执行生成逻辑
        ValueGenerator generator = ValueGeneratorRegistry.get(generatedValue.strategy());
        GenerateContext context = new GenerateContext(dba, row, propDesc.getField(), generatedValue.param());
        Object result = generator.generate(context);

        // 4. 回填对象，确保插入后 entity 中有值
        propDesc.setValue(row, result);

        // 5. 处理类型映射转换
        return mapper == null ? result : new FieldValue(result, mapper);
    }

    /**
     * 获取用于更新语句的属性值。
     * <p>
     * 处理逻辑如下：
     * <ol>
     *   <li>若未标注 {@link GeneratedValue} 或注解策略为 {@link GenerationTiming#INSERT}，则直接返回对象当前值。</li>
     *   <li>若注解策略为 {@link GenerationTiming#INSERT_UPDATE}，则忽略对象当前值，强制调用生成器生成新值。</li>
     *   <li>生成后，会将新值回填到入参对象（{@code row}）中。</li>
     * </ol>
     * <p>
     * <b>注意：</b> 对于 {@link GenerationTiming#INSERT_UPDATE} 的字段（如更新时间），更新操作会强制覆盖原有值，
     * 即使在 Java 对象中手动修改了该字段，也会被生成器的新值替代。
     *
     * @param dba 数据库访问对象，传递给生成器供其使用
     * @param row 当前行对象
     * @return 用于更新的值
     */
    public Object getUpdateValue(Dba dba, Object row) {
        // 只有 INSERT_UPDATE 策略才在更新时触发生成
        if (generatedValue == null || generatedValue.timing() != GenerationTiming.INSERT_UPDATE) {
            return getValue(row);
        }

        // 强制生成并覆盖
        ValueGenerator generator = ValueGeneratorRegistry.get(generatedValue.strategy());
        GenerateContext context = new GenerateContext(dba, row, propDesc.getField(), generatedValue.param());
        Object result = generator.generate(context);

        // 回填对象
        propDesc.setValue(row, result);

        return mapper == null ? result : new FieldValue(result, mapper);
    }

}
