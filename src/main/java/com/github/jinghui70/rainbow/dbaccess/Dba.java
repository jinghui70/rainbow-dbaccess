package com.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.db.dialect.DriverUtil;
import com.github.jinghui70.rainbow.utils.StringBuilderX;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class Dba {

    protected JdbcTemplate jdbcTemplate;

    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected TransactionTemplate transactionTemplate;

    protected Dialect dialect = DialectDefault.INSTANCE;

    protected Dba() {
    }

    public Dba(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
               TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        String driver = DriverUtil.identifyDriver(dataSource).toLowerCase();
        if (driver.contains("oracle"))
            this.dialect = new DialectOracle();
    }
    
    public Dba(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
               TransactionTemplate transactionTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dialect = dialect;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public Sql sql() {
        return new Sql(this);
    }

    public Sql sql(String sql) {
        return new Sql(this).append(sql);
    }

    public Sql select(String select) {
        return sql("SELECT ").append(select);
    }

    public Sql update(String table) {
        return sql("UPDATE ").append(table);
    }

    public Sql deleteFrom(String table) {
        return sql("DELETE FROM ").append(table);
    }

    public NamedSql namedSql() {
        return new NamedSql(this);
    }

    public NamedSql namedSql(String sql) {
        return new NamedSql(this).append(sql);
    }

    /**
     * 插入一个对象
     *
     * @param bean 需要插入的对象
     */
    public int insert(Object bean) {
        String tableName = DbaUtil.tableName(bean.getClass());
        Map<String, Object> map = DbaUtil.beanToMap(bean, true);
        return insert(tableName, map);
    }

    public int insert(String tableName, Map<String, Object> map) {
        Sql sql = sql("insert into ").append(tableName).append("(");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sql.append(entry.getKey()).appendTempComma().addParam(entry.getValue());
        }
        sql.clearTemp().append(") values (").append("?", map.size(), ",").append(")");
        return sql.execute();
    }

    public void insert(Collection<?> beans) {
        if (CollUtil.isEmpty(beans))
            return;
        Object first = CollUtil.get(beans, 0);
        String tableName = DbaUtil.tableName(first.getClass());
        List<Map<String, Object>> data = beans.stream().map(bean -> DbaUtil.beanToMap(bean, false)).collect(toList());
        insert(tableName, data);
    }

    /**
     * 插入一组Map到指定的表里
     *
     * @param tableName 数据表名
     * @param data      数据
     */
    public void insert(String tableName, List<Map<String, Object>> data) {
        doInsert(tableName, data, "insert");
    }

    /**
     * 插入一组数据到指定的表里，如果已经存在就更新。这个函数H2支持，别的数据库未必支持
     *
     * @param tableName 数据表名
     * @param data      数据
     */
    public void merge(String tableName, List<Map<String, Object>> data) {
        doInsert(tableName, data, "merge");
    }

    private void doInsert(String tableName, List<Map<String, Object>> data, String type) {
        if (CollUtil.isEmpty(data))
            return;
        List<String> keys = new ArrayList<>(data.get(0).keySet());
        StringBuilderX sql = new StringBuilderX(type).append(" into ").append(tableName) //
                .append("(").join(keys).append(") values("); //
        for (String key : keys) {
            sql.append(":").append(key).appendTempComma();
        }
        sql.clearTemp().append(")");
        SqlParameterSource[] spss = data.stream().map(MapSqlParameterSource::new).toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql.toString(), spss);
    }

    /**
     * 更新一个对象
     *
     * @param bean 待更新对象对象
     * @return 更新数据库行数，正常情况下应该为1
     */
    public int update(Object bean) {
        String tableName = DbaUtil.tableName(bean.getClass());
        Map<String, Object> map = DbaUtil.beanToMap(bean, false);
        return update(tableName, map, DbaUtil.keyProps(bean.getClass()));
    }

    /**
     * 更新一个数据表记录
     *
     * @param tableName 数据表名
     * @param map       数据
     * @param keys      主键字段名
     * @return 更新的条数
     */
    public int update(String tableName, Map<String, Object> map, Collection<String> keys) {
        Assert.notEmpty(keys);
        Sql sql = sql("update ").append(tableName).append(" set ");
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (!CollUtil.contains(keys, field)) {
                sql.append(field);
                if (value == null)
                    sql.append("=null");
                else
                    sql.append("=?").addParam(value);
                sql.appendTempComma();
            }
        }
        sql.clearTemp();
        for (String key : keys) {
            Object value = map.get(key);
            Assert.notNull(value, "update key value should not be null");
            sql.where(key, value);
        }
        return sql.execute();
    }

    /**
     * 检查一个数据表是否存在
     *
     * @param tableName 数据表名
     * @return 是否存在
     */
    public boolean exist(String tableName) {
        String sql = String.format("SELECT COUNT(1) FROM %s where 1!=1", tableName);
        try {
            jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public void transaction(Runnable runnable) {
        transactionTemplate.execute(status -> {
            runnable.run();
            return null;
        });
    }

    public <T> T transaction(TransactionCallback<T> action) {
        return transactionTemplate.execute(action);
    }

}
