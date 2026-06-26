package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.lang.Assert;
import cn.hutool.db.dialect.DriverNamePool;
import cn.hutool.db.dialect.DriverUtil;
import io.github.jinghui70.rainbow.dbaccess.dialect.Dialect;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectDefault;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectOracle;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectPostgreSQL;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.sql.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.keyArray;
import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.validTableName;

/**
 * 数据库访问核心对象，封装 {@link JdbcTemplate} 与 {@link TransactionTemplate}，提供 CRUD 快捷方法。
 * <p>
 * {@code Dba} 是 rainbow-dbaccess 的主入口，所有数据库操作均通过此类发起。
 * 它将 Spring JDBC 的 JdbcTemplate 和 TransactionTemplate 组合在一起，在保留 Spring JDBC 原生能力的同时，
 * 提供了更简洁、流畅的 API 来完成日常 CRUD 操作。
 * <p>
 * 典型用法示例：
 * <pre>{@code
 * // 构造 Dba
 * Dba dba = new Dba(dataSource);
 *
 * // 查询
 * User user = dba.selectByKey(User.class, "1");
 * List<User> users = dba.select().from("T_USER").where("AGE", Op.GT, 18).queryForList(User.class);
 *
 * // 插入
 * dba.insert(newUser);
 * dba.insertOf(mapData).into("T_USER").execute();
 *
 * // 更新
 * dba.update(user);
 * dba.update("T_USER").set("NAME", "NewName").where("ID", "1").execute();
 *
 * // 删除
 * dba.deleteByKey(User.class, "1");
 * dba.deleteFrom("T_USER").where("STATUS", "INACTIVE").execute();
 *
 * // 事务
 * dba.transaction(() -> {
 *     dba.insert(user1);
 *     dba.insert(user2);
 * });
 * }</pre>
 *
 * @author lijinghui
 * @see Sql
 * @see InsertBuilder
 * @see UpdateBuilder
 * @see UpdateSql
 * @see DeleteBuilder
 */
public class Dba {

    /** Spring JDBC 模板，用于执行 SQL 语句 */
    protected JdbcTemplate jdbcTemplate;

    /** Spring 事务模板，用于编程式事务管理 */
    protected TransactionTemplate transactionTemplate;

    /** 数据库方言，处理分页等数据库差异 */
    protected Dialect dialect = DialectDefault.INSTANCE;

    /** 保护级构造函数，供子类或框架扩展使用 */
    protected Dba() {
    }

    /**
     * 初始化数据源及相关组件，包括 JdbcTemplate、TransactionTemplate 和方言。
     *
     * @param dataSource 数据源
     * @param dialect    数据库方言，传 null 则自动识别
     */
    protected void initDataSource(DataSource dataSource, Dialect dialect) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.dialect = dialect == null ? identifyDialect() : dialect;
    }

    /**
     * 根据数据源构造 Dba，方言自动识别。
     * <p>H2/MySQL → {@link DialectDefault}，PostgreSQL/Kingbase → {@link DialectPostgreSQL}，Oracle → {@link DialectOracle}。
     *
     * @param dataSource 数据源
     */
    public Dba(DataSource dataSource) {
        initDataSource(dataSource, null);
    }

    /**
     * 根据数据源和自定义方言构造 Dba。
     *
     * @param dataSource 数据源
     * @param dialect    数据库方言，传 null 则自动识别
     */
    public Dba(DataSource dataSource, Dialect dialect) {
        initDataSource(dataSource, dialect);
    }

    /**
     * 根据 JdbcTemplate 和 TransactionTemplate 构造 Dba，方言自动识别。
     *
     * @param jdbcTemplate        Spring JDBC 模板
     * @param transactionTemplate Spring 事务模板
     */
    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dialect = identifyDialect();
    }

    /**
     * 根据 JdbcTemplate、TransactionTemplate 和自定义方言构造 Dba。
     *
     * @param jdbcTemplate        Spring JDBC 模板
     * @param transactionTemplate Spring 事务模板
     * @param dialect             数据库方言，传 null 则自动识别
     */
    public Dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dialect = dialect;
    }

    /**
     * 根据数据源自动识别数据库方言。
     * <p>H2/MySQL → {@link DialectDefault}，PostgreSQL/Kingbase → {@link DialectPostgreSQL}，Oracle → {@link DialectOracle}。
     *
     * @return 识别到的数据库方言
     */
    protected Dialect identifyDialect() {
        DataSource dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        String driver = DriverUtil.identifyDriver(dataSource);
        return switch (driver) {
            case DriverNamePool.DRIVER_KINGBASE8,
                 DriverNamePool.DRIVER_POSTGRESQL -> new DialectPostgreSQL();
            case DriverNamePool.DRIVER_ORACLE -> new DialectOracle();
            default -> DialectDefault.INSTANCE;
        };
    }

    /** @return 底层 JdbcTemplate */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /** @return 底层 TransactionTemplate */
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    /** @return 当前数据库方言 */
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * 以原始 SQL 片段创建 Sql 构建器，适用于需要完全控制 SQL 的场景。
     * <p>示例：{@code dba.sql("SELECT * FROM T_USER WHERE ID=?").addParam("1").queryForList(User.class)}
     *
     * @param sql SQL 片段
     * @return Sql 构建器
     */
    public Sql sql(String sql) {
        return new Sql(this).append(sql);
    }

    /**
     * 创建 SELECT * 查询构建器。
     * <p>示例：{@code dba.select().from("T_USER").where("ID", "1").queryForObject(User.class)}
     *
     * @return Sql 构建器
     */
    public Sql select() {
        return Sql.select().setDba(this);
    }

    /**
     * 创建指定字段的 SELECT 查询构建器。
     * <p>示例：{@code dba.select("NAME", "AGE").from("T_USER").queryForList(User.class)}
     *
     * @param fields 查询字段列表
     * @return Sql 构建器
     */
    public Sql select(String... fields) {
        return Sql.select(fields).setDba(this);
    }

    /**
     * 根据主键值查询单条记录，返回指定类型的实体对象。
     * <p>支持复合主键，keys 按实体类中 {@code @Id} 字段的声明顺序依次传入。
     * <p>示例：{@code dba.selectByKey(User.class, "1")}
     * <p>复合主键示例：{@code dba.selectByKey(ComplexEntity.class, "A", "B")}
     *
     * @param clazz 实体类，需用 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 标注主键
     * @param keys  主键值，多主键时按声明顺序传入
     * @param <T>   实体类型
     * @return 查询结果，不存在返回 null
     */
    public <T> T selectByKey(Class<T> clazz, Object... keys) {
        List<PropInfo> keyArray = keyArray(clazz);
        Assert.equals(keyArray.size(), keys.length, "argument size not match");
        Sql sql = select().from(DbaUtil.tableName(clazz));
        for (int i = 0; i < keyArray.size(); i++) {
            PropInfo propInfo = keyArray.get(i);
            sql.where(propInfo.getFieldName(), keys[i]);
        }
        return sql.queryForObject(BeanMapper.of(clazz));
    }

    /**
     * 插入数据的高频捷径，等价于 {@code insertOf(data).execute()}。
     * <p>支持 Bean / Bean 数组 / Bean 集合。Map 数据无法推导表名，请改用 {@link #insertOf(Object)} 指定表名。
     *
     * @param data 待插入的数据
     */
    public void insert(@NonNull Object data) {
        insertOf(data).execute();
    }

    /**
     * 创建插入构建器，支持 Bean/Map、单条/批量、自定义表名等场景。
     * <p>详见 {@link InsertBuilder} 的用法说明。
     *
     * @param data 待插入的数据
     * @return 插入构建器
     */
    public InsertBuilder insertOf(@NonNull Object data) {
        return new InsertBuilder(this, data);
    }

    /**
     * Merge（Insert Or Update）数据的高频捷径，等价于 {@code mergeOf(data).execute()}。
     * <p>支持 Bean / Bean 数组 / Bean 集合。Map 数据无法推导表名，请改用 {@link #mergeOf(Object)} 指定表名。
     * <p>注意：不是所有数据库都支持 MERGE 语法。
     *
     * @param data 待 Merge 的数据
     */
    public void merge(@NonNull Object data) {
        mergeOf(data).execute();
    }

    /**
     * 创建 Merge（Insert Or Update）构建器，支持 Bean/Map、单条/批量、自定义表名等场景。
     * <p>详见 {@link InsertBuilder} 的用法说明。
     * <p>注意：不是所有数据库都支持 MERGE 语法。
     *
     * @param data 待 Merge 的数据
     * @return 插入构建器（Merge 模式）
     */
    public InsertBuilder mergeOf(@NonNull Object data) {
        return new InsertBuilder(this, data).merge();
    }

    /**
     * 创建指定表的更新构建器（SQL 模式），通过 {@link UpdateSql#set} 手动指定 SET 字段和条件。
     * <p>示例：{@code dba.update("T_USER").set("NAME", "NewName").where("ID", "1").execute()}
     *
     * @param table 表名
     * @return 更新构建器
     */
    public UpdateSql update(String table) {
        return new UpdateSql(this, validTableName(table));
    }

    /**
     * 创建指定实体类对应表的更新构建器（SQL 模式），表名由 {@code @Table} 注解或类名自动推导。
     * <p>示例：{@code dba.update(User.class).set("NAME", "NewName").where("ID", "1").execute()}
     *
     * @param objClass 实体类
     * @return 更新构建器
     */
    public UpdateSql update(Class<?> objClass) {
        return new UpdateSql(this, DbaUtil.tableName(objClass));
    }

    /**
     * 按主键全量更新实体对象，以 {@code @Id} 字段生成 WHERE 条件，更新所有非主键字段。
     * <p>如需部分更新，请使用 {@link #updateOf(Object)} 配合 include/exclude/excludeNull。
     *
     * @param bean 包含主键的实体对象
     * @return 受影响行数
     */
    public int update(@NonNull Object bean) {
        return new UpdateBuilder(this, bean).execute();
    }

    /**
     * 创建更新构建器（Bean 模式），用于部分更新场景，如 include/exclude/excludeNull。
     * <p>以主键生成 WHERE 条件，需用 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 标注主键。
     *
     * @param bean 实体对象
     * @return 更新构建器
     */
    public UpdateBuilder updateOf(@NonNull Object bean) {
        return new UpdateBuilder(this, bean);
    }

    private static final String DELETE_FROM = "DELETE FROM ";

    /**
     * 创建指定表的 DELETE 语句构建器。
     * <p>示例：{@code dba.deleteFrom("T_USER").where("AGE", Op.LT, 18).execute()}
     *
     * @param table 表名
     * @return Sql 构建器，需继续拼接 WHERE 条件
     */
    public Sql deleteFrom(String table) {
        return sql(DELETE_FROM).append(validTableName(table));
    }

    /**
     * 创建指定实体类对应表的 DELETE 语句构建器，表名由 {@code @Table} 注解或类名自动推导。
     * <p>示例：{@code dba.deleteFrom(User.class).where("NAME", null).execute()}
     *
     * @param objClass 实体类
     * @return Sql 构建器，需继续拼接 WHERE 条件
     */
    public Sql deleteFrom(Class<?> objClass) {
        return sql(DELETE_FROM).append(DbaUtil.tableName(objClass));
    }

    /**
     * 按主键删除记录，支持 Bean / Bean 数组 / Bean 集合。
     * <p>需用 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 标注主键。
     * <p>示例：{@code dba.delete(existingUser)}
     *
     * @param data 待删除的数据
     * @return 受影响行数
     */
    public int delete(@NonNull Object data) {
        return DeleteBuilder.delete(this, data);
    }

    /**
     * 根据主键值删除记录，keys 按实体类中 {@code @Id} 字段的声明顺序依次传入。
     * <p>示例：{@code dba.deleteByKey(User.class, "1")}
     *
     * @param deleteClass 实体类，需用 {@link io.github.jinghui70.rainbow.dbaccess.annotation.Id} 标注主键
     * @param keys        主键值，多主键时按声明顺序传入
     * @return 受影响行数
     */
    public int deleteByKey(Class<?> deleteClass, Object... keys) {
        List<PropInfo> keyArray = keyArray(deleteClass);
        Assert.equals(keyArray.size(), keys.length, "argument size not match");
        Sql sql = deleteFrom(deleteClass);
        for (int i = 0; i < keyArray.size(); i++) {
            PropInfo propInfo = keyArray.get(i);
            sql.where(propInfo.getFieldName(), keys[i]);
        }
        return sql.execute();
    }

    /**
     * 在事务中执行操作，无返回值。
     * <p>示例：{@code dba.transaction(() -> { dba.insert(user1); dba.insert(user2); })}
     *
     * @param runnable 事务内要执行的操作
     */
    public void transaction(Runnable runnable) {
        transactionTemplate.execute(status -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在事务中执行操作并返回结果。
     * <p>示例：{@code int count = dba.transaction(status -> { dba.insert(user); return dba.select().from("T_USER").count(); })}
     *
     * @param action 事务回调
     * @param <T>    返回值类型
     * @return 事务回调的返回值
     */
    public <T> T transaction(TransactionCallback<T> action) {
        return transactionTemplate.execute(action);
    }

    /**
     * 检查数据表是否存在。
     * <p>通过执行 {@code SELECT 1 FROM tableName WHERE 1!=1} 来验证，若执行异常则认为表不存在。
     *
     * @param tableName 数据表名
     * @return 表存在返回 true，否则返回 false
     */
    public boolean exist(String tableName) {
        try {
            this.select("1").from(validTableName(tableName)).where("1!=1").queryForInt();
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    /**
     * 删除数据表（如果存在），等价于 {@code DROP TABLE IF EXISTS tableName}。
     *
     * @param tableName 数据表名
     */
    public void dropTable(String tableName) {
        this.sql("DROP TABLE IF EXISTS ").append(validTableName(tableName)).execute();
    }
}
