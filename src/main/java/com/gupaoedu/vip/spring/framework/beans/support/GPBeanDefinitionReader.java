package com.gupaoedu.vip.spring.framework.beans.support;

import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Tom on 2019-12-08.
 */
public class GPBeanDefinitionReader {

    private List<String> registryBeanClasses = new ArrayList<String>();
    private Properties contextConfig = new Properties();

    public GPBeanDefinitionReader(String[] configLoactions) {
        //直接从类路径下找到Spring主配置文件所在的路径
        //并且将其读取出来放到Properties对象中
        //相对于scanPackage=com.gupaoedu.demo 从文件中保存到了内存中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLoactions[0].replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    //扫描出相关的类
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        //scanPackage = com.gupaoedu.demo ，存储的是包路径
        //转换为文件路径，实际上就是把.替换为/就OK了
        //classpath下不仅有.class文件， .xml文件  .properties文件
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {

            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                //变成包名.类名
                //Class.forname()
                if (!file.getName().endsWith(".class")) {  continue; }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                //classNames.add();
                registryBeanClasses.add(className);
            }
        }
    }

    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();

        try {
            for (String className : registryBeanClasses) {
                Class<?> beanClass = Class.forName(className);

                if(beanClass.isInterface()){continue;}

                //1、默认类名首字母小写
                //2、自定义名字
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                //3、接口注入
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
        return result;
    }

    private GPBeanDefinition doCreateBeanDefinition(String fatoryBeanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(fatoryBeanName);
        return beanDefinition;
    }


    //如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也不可能出现非字母的情况

    //为了简化程序逻辑，就不做其他判断了，大家了解就OK
    //其实用写注释的时间都能够把逻辑写完了
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
