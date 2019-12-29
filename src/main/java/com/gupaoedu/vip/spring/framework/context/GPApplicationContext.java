//package com.gupaoedu.vip.spring.framework.context;
//
//import com.gupaoedu.vip.spring.framework.annotation.GPAutowired;
//import com.gupaoedu.vip.spring.framework.annotation.GPController;
//import com.gupaoedu.vip.spring.framework.annotation.GPService;
//import com.gupaoedu.vip.spring.framework.beans.GPBeanWrapper;
//import com.gupaoedu.vip.spring.framework.beans.config.GPBeanDefinition;
//import com.gupaoedu.vip.spring.framework.beans.support.GPBeanDefinitionReader;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by Tom.
// */
//public class GPApplicationContext {
//
//    private final Map<String,GPBeanDefinition> beanDefinitionMap = new HashMap<String,GPBeanDefinition>();
//
//    private String [] configLoactions;
//    private GPBeanDefinitionReader reader;
//
//    private Map<String,GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String,GPBeanWrapper>();
//    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();
//
//    public GPApplicationContext(String... configLocations){
//        //拿路径
//        this.configLoactions = configLocations;
//
//        try {
//            //1、读取配置文件
//            reader = new GPBeanDefinitionReader(this.configLoactions);
//
//            //2、解析配置文件，封装成BeanDefinition
//            List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
//
//            //3、把BeanDefinition对应的实例放入到IOC容器
//            doRegisterBeanDefinition(beanDefinitions);
//
//            //初始化阶段完成
//
//            //4、完成依赖注入
//            doAutowrited();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    private void doAutowrited() {
//        //依赖注入的第一步
//        //是不是先得根据配置把对象的实例搞出来，才有可能依赖注入
//        for (Map.Entry<String,GPBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
//            String beanName = beanDefinitionEntry.getKey();
//            getBean(beanName);
//        }
//
//    }
//
//    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception{
//        for (GPBeanDefinition beanDefinition : beanDefinitions) {
//            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
//                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists");
//            }
//            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
//            this.beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
//        }
//    }
//
//    //获得IOC容器中所有的beanName的名字
//    public String[] getBeanDefinitionNames(){
//        return beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
//    }
//
//    public int getBeanDefinitionCount(){
//        return beanDefinitionMap.size();
//    }
//
//    public Object getBean(Class beanClass){
//        return getBean(beanClass.getName());
//    }
//
//    public Object getBean(String beanName){
//        //1、读取GPBeanDefinition的配置信息
//        GPBeanDefinition gpBeanDefinition = this.beanDefinitionMap.get(beanName);
//
//        //2、用反射实例化
//        Object instance = instantiateBean(beanName,gpBeanDefinition);
//
//        //3、把创建出来的真实的实例包装为BeanWrapper对象
//        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
//
//        //循环依赖
//        //class A{B b;}
//        //class B{A a;}
//        //4、把BeanWrapper对象放入到真正的IOC容器里面
//        this.factoryBeanInstanceCache.put(beanName,beanWrapper);
//
//        //5、执行依赖注入
//        populateBean(beanName,new GPBeanDefinition(),beanWrapper);
//        return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
//    }
//
//    private void populateBean(String beanName, GPBeanDefinition gpBeanDefinition, GPBeanWrapper beanWrapper) {
//        Object instance = beanWrapper.getWrapperInstance();
//
//        Class<?> clazz = beanWrapper.getWrappedClass();
//
//        //只有加了注解的才进行依赖注入
//        if(!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))){
//            return;
//        }
//
//        //拿到实例的所有的字段
//        //Declared 所有的，特定的 字段，包括private/protected/default
//        //正常来说，普通的OOP编程只能拿到public的属性
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            if (!field.isAnnotationPresent(GPAutowired.class)) {
//                continue;
//            }
//            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
//            //如果用户没有自定义beanName，默认就根据类型注入
//            //这个地方省去了对类名首字母小写的情况的判断，这个作为课后作业
//            //小伙伴们自己去完善
//            String autowiredBeanName = autowired.value().trim();
//            if ("".equals(autowiredBeanName)) {
//                //获得接口的类型，作为key待会拿这个key到ioc容器中去取值
//                autowiredBeanName = field.getType().getName();
//            }
//
//            //如果是public以外的修饰符，只要加了@Autowired注解，都要强制赋值
//            //反射中叫做暴力访问， 强吻
//            field.setAccessible(true);
//
//            //反射调用的方式
//            //给entry.getValue()这个对象的field字段，赋ioc.get(beanName)这个值
//            try {
//                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
//                    continue;
//                }
//                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//                continue;
//            }
//        }
//
//    }
//
//    private Object instantiateBean(String beanName, GPBeanDefinition gpBeanDefinition) {
//        String className = gpBeanDefinition.getBeanClassName();
//
//        Object instance = null;
//        try {
//            Class<?> clazz = Class.forName(className);
//            instance = clazz.newInstance();
//            this.factoryBeanObjectCache.put(beanName,instance);
////            this.factoryBeanObjectCache.put(gpBeanDefinition.getFactoryBeanName(),instance);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return instance;
//    }
//
//}
