package com.luban.service;

import com.luban.framework.BeanPostProcessor;
import com.luban.framework.Component;

/**
 * @author yuan
 * @create 2020-10-15 21:25
 */
@Component
public class AutowiredAnnotationBeanPostProcessor implements BeanPostProcessor {
    @Override
    public void autowired() {
        System.out.println("处理Autowired注解");
    }
}
