package com.luban.service;

import com.luban.framework.*;

/**
 * @author yuan
 * @create 2020-10-13 19:30
 */
@Component("userService")
@Scope("singleton")
public class UserService implements BeanNameAware , InitializingBean {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println(orderService);
        System.out.println(beanName);
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println("setBeanName调用了");
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        //
        System.out.println("afterPropertiesSet调用了");

    }
}
