package io.github.jinghui70.rainbow.dbaccess.crud;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
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

    protected final Dba dba;

    protected final Class<T> clazz;

    /**
     * @param dba        数据库访问对象
     * @param objectType 实体类
     */
    public CrudService(Dba dba, Class<T> objectType) {
        this.dba = dba;
        this.clazz = objectType;
    }

    /**
     * 插入前钩子，子类可覆写做校验或填充默认值。
     *
     * @param object 待插入对象
     */
    protected void beforeInsert(T object) {
    }

    /**
     * 插入单条记录。
     *
     * @param object 实体对象
     */
    public void insert(T object) {
        beforeInsert(object);
        dba.insert(object);
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

    /**
     * 按实体对象的主键值查询完整记录。
     * <p>
     * 适用于插入后获取自增主键等数据库自动生成的字段值。
     *
     * @param object 包含主键值的实体对象
     * @return 完整实体，未找到返回 {@code null}
     */
    public T getByObject(T object) {
        Sql sql = dba.select().from(DbaUtil.tableName(clazz));
        for (PropInfo propInfo : DbaUtil.keyArray(clazz)) {
            sql.where(propInfo.getFieldName(), propInfo.getValue(object));
        }
        return sql.queryForObject(clazz);
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

    /**
     * 分页查询，返回实体类型结果。
     *
     * @param dto 查询条件及分页参数
     * @return 分页数据
     */
    public PageData<T> queryPage(@NonNull QueryDTO dto) {
        return dto.setEntity(clazz).queryPage(dba, clazz);
    }

    /**
     * 列表查询，返回实体类型结果。
     *
     * @param dto 查询条件
     * @return 结果列表
     */
    public List<T> queryList(@NonNull QueryDTO dto) {
        return dto.setEntity(clazz).query(dba, clazz);
    }

    /**
     * 查询全部记录，按实体类默认排序。
     *
     * @return 结果列表
     */
    public List<T> getAll() {
        return dba.select().from(clazz)
                .orderBy(DbaUtil.defaultOrderBy(clazz))
                .queryForList(clazz);
    }

    /**
     * 分页查询，返回指定 VO 类型结果。
     *
     * @param dto     查询条件及分页参数
     * @param voClass VO 类型
     * @param <VT>    VO 类型
     * @return 分页数据
     */
    public <VT> PageData<VT> queryPage(@NonNull QueryDTO dto, Class<VT> voClass) {
        return dto.setEntity(clazz).queryPage(dba, voClass);
    }

    /**
     * 列表查询，返回指定 VO 类型结果。
     *
     * @param dto     查询条件
     * @param voClass VO 类型
     * @param <VT>    VO 类型
     * @return 结果列表
     */
    public <VT> List<VT> queryList(@NonNull QueryDTO dto, Class<VT> voClass) {
        return dto.setEntity(clazz).query(dba, voClass);
    }

    /**
     * 查询全部记录，返回指定 VO 类型结果。
     *
     * @param voClass VO 类型
     * @param <VT>    VO 类型
     * @return 结果列表
     */
    public <VT> List<VT> getAll(Class<VT> voClass) {
        return dba.select().from(clazz)
                .orderBy(DbaUtil.defaultOrderBy(clazz))
                .queryForList(voClass);
    }

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
     * 按主键值查询。
     *
     * @param keys 主键值，多主键时按声明顺序传入
     */
    public void deleteByKey(Object... keys) {
        dba.deleteByKey(clazz, keys);
    }
}
