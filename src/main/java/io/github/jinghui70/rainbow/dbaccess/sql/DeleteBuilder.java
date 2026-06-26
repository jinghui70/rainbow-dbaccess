package io.github.jinghui70.rainbow.dbaccess.sql;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import org.springframework.lang.NonNull;

import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.keyArray;

/**
 * 删除操作，根据实体 Bean 或数组/集合的主键执行 DELETE。
 * <p>
 * 单条对象或数组/集合均以 {@code @Id} 主键生成 WHERE 条件。
 * 通过 {@link Dba#deleteFrom} 获取 SQL 构建器可实现更灵活的条件拼接。
 */
public class DeleteBuilder {

    /**
     * 执行删除。支持单条 Bean、Bean 数组、Bean 集合三种形态。
     *
     * @param dba  数据库访问对象
     * @param data 实体对象、数组或集合，需用 {@code @Id} 标注主键
     * @return 受影响行数
     */
    public static int delete(Dba dba, @NonNull Object data) {
        Class<?> dataClass = data.getClass();
        if (dataClass.isArray()) {
            return deleteList(dba, Arrays.asList((Object[]) data));
        } else if (data instanceof Collection<?> coll) {
            return deleteList(dba, List.copyOf(coll));
        } else {
            List<PropInfo> keyArray = keyArray(dataClass);
            Sql sql = dba.deleteFrom(dataClass);
            for (PropInfo propInfo : keyArray) {
                sql.where(propInfo.getFieldName(), propInfo.getValue(data));
            }
            return sql.execute();
        }
    }

    /**
     * 批量删除，按主键逐条执行 DELETE。
     *
     * @param dba  数据库访问对象
     * @param data 实体对象列表，需用 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 标注主键
     * @return 受影响行数合计
     */
    private static int deleteList(Dba dba, List<?> data) {
        if (CollUtil.isEmpty(data)) return 0;
        Class<?> clazz = data.get(0).getClass();
        List<PropInfo> keyArray = keyArray(clazz);
        Sql sql = dba.deleteFrom(clazz);
        for (PropInfo key : keyArray) {
            sql.where(key.getFieldName()).append("=?");
        }
        int result = 0;
        Object[] keys = new Object[keyArray.size()];
        for (Object object : data) {
            int i = 0;
            for (PropInfo propInfo : keyArray) {
                keys[i++] = propInfo.getValue(object);
            }
            result += sql.setParam(keys).execute();
        }
        return result;
    }

}
