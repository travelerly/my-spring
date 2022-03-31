package com.colin.proxy;

/**
 * @author colin
 * @create 2022-03-31 09:50
 */
public class ProductServiceImpl implements ProductService{
    @Override
    public void addProduct(Product product) {
        // 执行方法核心逻辑
        System.out.println("开始执行添加商品的操作，product：" + product);
    }

    @Override
    public Product getProduct(Integer productId) {
        // 执行方法核心逻辑
        System.out.println("开始执行查询商品的操作，productId：" + productId);

        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("测试商品");
        product.setProductPrice(100);

        return product;
    }
}
