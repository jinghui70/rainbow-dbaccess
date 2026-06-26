package io.github.jinghui70.rainbow.dbaccess.sql;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.utils.StringBuilderX;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.jinghui70.rainbow.dbaccess.DbaUtil.*;

/**
 * 插入操作构建器，统一处理 Bean/Map、单条/数组/集合、默认表名/指定表名 的所有组合。
 * <p>
 * 构造时传入的 {@code data} 会被自动识别为以下 5 种形态之一：
 * <ul>
 *     <li>单条 Bean（任意非 Array/Collection/Map 对象）</li>
 *     <li>单条 Map（{@code Map<String,Object>}）</li>
 *     <li>Bean 数组 / Bean 集合（List、Set 等）</li>
 *     <li>Map 数组 / Map 集合</li>
 * </ul>
 * 数组与集合在执行时等价：均走批量 addBatch 路径。Map 数据必须通过 {@link #into(String)} 指定表名。
 * <p>
 * 典型用法：
 * <pre>
 *     dba.insertBuilder(bean).execute();
 *     dba.insertBuilder(beans).into("OTHER_TABLE").batchSize(500).execute();
 *     dba.insertBuilder(map).into("MY_TABLE").execute();
 *     dba.insertBuilder(bean).merge().execute();
 * </pre>
 */
public class InsertBuilder {

    private final Dba dba;

    /**
     * 统一存放数据，单条也包装成单元素列表
     */
    private final List<?> rows;

    /**
     * true: 行数据是 Map；false: 行数据是注解标注的 Bean
     */
    private final boolean isMap;

    /**
     * true 表示语义上是单条（影响 execute() 执行路径）
     */
    private final boolean single;

    private String tableName;

    private int batchSize = 0;

    private String action = INSERT_INTO;

    public InsertBuilder(Dba dba, @NonNull Object data) {
        this.dba = dba;

        Class<?> dataClass = data.getClass();
        if (dataClass.isArray()) {
            // 基本类型数组（int[]/long[]/...）显然不该用来插入
            Assert.isFalse(dataClass.getComponentType().isPrimitive(), "数据不合法");
            this.rows = Arrays.asList((Object[]) data);
            this.isMap = Map.class.isAssignableFrom(dataClass.getComponentType());
            this.single = false;
        } else if (data instanceof Collection<?> coll) {
            this.rows = List.copyOf(coll);
            // 集合泛型擦除，靠首元素探测；空集合默认按 Bean 处理（execute() 会直接返回 0）
            this.isMap = !rows.isEmpty() && rows.get(0) instanceof Map;
            this.single = false;
        } else if (data instanceof Map) {
            this.rows = Collections.singletonList(data);
            this.isMap = true;
            this.single = true;
        } else {
            this.rows = Collections.singletonList(data);
            this.isMap = false;
            this.single = true;
        }
    }

    /**
     * 指定表名
     */
    public InsertBuilder into(String tableName) {
        this.tableName = validTableName(tableName);
        return this;
    }

    /**
     * 通过类名指定表名
     */
    public InsertBuilder into(Class<?> clazz) {
        this.tableName = tableName(clazz);
        return this;
    }


    /**
     * 设置批量提交大小，仅对数组/集合插入有效。0（默认）表示一次性 addBatch 后 executeBatch。
     */
    public InsertBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * 切换为 MERGE INTO，行为同 INSERT 但已存在记录会被更新（仅部分数据库支持，如 H2）。
     */
    public InsertBuilder merge() {
        this.action = MERGE_INTO;
        return this;
    }

    /**
     * 执行插入。失败抛 {@link org.springframework.dao.DataAccessException}；不返回受影响行数。
     */
    public void execute() {
        if (rows.isEmpty()) return;
        RowBinder binder = isMap ? mapBinder() : beanBinder();
        String sql = buildSql(binder);
        if (single) executeSingle(sql, binder);
        else executeBatch(sql, binder);
    }

    /**
     * 构建 Sql
     *
     * @param binder 数据绑定器
     * @return Sql字符串
     */
    private String buildSql(RowBinder binder) {
        List<String> columns = binder.columns();
        return new StringBuilderX(action).append(binder.tableName())
                .append("(").join(columns).append(") values(")
                .repeat("?", columns.size(), StrUtil.COMMA).append(")")
                .toString();
    }

    /**
     * 单条数据插入执行
     *
     * @param sql    插入sql
     * @param binder 数据绑定器
     */
    private void executeSingle(String sql, RowBinder binder) {
        dba.getJdbcTemplate().execute(sql, (PreparedStatementCallback<Void>) ps -> {
            binder.bind(ps, rows.get(0), null);
            ps.executeUpdate();
            return null;
        });
    }

    /**
     * 多条数据插入执行
     *
     * @param sql    插入sql
     * @param binder 数据绑定器
     */
    private void executeBatch(String sql, RowBinder binder) {
        Map<Integer, Integer> nullTypeCache = new HashMap<>();
        dba.getJdbcTemplate().execute(sql, (PreparedStatementCallback<Void>) ps -> {
            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                int i = 0;
                for (Object row : rows) {
                    binder.bind(ps, row, nullTypeCache);
                    ps.addBatch();
                    if (batchSize > 0 && ++i == batchSize) {
                        ps.executeBatch();
                        i = 0;
                    }
                }
                if (batchSize == 0 || i > 0)
                    ps.executeBatch();
            } else {
                for (Object row : rows) {
                    binder.bind(ps, row, nullTypeCache);
                    ps.executeUpdate();
                }
            }
            return null;
        });
    }

    /**
     * 把 Bean 或 Map 的行内数据绑定到 PreparedStatement 上，并提供列名和表名。
     * <p>Bean 实现使用 {@link PropInfoCache}；Map 实现直接用 key 作为列名、value 作为参数。
     */
    private interface RowBinder {
        String tableName();

        List<String> columns();

        void bind(PreparedStatement ps, Object row, Map<Integer, Integer> nullTypeCache) throws SQLException;
    }

    private RowBinder beanBinder() {
        Class<?> clazz = rows.get(0).getClass();
        LinkedHashMap<String, PropInfo> propMap = PropInfoCache.get(clazz);
        List<PropInfo> props = propMap.values().stream()
                .filter(p -> !p.isAutoIncrement())
                .toList();
        List<String> columns = props.stream().map(PropInfo::getFieldName).collect(Collectors.toList());
        String table = tableName == null ? DbaUtil.tableName(clazz) : tableName;
        return new RowBinder() {
            @Override
            public String tableName() {
                return table;
            }

            @Override
            public List<String> columns() {
                return columns;
            }

            @Override
            public void bind(PreparedStatement ps, Object row, Map<Integer, Integer> nullTypeCache) throws SQLException {
                int i = 1;
                for (PropInfo p : props) {
                    Object value = p.getInsertValue(dba, row);
                    DbaUtil.setParameterValue(ps, i++, value, nullTypeCache);
                }
            }
        };
    }

    private RowBinder mapBinder() {
        Assert.notNull(tableName, "Map 插入必须指定表名");
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) rows.get(0);
        Assert.notEmpty(first, "Map 数据不能为空");
        List<String> columns = new ArrayList<>(first.keySet());
        return new RowBinder() {
            @Override
            public String tableName() {
                return tableName;
            }

            @Override
            public List<String> columns() {
                return columns;
            }

            @Override
            public void bind(PreparedStatement ps, Object row, Map<Integer, Integer> nullTypeCache) throws SQLException {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) row;
                int i = 1;
                for (String col : columns) {
                    DbaUtil.setParameterValue(ps, i++, map.get(col), nullTypeCache);
                }
            }
        };
    }
}
