package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.utils.StringBuilderX;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.INSERT;
import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.MERGE;

public class ObjectDba<T> {

    protected Dba dba;
    protected Class<T> clazz;
    protected List<PropInfo> propArray;

    public ObjectDba(Dba dba, Class<T> clazz) {
        this.dba = dba;
        this.clazz = clazz;
        propArray = PropInfo.getPropInfoList(clazz);
    }

    protected String insertSql(String action, String table) {
        StringBuilderX sb = new StringBuilderX(action).append(" into ").append(table).append("(");
        int count = propArray.size();
        for (PropInfo propInfo : propArray) {
            if (propInfo.isAutoIncrement()) {
                count--;
                continue;
            }
            sb.append(propInfo.getFieldName()).appendTempComma();
        }
        sb.clearTemp().append(") values(").repeat("?", count).append(")");
        return sb.toString();
    }

    public int insert(T object) {
        return doInsert(DbaUtil.tableName(clazz), object, INSERT);
    }

    public int merge(T object) {
        return doInsert(DbaUtil.tableName(clazz), object, MERGE);
    }

    public int insert(String table, T object) {
        return doInsert(table, object, INSERT);
    }

    public int merge(String table, T object) {
        return doInsert(table, object, MERGE);
    }

    private int doInsert(String table, T object, String action) {
        Integer result = dba.getJdbcTemplate().execute(insertSql(action, table), (PreparedStatementCallback<Integer>) ps -> {
            setValues(ps, object, null);
            return ps.executeUpdate();
        });
        return result == null ? 0 : result;
    }

    public void insert(Collection<T> objects) {
        doInsert(DbaUtil.tableName(clazz), objects, INSERT, 0);
    }

    public void insert(String table, Collection<T> objects) {
        doInsert(table, objects, INSERT, 0);
    }

    public void insert(Collection<T> objects, int batchSize) {
        doInsert(DbaUtil.tableName(clazz), objects, INSERT, batchSize);
    }

    public void insert(String table, Collection<T> objects, int batchSize) {
        doInsert(table, objects, INSERT, batchSize);
    }

    public void merge(Collection<T> objects) {
        doInsert(DbaUtil.tableName(clazz), objects, MERGE, 0);
    }

    public void merge(String table, Collection<T> objects) {
        doInsert(table, objects, MERGE, 0);
    }

    public void merge(Collection<T> objects, int batchSize) {
        doInsert(DbaUtil.tableName(clazz), objects, MERGE, batchSize);
    }

    public void merge(String table, Collection<T> objects, int batchSize) {
        doInsert(table, objects, MERGE, batchSize);
    }

    public void doInsert(String table, Collection<T> objects, String action, int batchSize) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        dba.getJdbcTemplate().execute(insertSql(action, table), (PreparedStatementCallback<int[]>) ps -> {
            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                int i = 0;
                for (T t : objects) {
                    setValues(ps, t, nullTypeCache);
                    ps.addBatch();
                    if (batchSize > 0 && ++i == batchSize) {
                        ps.executeBatch();
                        i = 0;
                    }
                }
                if (batchSize == 0 || i > 0)
                    ps.executeBatch();
            } else {
                for (T t : objects) {
                    setValues(ps, t, nullTypeCache);
                    ps.executeUpdate();
                }
            }
            return new int[0]; // we don't care row's affect
        });
    }

    private void setValues(PreparedStatement ps, T object, Map<Integer, Integer> nullTypeCache) throws SQLException {
        int i = 1;
        for (PropInfo p : propArray) {
            if (p.isAutoIncrement()) continue;
            DbaUtil.setParameterValue(ps, i++, p.getValue(object), nullTypeCache);
        }
    }

    public int update(T object) {
        return update(DbaUtil.tableName(clazz), object);
    }

    public int update(String table, T object) {
        Sql sql = dba.update(table);
        for (PropInfo propInfo : propArray) {
            if (propInfo.getId() == null)
                sql.set(propInfo.getFieldName(), propInfo.getValue(object));
        }
        for (PropInfo propInfo : propArray) {
            if (propInfo.getId() != null)
                sql.where(propInfo.getFieldName(), propInfo.getValue(object));
        }
        return sql.execute();
    }

}
