package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.db.dialect.DriverUtil;
import io.github.jinghui70.rainbow.dbaccess.dialect.Dialect;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectDefault;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectOracle;
import io.github.jinghui70.rainbow.dbaccess.map.MapHandler;
import io.github.jinghui70.rainbow.dbaccess.object.ObjectDao;
import io.github.jinghui70.rainbow.dbaccess.object.ObjectSql;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.INSERT_INTO;
import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.MERGE_INTO;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Dba {

    protected JdbcTemplate jdbcTemplate;

    protected TransactionTemplate transactionTemplate;

    protected Dialect dialect = DialectDefault.INSTANCE;

    protected Dba() {
    }

    protected void initDataSource(DataSource dataSource, Dialect dialect) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        if (dialect != null)
            this.dialect = dialect;
        else
            initDialect();
    }

    public Dba(DataSource dataSource) {
        initDataSource(dataSource, null);
    }

    public Dba(DataSource dataSource, Dialect dialect) {
        initDataSource(dataSource, dialect);
    }

    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        initDialect();
    }

    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dialect = dialect;
    }

    public String getDriver() {
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        return DriverUtil.identifyDriver(dataSource);
    }

    protected void initDialect() {
        String driver = getDriver();
        if (driver == null) return;
        if (driver.toLowerCase().contains("oracle"))
            this.dialect = new DialectOracle();
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
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
        return sql().append(sql);
    }

    public Sql select() {
        return sql("SELECT *");
    }

    public Sql select(String select) {
        return sql("SELECT ").append(select);
    }

    public <T> ObjectSql<T> select(Class<T> selectClass) {
        return new ObjectSql<>(this, selectClass).append("SELECT * FROM ").append(DbaUtil.tableName(selectClass));
    }

    public <T> ObjectSql<T> select(Class<T> selectClass, Map<String, Object> replaceMap) {
        return new ObjectSql<>(this, selectClass).selectFields(replaceMap);
    }

    public Sql update(String table) {
        return sql("UPDATE ").append(table);
    }

    public <T> ObjectSql<T> update(Class<T> updateClass) {
        return new ObjectSql<>(this, updateClass).append("UPDATE ").append(DbaUtil.tableName(updateClass));
    }

    public <T> ObjectSql<T> insertInto(Class<T> insertClass) {
        return new ObjectSql<>(this, insertClass).insertInto();
    }

    public Sql deleteFrom(String table) {
        return sql("DELETE FROM ").append(table);
    }

    public <T> ObjectSql<T> deleteFrom(Class<T> deleteClass) {
        return new ObjectSql<>(this, deleteClass).append("DELETE FROM ").append(DbaUtil.tableName(deleteClass));
    }

    /**
     * 插入一个对象
     *
     * @param bean 需要插入的对象
     * @param <T>  对象泛型
     * @return 插入改变的行数，正常应该是1
     */
    @SuppressWarnings("unchecked")
    public <T> int insert(T bean) {
        Assert.notNull(bean, "can't insert null object");
        return new ObjectDao<>(this, (Class<T>) bean.getClass()).insert(bean);
    }

    /**
     * 插入一个对象，如果已经存在就更新。这个函数H2支持，别的数据库未必支持
     *
     * @param bean 需要插入的对象
     * @param <T>  对象泛型
     * @return 插入改变的行数，正常应该是1
     */
    @SuppressWarnings("unchecked")
    public <T> int merge(T bean) {
        Assert.notNull(bean, "can't insert null object");
        return new ObjectDao<>(this, (Class<T>) bean.getClass()).merge(bean);
    }

    /**
     * 插入一组数据
     *
     * @param beans 数据的集合
     * @param <T>   对象泛型
     */
    public <T> void insert(List<T> beans) {
        insert(beans, 0);
    }

    @SuppressWarnings("unchecked")
    public <T> void insert(List<T> beans, int batchSize) {
        if (CollUtil.isEmpty(beans)) return;
        new ObjectDao<>(this, (Class<T>) beans.get(0).getClass()).insert(beans, batchSize);
    }

    /**
     * 插入一组数据，如果已经存在就更新。这个函数H2支持，别的数据库未必支持
     *
     * @param beans 数据的集合
     * @param <T>   对象泛型
     */
    @SuppressWarnings("unchecked")
    public <T> void merge(List<T> beans) {
        if (CollUtil.isEmpty(beans)) return;
        new ObjectDao<>(this, (Class<T>) beans.get(0).getClass()).merge(beans);
    }

    /**
     * 插入一个map到一个数据表中
     *
     * @param tableName 表名
     * @param map       map对象
     * @return 插入改变的行数，正常应该是1
     */
    public int insert(String tableName, Map<String, Object> map) {
        return new MapHandler(this).doInsert(tableName, map, INSERT_INTO);
    }

    /**
     * 插入一条记录到指定的表里，如果已经存在就更新。这个函数H2支持，别的数据库未必支持
     *
     * @param tableName 数据表名
     * @param map       map对象
     * @return 插入改变的行数，正常应该是1
     */
    public int merge(String tableName, Map<String, Object> map) {
        return new MapHandler(this).doInsert(tableName, map, MERGE_INTO);
    }

    /**
     * 插入一组Map到指定的表里
     *
     * @param tableName 数据表名
     * @param data      数据列表
     */
    public void insert(String tableName, List<Map<String, Object>> data) {
        new MapHandler(this).doInsert(tableName, data, INSERT_INTO, 0);
    }

    /**
     * 批量插入数据到指定的表里
     *
     * @param tableName 数据表名
     * @param data      数据的集合
     * @param batchSize 批量大小，如果小于1则不开启批量模式
     */
    public void insert(String tableName, List<Map<String, Object>> data, int batchSize) {
        new MapHandler(this).doInsert(tableName, data, INSERT_INTO, batchSize);
    }

    /**
     * 插入一组数据到指定的表里，如果已经存在就更新。这个函数H2支持，别的数据库未必支持
     *
     * @param tableName 数据表名
     * @param data      数据
     */
    public void merge(String tableName, List<Map<String, Object>> data) {
        new MapHandler(this).doInsert(tableName, data, MERGE_INTO, 0);
    }


    /**
     * 更新一个对象
     *
     * @param bean 待更新对象对象
     * @param <T>  对象泛型
     * @return 更新数据库行数，正常情况下应该为1
     */
    @SuppressWarnings("unchecked")
    public <T> int update(T bean) {
        Assert.notNull(bean);
        return new ObjectDao<>(this, (Class<T>) bean.getClass()).update(bean);
    }

    /**
     * 更新一个对象
     *
     * @param tableName 表名
     * @param bean      待更新对象对象
     * @param <T>       对象泛型
     * @return 更新数据库行数，正常情况下应该为1
     */
    @SuppressWarnings("unchecked")
    public <T> int update(String tableName, T bean) {
        Assert.notNull(bean);
        return new ObjectDao<>(this, (Class<T>) bean.getClass()).update(tableName, bean);
    }

    @SuppressWarnings("unchecked")
    public <T> int delete(T object) {
        return new ObjectDao<>(this, (Class<T>) object.getClass()).delete(object);
    }

    public <T> int deleteByKey(Class<T> deleteClass, Object... keys) {
        return new ObjectDao<>(this, deleteClass).deleteByKey(keys);
    }

    public <T> T selectByKey(Class<T> selectClass, Object... keys) {
        return new ObjectDao<>(this, selectClass).selectByKey(keys);
    }

    /**
     * 检查一个数据表是否存在
     *
     * @param tableName 数据表名
     * @return 是否存在
     */
    public boolean exist(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s where 1!=1", tableName);
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
