package io.github.jinghui70.rainbow.dbaccess.map;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.*;
import io.github.jinghui70.rainbow.utils.StringBuilderX;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

import java.util.*;

public class MapHandler {

    private final Dba dba;

    public MapHandler(Dba dba) {
        this.dba = dba;
    }

    public int doInsert(String tableName, Map<String, Object> map, String action) {
        Sql sql = dba.sql(action).append(tableName).append("(");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sql.append(entry.getKey()).appendTempComma().addParam(entry.getValue());
        }
        sql.clearTemp().append(") values (").repeat("?", map.size()).append(")");
        return sql.execute();
    }

    public void doInsert(String tableName, List<Map<String, Object>> data, String action, int batchSize) {
        if (CollUtil.isEmpty(data))
            return;
        List<String> keys = new ArrayList<>(data.get(0).keySet());
        String sql = new StringBuilderX(action).append(tableName) //
                .append("(").join(keys).append(") values(")
                .repeat("?", keys.size())
                .append(")")
                .toString();
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        dba.getJdbcTemplate().execute(sql, (PreparedStatementCallback<int[]>) ps -> {
            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                int i = 0;
                for (Map<String, Object> t : data) {
                    for (String key : keys)
                        DbaUtil.setParameterValue(ps, i++, t.get(key), nullTypeCache);
                    ps.addBatch();
                    if (batchSize > 0 && ++i == batchSize) {
                        ps.executeBatch();
                        i = 0;
                    }
                }
                if (batchSize == 0 || i > 0)
                    ps.executeBatch();
            } else {
                int i = 1;
                for (Map<String, Object> t : data) {
                    for (String key : keys)
                        DbaUtil.setParameterValue(ps, i++, t.get(key), nullTypeCache);
                    ps.executeUpdate();
                }
            }
            return new int[0]; // we don't care row's affect
        });
    }

}
