package com.gupaoedu.vip.spring.framework.context;

import com.gupaoedu.vip.spring.framework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.framework.annotation.GPController;
import com.gupaoedu.vip.spring.framework.annotation.GPService;
import com.gupaoedu.vip.spring.framework.beans.GPBeanWrapper;
import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition_1;
import com.gupaoedu.vip.spring.framework.beans.support.GPBeanDefinitionReader_1;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: dahai.li
 * @Description: 入口类
 * @Date: Create in 15:21 2019/12/25
 */
public class GPApplicationContext_1 {

    private final Map<String, GPBeanDefinition_1> beanDefinitionMap = new HashMap<String, GPBeanDefinition_1>();

    private String[] configLocations;

    private GPBeanDefinitionReader_1 reader;

    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String, GPBeanWrapper>();

    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();



    public GPApplicationContext_1(String... configLocations) {

        //拿路径
        this.configLocations = configLocations;

        try {
            //1、读取配置文件
            reader = new GPBeanDefinitionReader_1(this.configLocations);

            //2、解析配置文件，封装成BeanDefinition
            List<GPBeanDefinition_1> beanDefinitions = reader.loadBeanDefinitions();

            //3、把BeanDefinition对应的实例放入到IOC容器
            doRegisterBeanDefinition(beanDefinitions);

            //初始化阶段完成

            //4、完成依赖出入
            doAutowrited();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doAutowrited() {
        //依赖注入的第一步
        //是不是先得根据配置把对象得实例搞出来，才能可能依赖注入
        for (Map.Entry<String,GPBeanDefinition_1> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }

    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition_1> beanDefinitions) throws Exception {
        for (GPBeanDefinition_1 beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!");
            }
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            this.beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    //获得IOC容器中所有beanName的名字
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }



    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    public Object getBean(String beanName) {
        //1、读取GPBeanDefinition的配置信息
        GPBeanDefinition_1 gpBeanDefinition = this.beanDefinitionMap.get(beanName);

        //2、用反射实例化
        Object instance = instantiateBean(beanName, gpBeanDefinition);

        //3、把创建出来的实例包装为BeanWrapper对象
        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);

        //循环依赖
        // class A{B b;}
        // class B{A a;}
        //4、把BeanWrapper对象放入到真正的IOC容器里面
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);


        //5、执行依赖注入
        populateBean(beanName, new GPBeanDefinition_1(), beanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    private void populateBean(String beanName, GPBeanDefinition_1 gpBeanDefinition, GPBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        //只有加了注解的才进行依赖注入
        if (!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))) {
            return;
        }


        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }
            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            //强制暴力访问
            //强吻
            field.setAccessible(true);

            //反射调用的方式
            //给entry.getValue() 这个对象的field字段，
            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

    }

    private Object instantiateBean(String beanName, GPBeanDefinition_1 gpBeanDefinition) {
        String className = gpBeanDefinition.getBeanClassName();
        Object instance = null;

        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
            this.factoryBeanObjectCache.put(beanName, instance);
            this.factoryBeanObjectCache.put(gpBeanDefinition.getFactoryBeanName(), instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }


}
