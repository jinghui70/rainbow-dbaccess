# MemoryDba：用数据库的方式处理内存数据

## 设计哲学

MemoryDba 封装了 H2 内存数据库，可以**在开发中使用数据库的方式来处理数据**。有时候这种方式既方便又直观，远比用 Java 的数据结构处理起来轻松。另外，也可以使用内存数据库简化单元测试的开发。

## 典型场景：跨数据源合并计算

```java
List<PurchaseInfo> purchases = purchaseService.getProductPrices();
List<SaleInfo> sales = saleService.getProductSales();

try (MemoryDba mem = new MemoryDba()) {
    mem.createTable(
        Field.createKeyString("ID"),
        Field.createString("NAME"),
        Field.createNumeric("AVG_PURCHASE_PRICE", 2),
        Field.createNumeric("AVG_SELL_PRICE", 2),
        Field.createInt("SOLD_QTY"),
        Field.createNumeric("PROFIT", 2));

    mem.mergeOf(purchases).into(Table.DEFAULT_NAME).execute();
    mem.mergeOf(sales).into(Table.DEFAULT_NAME).execute();
    mem.update(Table.DEFAULT_NAME)
        .set("PROFIT=(AVG_SELL_PRICE-AVG_PURCHASE_PRICE)*SOLD_QTY").execute();

    List<ProductProfit> profits = mem.select().from(Table.DEFAULT_NAME)
        .queryForList(ProductProfit.class);
}
```

不指定表名时，默认表名为 `Table.DEFAULT_NAME`（"X"）。

## Field DSL

| 方法 | 说明 |
|------|------|
| `Field.createKeyString(name)` | VARCHAR(32) 主键 |
| `Field.createKeyString(name, length)` | VARCHAR(length) 主键 |
| `Field.createKeyInt(name)` | INT 主键 |
| `Field.createKeyDate(name)` | DATE 主键 |
| `Field.createString(name)` | VARCHAR(32) |
| `Field.createString(name, length)` | VARCHAR(length) |
| `Field.createInt(name)` | INT |
| `Field.createDouble(name)` | DOUBLE |
| `Field.createNumeric(name, scale)` | DECIMAL |
| `Field.createMoney(name)` | DECIMAL(32,10) |
| `Field.createDate(name)` | DATE |
| `Field.createTimestamp(name)` | TIMESTAMP |
| `Field.create(name).setType(DataType.BLOB)` | BLOB |
| `Field.create(name).setType(DataType.CLOB)` | CLOB |
