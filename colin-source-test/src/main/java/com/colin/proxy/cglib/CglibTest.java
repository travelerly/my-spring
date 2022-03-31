package com.colin.proxy.cglib;

import com.colin.proxy.Product;
import com.colin.proxy.ProductService;
import com.colin.proxy.ProductServiceImpl;

/**
 * @author colin
 * @create 2022-03-31 09:35
 */
public class CglibTest {

    public static void main(String[] args) {
        // 测试使用的数据
        Integer productId = 100;

        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("测试商品");
        product.setProductPrice(100);

        // 目标对象
        ProductService target = new ProductServiceImpl();

        // 测试 cglib 动态代理
        // 创建 cglib 动态代理类
        ProductServiceImpl cglibDynamicProxy = (ProductServiceImpl) new CglibDynamicProxy(target).getProxy();
        // 调用代理方法
        cglibDynamicProxy.addProduct(product);
        System.out.println("===== 分割线 =====");
        cglibDynamicProxy.getProduct(productId);
    }
}
