package com.luban.framework;

import com.luban.service.UserService;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuan
 * @create 2020-10-13 19:32
 */
public class LubanApplicationContext {

    // 容器的配置类
    private Class configClass;

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    // 单例池
    private Map<String, Object> singletonObjectMap = new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public Class getConfigClass() {
        return configClass;
    }

    public void setConfigClass(Class configClass) {
        this.configClass = configClass;
    }

    public LubanApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 扫描
        scan(configClass);

        // 创建非懒加载的单例bean
        createNonLazySingleton();


    }

    private void createNonLazySingleton() {
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 非懒加载
            if(!beanDefinition.isLazy()){
                if("singleton".equals(beanDefinition.getScope())) {
                    // 创建bean
                    Object bean = createBean(beanDefinition, beanName);
                    singletonObjectMap.put(beanName, bean);
                }
            }
        }
    }

    /**
     * 创建一个bean
     * @param beanDefinition
     * @return
     */
    private Object createBean(BeanDefinition beanDefinition, String beanName) {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            // 利用无参的构造方法创建对象
            Object bean = beanClass.getDeclaredConstructor().newInstance();

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.autowired();
            }

            // 填充属性
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(bean, getBean(field.getName()));
                }
            }

            // 在创建bean的时候，如果该bean实现了BeanNameAware接口，则调用此接口的设置beanName方法
            if(bean instanceof BeanNameAware) {
                ((BeanNameAware)bean).setBeanName(beanName);
            }

            // 属性设置完 检查一下属性
            if(bean instanceof InitializingBean) {
                ((InitializingBean)bean).afterPropertiesSet();
            }

            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void scan(Class configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            System.out.println(path);

            ClassLoader classLoader = LubanApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path.replace(".","/"));
            File file = new File(resource.getFile());
            for (File f : file.listFiles()) {
                // f是class文件,只取中间的路径
                // f文件路径  E:\05-Learn\spring-study-luban\target\classes\com\luban\service\UserService.class
                // 需要 com.luban.service.UserService
                String s = f.getAbsolutePath();
                if(s.endsWith(".class")){
                    s = s.substring(s.indexOf("com"), s.indexOf(".class"));
                    s = s.replace("\\",".");
                    try {
                        Class clazz = classLoader.loadClass(s);

                        if(clazz.isAnnotationPresent(Component.class)){
                            // 有Component注解，表示这是一个bean

                            // 获取bean后置处理器 如果当前类是实现了BeanPostProcessor接口
                            if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                // 创建对象
                                BeanPostProcessor o = null;
                                try {
                                    o = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                                beanPostProcessorList.add(o);

                            }

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setBeanClass(clazz);

                            Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            // 判断是否是懒加载
                            if(clazz.isAnnotationPresent(Lazy.class)){
                                // 有lazy注解,懒加载
                                beanDefinition.setLazy(true);
                            }

                            // 判断是否是单例
                            if(clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                                String scope = scopeAnnotation.value();
                                beanDefinition.setScope(scope);
                            } else {
                                // 没有Scope注解，默认是单例
                                beanDefinition.setScope("singleton");
                            }

                            // 将解析的beanDefinition存到map中去
                            beanDefinitionMap.put(beanName,beanDefinition);

                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }


            }

        }
    }

    public Object getBean(String beanName) {
        if(!beanDefinitionMap.containsKey(beanName)) {
            throw new RuntimeException("没有此名称的bean");
        } else {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if("singleton".equals(beanDefinition.getScope())) {
                // 单例，从单例池中获取对象
                Object o = singletonObjectMap.get(beanName);
                if(null == o){
                    o = createBean(beanDefinition, beanName);
                    singletonObjectMap.put(beanName, o);
                }
                return o;
            } else if("prototype".equals(beanDefinition.getScope())) {
                // 原型，创建一个bean
                return createBean(beanDefinition, beanName);
            }
        }
        return null;
    }
}
