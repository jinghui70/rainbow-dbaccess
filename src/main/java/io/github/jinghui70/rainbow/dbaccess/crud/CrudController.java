package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 通用 CRUD Controller 基类
 * <p>
 * 提供 insert、update、deltaUpdate、getByKey、query、queryList、queryAll、delete、deleteBatch 等端点。
 * 可通过 {@code @RequestMapping} 注解在子类上统一指定路径前缀。
 * <p>
 * 默认映射：
 * <ul>
 *     <li>POST /insert</li>
 *     <li>POST /update</li>
 *     <li>POST /deltaUpdate</li>
 *     <li>POST /getByKey</li>
 *     <li>POST /query、/queryList</li>
 *     <li>POST /delete、/deleteBatch</li>
 * </ul>
 *
 * @param <T> 实体类型
 */
public abstract class CrudController<T> {

    protected final CrudService<T> service;

    /**
     * 使用外部构造的 {@link CrudService} 实例。
     *
     * @param service CRUD 服务
     */
    protected CrudController(CrudService<T> service) {
        this.service = service;
    }

    /**
     * 自动创建 {@link CrudService} 实例。
     *
     * @param dba        数据库访问对象
     * @param objectType 实体类
     */
    protected CrudController(Dba dba, Class<T> objectType) {
        this.service = new CrudService<>(dba, objectType);
    }

    /**
     * 插入记录并返回数据库最新状态。
     *
     * @param object 待插入实体
     * @return 插入后的完整实体（含自增主键等数据库生成字段）
     */
    @PostMapping("/insert")
    public T insert(@RequestBody T object) {
        service.insert(object);
        return service.getByObject(object);
    }

    /**
     * 全量更新记录并返回数据库最新状态。
     *
     * @param object 包含主键的实体
     * @return 更新后的完整实体
     */
    @PostMapping("/update")
    public T update(@RequestBody T object) {
        service.update(object);
        return service.getByObject(object);
    }

    /**
     * 增量更新，仅更新 DTO 中指定的字段。
     *
     * @param dto 包含记录和变更属性列表
     * @return 受影响行数
     */
    @PostMapping("/delta-update")
    public int deltaUpdate(@RequestBody UpdateDTO<T> dto) {
        return service.deltaUpdate(dto);
    }

    /**
     * 按主键查询。
     *
     * @param keyValues 主键值数组
     * @return 实体对象
     */
    @PostMapping("/get-by-key")
    public T getByKey(@RequestBody Object[] keyValues) {
        return service.getByKey(keyValues);
    }

    /**
     * 分页查询。
     *
     * @param dto 查询条件及分页参数
     * @return 分页数据
     */
    @PostMapping("/query-page")
    public PageData<T> queryPage(@RequestBody QueryDTO dto) {
        return service.queryPage(dto);
    }

    /**
     * 列表查询。
     *
     * @param dto 查询条件
     * @return 结果列表
     */
    @PostMapping("/query-list")
    public List<T> queryList(@RequestBody QueryDTO dto) {
        return service.queryList(dto);
    }

    /**
     * 按主键删除单条记录。
     * <p>
     * 只读取主键字段，请求体中的非主键字段会被忽略，主键字段不能为空。
     *
     * @param object 包含主键的实体
     * @return 受影响行数
     */
    @PostMapping("/delete")
    public int delete(@RequestBody T object) {
        return service.delete(object);
    }

    /**
     * 批量删除。
     * <p>
     * 只读取主键字段，请求体中的非主键字段会被忽略，主键字段不能为空。
     *
     * @param objects 实体列表
     * @return 受影响行数合计
     */
    @PostMapping("/batch-delete")
    public int batchDelete(@RequestBody List<T> objects) {
        return service.delete(objects);
    }
}
