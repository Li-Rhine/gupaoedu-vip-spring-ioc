package com.gupaoedu.vip.spring.framework.beans.support;

import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2019/12/26 22:50
 **/
public class GPBeanDefinitionReader_1 {

    private Properties contextConfig = new Properties();

    public GPBeanDefinitionReader_1(String[] configLocations) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocations[0].replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<GPBeanDefinition> loadBeanDefinitions() {
        return null;
    }
}
