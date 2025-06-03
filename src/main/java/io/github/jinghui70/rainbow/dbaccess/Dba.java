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
import org.springframework.lang.NonNull;
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

    /**
     * Dba类的构造函数
     *
     * @param dataSource 数据源对象，提供数据库连接
     *                   该构造函数用于初始化Dba对象。它接收一个参数：数据源（DataSource），该数据源对象负责提供数据库连接。
     *                   在构造函数内部，调用initDataSource方法来初始化数据源，其中方言参数传递为null，表示使用默认方言设置。
     */
    public Dba(DataSource dataSource) {
        initDataSource(dataSource, null);
    }

    /**
     * Dba类的构造函数
     *
     * @param dataSource 数据源对象，提供数据库连接
     * @param dialect    方言对象，定义数据库操作的特定行为
     *                   这个构造函数用于初始化Dba对象。它接收两个参数：数据源（DataSource）和方言（Dialect）。
     *                   数据源对象负责提供数据库连接，而方言对象则定义了针对特定数据库的特定操作行为。
     *                   在构造函数内部，调用initDataSource方法来使用提供的数据源和方言来初始化Dba对象。
     */
    public Dba(DataSource dataSource, Dialect dialect) {
        initDataSource(dataSource, dialect);
    }

    /**
     * Dba类的构造函数
     *
     * @param jdbcTemplate        JdbcTemplate对象，用于执行数据库操作
     * @param transactionTemplate TransactionTemplate对象，用于管理数据库事务
     *                            这个构造函数用于初始化Dba对象。它接收两个参数：JdbcTemplate和TransactionTemplate。
     *                            JdbcTemplate用于执行数据库操作，如查询、更新等。
     *                            TransactionTemplate用于管理数据库事务，确保数据库操作的原子性、一致性、隔离性和持久性。
     *                            在构造函数中，还调用了initDialect方法，该方法用于初始化方言配置，以确保数据库操作的正确性和效率。
     */
    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        initDialect();
    }

    /**
     * Dba类的构造函数
     *
     * @param jdbcTemplate        JdbcTemplate对象，用于执行数据库操作
     * @param transactionTemplate TransactionTemplate对象，用于管理数据库事务
     * @param dialect             方言对象，定义数据库操作的特定行为
     *                            此构造函数用于初始化Dba对象。它接收三个参数：JdbcTemplate、TransactionTemplate和Dialect。
     *                            JdbcTemplate用于执行数据库操作，如查询、更新等。
     *                            TransactionTemplate用于管理数据库事务，确保数据库操作的原子性、一致性、隔离性和持久性。
     *                            Dialect对象定义了针对特定数据库的特定操作行为，以确保数据库操作的正确性和效率。
     *                            在构造函数内部，这些参数分别被赋值给Dba对象的相应属性。
     */
    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dialect = dialect;
    }

    /**
     * 获取当前数据库连接的驱动名称
     *
     * @return 返回当前数据库连接的驱动名称
     */
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

    /**
     * 返回一个Sql对象
     *
     * @return 返回一个新的Sql对象，该对象使用当前Dba实例作为数据源
     */
    public Sql sql() {
        return new Sql(this);
    }

    /**
     * 创建一个包含指定SQL语句的Sql对象
     *
     * @param sql 需要附加的SQL语句
     * @return 返回一个包含指定SQL语句的Sql对象
     */
    public Sql sql(String sql) {
        return sql().append(sql);
    }

    /**
     * 创建一个用于执行SELECT *语句的Sql对象
     *
     * @return 返回一个包含"SELECT *" SQL语句的Sql对象
     */
    public Sql select() {
        return sql("SELECT *");
    }


    /**
     * 根据指定的字段生成查询用的 Sql对象。
     *
     * @param fields 需要查询的字段，可以传入一个或多个字段名。不传默认为 *
     * @return 根据字段生成的 Sql 对象。
     */
    public Sql select(String... fields) {
        if (fields.length == 0)
            return sql("SELECT *");
        if (fields.length == 1)
            return sql("SELECT ").append(fields[0]);
        return sql("SELECT ").join(fields);
    }

    /**
     * 创建一个用于执行SELECT * FROM查询的ObjectSql对象
     *
     * @param <T>         实体类的类型
     * @param selectClass 指定查询实体类
     * @return 返回一个配置好的ObjectSql对象，用于执行SELECT * FROM 实体类对应数据表的查询
     */
    public <T> ObjectSql<T> select(Class<T> selectClass) {
        return new ObjectSql<>(this, selectClass).append("SELECT * FROM ")
                .append(DbaUtil.tableName(selectClass));
    }

    /**
     * 创建一个用于执行自定义SELECT查询的ObjectSql对象，并替换查询中的占位符
     *
     * @param <T>         实体类的类型
     * @param selectClass 指定查询结果的实体类
     * @param replaceMap  包含占位符及其替换值的映射表
     * @return 返回一个配置好的ObjectSql对象，用于执行自定义SELECT查询
     * @see ObjectSql#selectFields(Map)
     */
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
    public <T> int insert(@NonNull T bean) {
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
    public <T> int merge(@NonNull T bean) {
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

    /**
     * 将一个对象列表批量插入到数据库中。
     *
     * @param beans     需要插入的对象列表
     * @param batchSize 批处理大小
     * @param <T>       对象类型
     */
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

    /**
     * 根据键删除数据
     *
     * @param deleteClass 待删除数据的类类型
     * @param keys        待删除数据的键
     * @param <T>         泛型参数，表示待删除数据的类类型
     * @return 删除的数据条数
     */
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
