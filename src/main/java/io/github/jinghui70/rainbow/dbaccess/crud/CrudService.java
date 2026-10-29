package io.github.jinghui70.rainbow.dbaccess.crud;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 通用 CRUD 服务基类，封装对指定实体类的增删改查操作。
 * <p>
 * 提供插入前后、更新前后的钩子方法供子类覆写。
 * 支持通过 {@link QueryDTO} 灵活分页查询和列表查询，并可指定 VO 类型做结果映射。
 *
 * @param <T> 实体类型
 */
public class CrudService<T> {

    /** 数据库访问对象 */
    protected final Dba dba;

    /** 本服务操作的实体类 */
    protected final Class<T> clazz;

    /**
     * 构造指定实体类型的 CRUD 服务。
     *
     * @param dba        数据库访问对象
     * @param objectType 实体类
     */
    public CrudService(Dba dba, Class<T> objectType) {
        this.dba = dba;
        this.clazz = objectType;
    }

    // ===== 插入 =====

    /**
     * 插入前钩子，子类可覆写做校验或填充默认值。
     *
     * @param object 待插入对象
     */
    protected void beforeInsert(T object) {
    }

    /**
     * 插入单条记录。
     * <p>插入前会调用 {@link #beforeInsert(Object)} 钩子。
     *
     * @param object 实体对象
     * @return 插入后的实体对象，即传入的 object 本身（可能已被钩子填充默认值）
     */
    public T insert(T object) {
        beforeInsert(object);
        dba.insert(object);
        return object;
    }

    /**
     * 批量插入。
     *
     * @param objects 实体对象集合
     */
    public void insert(Collection<T> objects) {
        if (CollUtil.isEmpty(objects)) return;
        objects.forEach(this::beforeInsert);
        dba.insert(objects);
    }

    /**
     * 批量插入，指定批次大小。
     *
     * @param objects   实体对象集合
     * @param batchSize 每批提交数量
     */
    public void insert(Collection<T> objects, int batchSize) {
        objects.forEach(this::beforeInsert);
        dba.insertOf(objects).batchSize(batchSize).execute();
    }

    // ===== 更新 =====

    /**
     * 更新前钩子，子类可覆写做校验或填充更新时间等。
     *
     * @param object 待更新对象
     */
    protected void beforeUpdate(T object) {
    }

    /**
     * 按主键全量更新。
     *
     * @param object 包含主键的实体对象
     * @return 受影响行数
     */
    public int update(T object) {
        beforeUpdate(object);
        return dba.update(object);
    }

    /**
     * 增量更新，仅更新 {@link UpdateDTO#getChangedProps()} 指定的字段。
     *
     * @param dto 包含记录和变更属性列表的 DTO
     * @return 受影响行数
     */
    public int deltaUpdate(UpdateDTO<T> dto) {
        if (CollUtil.isEmpty(dto.getChangedProps())) return 0;
        return dba.updateOf(dto.getRecord()).include(dto.getChangedProps()).execute();
    }

    // ===== 按主键查单条 =====

    /**
     * 按实体对象的主键值查询完整记录。
     * <p>
     * 适用于插入后获取自增主键等数据库自动生成的字段值。
     *
     * @param object 包含主键值的实体对象
     * @return 完整实体，未找到返回 {@code null}
     */
    public T getByObject(T object) {
        return dba.select().from(clazz).whereByObj(object).queryForObject(clazz);
    }

    /**
     * 按主键值查询。
     *
     * @param keys 主键值，多主键时按声明顺序传入
     * @return 实体对象，未找到返回 {@code null}
     */
    public T getByKey(Object... keys) {
        return dba.selectByKey(clazz, keys);
    }

    /**
     * 按主键值查询，返回指定 VO 类型结果。
     * <p>
     * 与 {@link #getByKey(Object...)} 的区别是结果直接映射为 {@code voClass}，不必先查出实体再转换。
     * 查询仍按实体类 {@code clazz} 的 {@code @Id} 生成 WHERE 条件，所以 keys 依旧按实体主键的声明顺序传入。
     * <p>
     * 不叫 {@code getByKey} 是因为它与 {@code getByKey(Object...)} 的参数空间重叠（{@code Object...}
     * 能接受 {@code Class + keys} 这段实参），解析结果会随实参静态类型漂移，容易绑错版本。
     *
     * @param voClass VO 类型
     * @param keys    主键值，多主键时按声明顺序传入
     * @param <VT>    VO 类型
     * @return VO 对象，未找到返回 {@code null}
     */
    public <VT> VT getByKeyAs(Class<VT> voClass, Object... keys) {
        return dba.select().from(clazz).whereByKey(clazz, keys).queryForObject(voClass);
    }

    // ===== 条件查询 =====

    /**
     * 分页查询，返回实体类型结果，可指定默认排序。
     * <p>
     * 排序写进 QueryDTO 作为默认排序——客户端传了 {@code orderBys} 时仍以客户端为准；不传则不设默认排序。
     *
     * @param dto             查询条件及分页参数
     * @param defaultOrderBys 默认排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @return 分页数据
     */
    public PageData<T> queryPage(@NonNull QueryDTO dto, String... defaultOrderBys) {
        return dto.setEntity(clazz).defaultOrderBy(defaultOrderBys).queryPage(dba, clazz);
    }

    /**
     * 分页查询，返回指定 VO 类型结果，可指定默认排序。
     *
     * @param dto             查询条件及分页参数
     * @param voClass         VO 类型
     * @param defaultOrderBys 默认排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @param <VT>            VO 类型
     * @return 分页数据
     */
    public <VT> PageData<VT> queryPage(@NonNull QueryDTO dto, Class<VT> voClass, String... defaultOrderBys) {
        return dto.setEntity(clazz).defaultOrderBy(defaultOrderBys).queryPage(dba, voClass);
    }

    /**
     * 列表查询，返回实体类型结果，可指定默认排序。
     * <p>
     * 排序写进 QueryDTO 作为默认排序——客户端传了 {@code orderBys} 时仍以客户端为准；不传则不设默认排序。
     *
     * @param dto             查询条件
     * @param defaultOrderBys 默认排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @return 结果列表
     */
    public List<T> queryList(@NonNull QueryDTO dto, String... defaultOrderBys) {
        return dto.setEntity(clazz).defaultOrderBy(defaultOrderBys).query(dba, clazz);
    }

    /**
     * 列表查询，返回指定 VO 类型结果，可指定默认排序。
     *
     * @param dto             查询条件
     * @param voClass         VO 类型
     * @param defaultOrderBys 默认排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @param <VT>            VO 类型
     * @return 结果列表
     */
    public <VT> List<VT> queryList(@NonNull QueryDTO dto, Class<VT> voClass, String... defaultOrderBys) {
        return dto.setEntity(clazz).defaultOrderBy(defaultOrderBys).query(dba, voClass);
    }

    /**
     * 查询全部记录，返回实体类型结果。
     * <p>
     * 传了 {@code orderBys} 就完全按它排序；不传才按实体主键排序（{@code getAll()} 即此用法）。
     * <p>
     * 注意这里不追加主键兜底，与 {@link QueryDTO} 的分页排序不同：全量查询没有翻页边界，
     * 不依赖主键保证行不重不漏。
     *
     * @param orderBys 排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @return 结果列表
     */
    public List<T> getAll(String... orderBys) {
        return dba.select().from(clazz)
                .orderBy(orderOf(orderBys))
                .queryForList(clazz);
    }

    /**
     * 查询全部记录，返回指定 VO 类型结果。
     * <p>
     * 传了 {@code orderBys} 就完全按它排序；不传才按实体主键排序（{@code getAll(voClass)} 即此用法）。
     *
     * @param voClass  VO 类型
     * @param orderBys 排序字段，降序用 {@link io.github.jinghui70.rainbow.dbaccess.DbaUtil#desc(String)} 拼接
     * @param <VT>     VO 类型
     * @return 结果列表
     */
    public <VT> List<VT> getAll(Class<VT> voClass, String... orderBys) {
        return dba.select().from(clazz)
                .orderBy(orderOf(orderBys))
                .queryForList(voClass);
    }

    // ===== 删除 =====

    /**
     * 删除记录，支持单条、数组或集合。
     *
     * @param object 实体、数组或集合
     * @return 受影响行数
     */
    public int delete(Object object) {
        return dba.delete(object);
    }

    /**
     * 按主键值删除。
     *
     * @param keys 主键值，多主键时按声明顺序传入
     */
    public void deleteByKey(Object... keys) {
        dba.deleteByKey(clazz, keys);
    }

    // ===== 私有工具 =====

    /**
     * 组装 ORDER BY 字段：传了排序字段就完全用它，没传才回退到实体主键的默认排序。
     * <p>
     * 与 {@link QueryDTO} 不同，这里不追加主键兜底——{@code getAll} 是一次性取全量、没有翻页边界，
     * 不需要为主键唯一性付出去重与合并的代价。
     * <p>
     * 无 {@code @Id} 且没传排序字段时返回空数组，避免把 {@code null} 交给 {@code orderBy} 而抛 NPE。
     *
     * @param orderBys 调用方传入的排序字段
     * @return 最终参与排序的字段，可能为空
     */
    private String[] orderOf(String... orderBys) {
        if (orderBys != null && orderBys.length > 0) return orderBys;
        return PropInfoCache.get(clazz).values().stream()
                .filter(prop -> prop.getId() != null)
                .map(PropInfo::getFieldName)
                .toArray(String[]::new);
    }
}
