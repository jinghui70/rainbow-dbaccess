package io.github.jinghui70.rainbow.dbaccess.memory;

public class Field {

    private String name;

    private DataType type;

    private int length;

    private int precision;

    private boolean key;

    private boolean autoIncrement;

    private boolean mandatory;

    private Object defaultValue;

    public static Field create(String name) {
        return new Field().setName(name);
    }

    public static Field createDouble(String name) {
        return new Field().setName(name).setType(DataType.DOUBLE).setDefaultValue(0);
    }

    public static Field createNumeric(String name, int precision) {
        return new Field().setName(name).setType(DataType.NUMERIC).setLength(32).setPrecision(precision).setDefaultValue(0);
    }

    public static Field createMoney(String name) {
        return new Field().setName(name).setType(DataType.NUMERIC).setLength(32).setPrecision(10).setDefaultValue(0);
    }

    public static Field createKeyDate(String name) {
        return new Field().setName(name).setType(DataType.DATE).setKey(true);
    }

    public static Field createDate(String name) {
        return new Field().setName(name).setType(DataType.DATE);
    }

    public static Field createKeyInt(String name) {
        return new Field().setName(name).setType(DataType.INT).setKey(true);
    }

    public static Field createInt(String name) {
        return new Field().setName(name).setType(DataType.INT).setDefaultValue(0);
    }

    public static Field createKeyString(String name) {
        return createKeyString(name, 32);
    }

    public static Field createKeyString(String name, int length) {
        return new Field().setName(name).setType(DataType.VARCHAR).setLength(length).setKey(true);
    }

    public static Field createString(String name) {
        return createString(name, 32);
    }

    public static Field createString(String name, int length) {
        return new Field().setName(name).setType(DataType.VARCHAR).setLength(length);
    }

    public String getName() {
        return name;
    }

    public Field setName(String name) {
        this.name = name;
        return this;
    }

    public DataType getType() {
        return type;
    }

    public Field setType(DataType type) {
        this.type = type;
        return this;
    }

    public int getLength() {
        return length;
    }

    public Field setLength(int length) {
        this.length = length;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    public Field setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public boolean isKey() {
        return key;
    }

    public Field setKey(boolean key) {
        this.key = key;
        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Field setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public Field setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Field setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

}
