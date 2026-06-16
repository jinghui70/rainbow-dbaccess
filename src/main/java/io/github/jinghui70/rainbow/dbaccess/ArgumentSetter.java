package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 扩展 Spring 的 {@link ArgumentPreparedStatementSetter}，使用 {@link DbaUtil#setParameterValue} 处理特殊类型参数。
 * <p>
 * 主要用于处理枚举、字段映射器等特殊类型的参数绑定，支持 {@link io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue} 包装类型。
 *
 * @author lijinghui
 * @see DbaUtil#setParameterValue
 */
public class ArgumentSetter extends ArgumentPreparedStatementSetter {

    /**
     * 构造函数，接收参数集合。
     *
     * @param args 参数集合
     */
    public ArgumentSetter(Collection<?> args) {
        super(args.toArray());
    }

    /**
     * 设置参数值，委托给 {@link DbaUtil#setParameterValue} 处理。
     *
     * @param ps               PreparedStatement 对象
     * @param parameterPosition 参数位置
     * @param argValue         参数值
     * @throws SQLException SQL 异常
     */
    @Override
    public void doSetValue(@NonNull PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
        DbaUtil.setParameterValue(ps, parameterPosition, argValue, null);
    }
}
