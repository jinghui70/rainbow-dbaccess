package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import org.springframework.lang.NonNull;

import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.keyArray;

public class DeleteBuilder {

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
     * 批量删除，按主键逐条执行 DELETE
     *
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
