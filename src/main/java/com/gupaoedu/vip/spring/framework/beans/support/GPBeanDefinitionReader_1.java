package com.gupaoedu.vip.spring.framework.beans.support;


import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition_1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Description：
 * @Author： Rhine
 * @Date： 2019/12/26 22:50
 **/
public class GPBeanDefinitionReader_1 {

    private List<String> registryBeanClasses = new ArrayList<String>();

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

        doScanner(contextConfig.getProperty("scanPackage"));
    }


    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        //不仅有.class文件， xml文件 .properties文件
        File classPath = new File(url.getFile());

        for (File file: classPath.listFiles()) {

            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            }else {
                //变成 包名.类名
                //Class.forname()
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                //className.add();
                registryBeanClasses.add(className);
            }

        }
    }

    public List<GPBeanDefinition_1> loadBeanDefinitions() {
        List<GPBeanDefinition_1> result = new ArrayList<GPBeanDefinition_1>();
        try {
            for (String className : registryBeanClasses) {
                Class<?> beanClass = Class.forName(className);

                if (beanClass.isInterface()) {
                    continue;
                }

                //1、默认类名首字母小写
                //2、自定义名字
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                //3、接口注入
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private GPBeanDefinition_1 doCreateBeanDefinition(String fatoryName, String beanClassName) {
        GPBeanDefinition_1 beanDefinition = new GPBeanDefinition_1();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(fatoryName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig(){
        return this.contextConfig;
    }
}
