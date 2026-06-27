package io.github.jinghui70.rainbow.dbaccess.annotation;

/**
 * 字段自动生成的时机
 */
public enum GenerationTiming {

    /**
     * 仅在插入时生成。
     * <p>
     * 适用场景：主键 ID、创建时间、初始状态等。
     * 行为逻辑：通常仅当字段值为 {@code null} 时生成。
     */
    INSERT,

    /**
     * 在插入和更新时都生成。
     * <p>
     * 适用场景：更新时间、更新人、版本号等。
     * 行为逻辑：
     * <ul>
     *   <li>插入时：同 INSERT（通常判空）。</li>
     *   <li>更新时：通常强制覆盖，忽略当前值。</li>
     * </ul>
     */
    INSERT_UPDATE
}
