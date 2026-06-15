package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

/**
 * 使用默认表名的实体（无 @Table 注解），测试驼峰转下划线大写。
 */
public class ProductEntity {

    @Id
    private String id;

    private String productName;

    private Double price;

    public ProductEntity() {
    }

    public ProductEntity(String id, String productName, Double price) {
        this.id = id;
        this.productName = productName;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
