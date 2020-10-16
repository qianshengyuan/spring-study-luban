package com.luban;

import com.luban.framework.LubanApplicationContext;
import com.luban.service.UserService;

/**
 * @author yuan
 * @create 2020-10-13 19:32
 */
public class Test {

    public static void main(String[] args) {
        // 启动Spring，创建bean，提供给用户使用
        // Spring启动过程：
            // 1.扫描（不是项目中所有bean都要创建对象）
            // 2.创建bean（非懒加载的单例bean）
        LubanApplicationContext applicationContext = new LubanApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        Object userService1 = applicationContext.getBean("userService");
        Object userService2 = applicationContext.getBean("userService");
        System.out.println(userService1);
        System.out.println(userService2);
        userService.test();
    }
}
