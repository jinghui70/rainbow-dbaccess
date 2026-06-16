package io.github.jinghui70.rainbow.dbaccess.memory;

/**
 * 数据库字段定义
 * <p>
 * 用于描述内存数据库表的字段属性，包括字段名、类型、长度、精度、是否主键等
 * </p>
 */
public class Field {

    private String name;

    private DataType type;

    private int length;

    private int precision;

    private boolean key;

    private boolean autoIncrement;

    private boolean mandatory;

    private Object defaultValue;

    /**
     * 创建一个字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field create(String name) {
        return new Field().setName(name);
    }

    /**
     * 创建一个DOUBLE类型的字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createDouble(String name) {
        return new Field().setName(name).setType(DataType.DOUBLE).setDefaultValue(0);
    }

    /**
     * 创建一个NUMERIC类型的字段
     *
     * @param name      字段名
     * @param precision 小数精度
     * @return 字段对象
     */
    public static Field createNumeric(String name, int precision) {
        return new Field().setName(name).setType(DataType.NUMERIC).setLength(32).setPrecision(precision).setDefaultValue(0);
    }

    /**
     * 创建一个金额类型的字段（NUMERIC，精度10）
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createMoney(String name) {
        return new Field().setName(name).setType(DataType.NUMERIC).setLength(32).setPrecision(10).setDefaultValue(0);
    }

    /**
     * 创建一个DATE类型的主键字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createKeyDate(String name) {
        return new Field().setName(name).setType(DataType.DATE).setKey(true);
    }

    /**
     * 创建一个DATE类型的字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createDate(String name) {
        return new Field().setName(name).setType(DataType.DATE);
    }

    /**
     * 创建一个TIMESTAMP类型的字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createTimestamp(String name) {
        return new Field().setName(name).setType(DataType.TIMESTAMP);
    }

    /**
     * 创建一个INT类型的主键字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createKeyInt(String name) {
        return new Field().setName(name).setType(DataType.INT).setKey(true);
    }

    /**
     * 创建一个INT类型的字段
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createInt(String name) {
        return new Field().setName(name).setType(DataType.INT).setDefaultValue(0);
    }

    /**
     * 创建一个VARCHAR类型的主键字段（默认长度32）
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createKeyString(String name) {
        return createKeyString(name, 32);
    }

    /**
     * 创建一个VARCHAR类型的主键字段
     *
     * @param name   字段名
     * @param length 字段长度
     * @return 字段对象
     */
    public static Field createKeyString(String name, int length) {
        return new Field().setName(name).setType(DataType.VARCHAR).setLength(length).setKey(true);
    }

    /**
     * 创建一个VARCHAR类型的字段（默认长度32）
     *
     * @param name 字段名
     * @return 字段对象
     */
    public static Field createString(String name) {
        return createString(name, 32);
    }

    /**
     * 创建一个VARCHAR类型的字段
     *
     * @param name   字段名
     * @param length 字段长度
     * @return 字段对象
     */
    public static Field createString(String name, int length) {
        return new Field().setName(name).setType(DataType.VARCHAR).setLength(length);
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置字段名
     *
     * @param name 字段名
     * @return 当前字段对象（链式调用）
     */
    public Field setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 获取字段数据类型
     *
     * @return 数据类型
     */
    public DataType getType() {
        return type;
    }

    /**
     * 设置字段数据类型
     *
     * @param type 数据类型
     * @return 当前字段对象（链式调用）
     */
    public Field setType(DataType type) {
        this.type = type;
        return this;
    }

    /**
     * 获取字段长度
     *
     * @return 字段长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 设置字段长度
     *
     * @param length 字段长度
     * @return 当前字段对象（链式调用）
     */
    public Field setLength(int length) {
        this.length = length;
        return this;
    }

    /**
     * 获取字段精度
     *
     * @return 字段精度
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 设置字段精度
     *
     * @param precision 字段精度
     * @return 当前字段对象（链式调用）
     */
    public Field setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    /**
     * 判断是否为主键字段
     *
     * @return true表示是主键字段
     */
    public boolean isKey() {
        return key;
    }

    /**
     * 设置是否为主键字段
     *
     * @param key true表示是主键字段
     * @return 当前字段对象（链式调用）
     */
    public Field setKey(boolean key) {
        this.key = key;
        return this;
    }

    /**
     * 判断是否为自增字段
     *
     * @return true表示是自增字段
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    /**
     * 设置是否为自增字段
     *
     * @param autoIncrement true表示是自增字段
     * @return 当前字段对象（链式调用）
     */
    public Field setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    /**
     * 判断是否为必填字段
     *
     * @return true表示是必填字段
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * 设置是否为必填字段
     *
     * @param mandatory true表示是必填字段
     * @return 当前字段对象（链式调用）
     */
    public Field setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    /**
     * 获取字段默认值
     *
     * @return 默认值
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * 设置字段默认值
     *
     * @param defaultValue 默认值
     * @return 当前字段对象（链式调用）
     */
    public Field setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

}
