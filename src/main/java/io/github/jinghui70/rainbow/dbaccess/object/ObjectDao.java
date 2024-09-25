package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.lang.Assert;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.utils.StringBuilderX;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.INSERT;
import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.MERGE;

public class ObjectDao<T> {

    protected Dba dba;
    protected Class<T> clazz;
    protected LinkedHashMap<String, PropInfo> propMap;
    protected List<PropInfo> keyArray;

    public ObjectDao(Dba dba, Class<T> clazz) {
        this.dba = dba;
        this.clazz = clazz;
        propMap = PropInfoCache.get(clazz);
        keyArray = propMap.values().stream().filter(p -> p.getId() != null).collect(Collectors.toList());
    }

    protected String insertSql(String action, String table) {
        StringBuilderX sb = new StringBuilderX(action).append(" into ").append(table).append("(");
        int count = propMap.size();
        for (PropInfo propInfo : propMap.values()) {
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
        for (PropInfo p : propMap.values()) {
            if (p.isAutoIncrement()) continue;
            DbaUtil.setParameterValue(ps, i++, p.getValue(object), nullTypeCache);
        }
    }

    public int update(T object) {
        return update(DbaUtil.tableName(clazz), object);
    }

    public int update(String table, T object) {
        Assert.isTrue(keyArray.size() > 0, "no key field defined");
        Sql sql = dba.update(table);
        for (PropInfo propInfo : propMap.values()) {
            if (propInfo.getId() == null) {
                sql.set(propInfo.getFieldName(), propInfo.getValue(object));
            }
        }
        for (PropInfo propInfo : keyArray) {
            if (propInfo.getId() != null)
                sql.where(propInfo.getFieldName(), propInfo.getValue(object));
        }
        return sql.execute();
    }

    public T selectById(Object id) {
        return dba.selectAll().from(DbaUtil.tableName(clazz)).where("id", id)
                .queryForObject(getMapper());
    }

    public T selectByKey(Object... keys) {
        Assert.isTrue(keyArray.size() > 0, "no key field defined");
        Assert.equals(keyArray.size(), keys.length, "argument size not match");
        Sql sql = dba.selectAll().from(DbaUtil.tableName(clazz));
        for (int i = 0; i < keyArray.size(); i++) {
            PropInfo propInfo = keyArray.get(i);
            sql.where(propInfo.getFieldName(), keys[i]);
        }
        return sql.queryForObject(getMapper());
    }

    public BeanMapper<T> getMapper() {
        return new BeanMapper<>(clazz, propMap);
    }

    public int delete(T object) {
        Assert.isTrue(keyArray.size() > 0, "no key field defined");
        Sql sql = dba.deleteFrom(DbaUtil.tableName(clazz));
        for (PropInfo propInfo : keyArray) {
            sql.where(propInfo.getFieldName(), propInfo.getValue(object));
        }
        return sql.execute();
    }

    public int deleteById(Object id) {
        return dba.deleteFrom(DbaUtil.tableName(clazz)).where("id", id).execute();
    }

    public int deleteByKey(Object... keys) {
        Assert.isTrue(keyArray.size() > 0, "no key field defined");
        Assert.equals(keyArray.size(), keys.length, "argument size not match");
        Sql sql = dba.deleteFrom(DbaUtil.tableName(clazz));
        for (int i = 0; i < keyArray.size(); i++) {
            PropInfo propInfo = keyArray.get(i);
            sql.where(propInfo.getFieldName(), keys[i]);
        }
        return sql.execute();
    }
}
