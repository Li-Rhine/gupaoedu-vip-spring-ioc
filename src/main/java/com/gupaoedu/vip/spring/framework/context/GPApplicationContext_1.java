package com.gupaoedu.vip.spring.framework.context;

import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition;
import com.gupaoedu.vip.spring.framework.beans.support.GPBeanDefinitionReader_1;

import java.util.List;

/**
 * @Author: dahai.li
 * @Description: 入口类
 * @Date: Create in 15:21 2019/12/25
 */
public class GPApplicationContext_1 {

    private String[] configLocations;

    private GPBeanDefinitionReader_1 reader;

    public GPApplicationContext_1(String... configLocations) {

        //拿路径
        this.configLocations = configLocations;

        //1、读取配置文件
        reader = new GPBeanDefinitionReader_1(this.configLocations);

        //2、解析配置文件，封装成BeanDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、把BeanDefinition对应的实例放入到IOC容器
        doRegisterBeanDefinition(beanDefinitions);

        //4、完成依赖出入
        doAutowrited();

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
