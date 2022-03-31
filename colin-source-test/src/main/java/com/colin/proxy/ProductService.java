package com.colin.proxy;

/**
 * @author colin
 * @create 2022-03-31 09:50
 */
public interface ProductService {

    // 添加商品
    void addProduct(Product product);

    // 根据 id 查询商品
    Product getProduct(Integer productId);
}
