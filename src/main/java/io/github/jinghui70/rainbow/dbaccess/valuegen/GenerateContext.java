package io.github.jinghui70.rainbow.dbaccess.valuegen;

import io.github.jinghui70.rainbow.dbaccess.Dba;

import java.lang.reflect.Field;

/**
 * {@link ValueGenerator#generate} 的上下文，提供生成字段值所需的全部信息。
 *
 * @param dba   数据库访问对象。传入它是为了支持需要查库的生成策略——例如取数据库序列
 *              （{@code select seq.nextval}）、按当前最大值递增、或依据库中数据计算流水号。
 *              纯内存策略（如雪花 id、当前时间）可忽略。
 * @param data  当前正在插入的行对象，生成器可据其它字段的值来生成（如按类型拼前缀）
 * @param field 目标字段，可据其类型决定返回值类型
 * @param param {@link io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue#param()} 的值，
 *              含义由具体策略约定
 */
public record GenerateContext(Dba dba, Object data, Field field, String param) {
}
