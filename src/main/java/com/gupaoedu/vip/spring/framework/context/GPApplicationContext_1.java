package com.gupaoedu.vip.spring.framework.context;

/**
 * @Author: dahai.li
 * @Description: 入口类
 * @Date: Create in 15:21 2019/12/25
 */
public class GPApplicationContext_1 {

    public GPApplicationContext_1(String... configLocations) {

    }

    //获得IOC容器中所有beanName的名字
    public String[] getBeanDefinitionNames() {
        return null;
    }

    public int getBeanDefinitionCount() {
        return 0;
    }



    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    public Object getBean(String beanName) {
        return null;
    }


}
