package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import com.gupaoedu.vip.spring.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author: dahai.li
 * @Description:
 * @Date: Create in 10:05 2019/12/24
 */
public class GPDispatcherServlet_1_Bak extends HttpServlet {

    private Properties contextConfig = new Properties();

    private List<String> className = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //6、根据url调用method
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception, Detail:" + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = this.handlerMapping.get(url);
        Map<String, String[]> paramsMap = req.getParameterMap();

        //实参列表
        //实参列表要根据形参列表才能决定，首先得拿到形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameValues = new Object[parameterTypes.length];

        for (int i = 0; i< parameterTypes.length; i++) {
            Class paramterType =  parameterTypes[i];
            if (paramterType == HttpServletRequest.class) {
                parameValues[i] = req;
                continue;
            }else if (paramterType == HttpServletResponse.class) {
                parameValues[i] = resp;
                continue;
            }else if (paramterType == String.class) {
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j =0; j<pa.length;j++) {
                    for (Annotation a : pa[i]) {
                        if (a instanceof GPRequestParam) {
                            String paramName = ((GPRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(paramsMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                parameValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName), parameValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //工厂类 GPApplicationContext   IOC、DI


        //========== IOC ============
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化扫描到的类，并且放入到IOC容器中
        doInstance();

        //======== DI ===========
        //4、完成自动化的依赖注入
        doAutowired();


        //========= MVC ==============
        //5、初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("GP Spring framework is init.");
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
                className.add(scanPackage + "." + file.getName().replace(".class", ""));
            }

        }
    }


    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(GPController.class)) {
                continue;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);

                System.out.println("Mapper" + url + "," + method);
            }

        }

    }

    private void doAutowired() {

        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //拿到实例的所有字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(GPAutowired.class)) {
                    continue;
                }
                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                //强制暴力访问
                //强吻
                field.setAccessible(true);

                //反射调用的方式
                //给entry.getValue() 这个对象的field字段，
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            
        }
    }

    private void doInstance() {
        if (className.isEmpty()) {
            return;
        }

        try {
            for (String className : className) {
                Class<?> clazz = Class.forName(className);


                if (clazz.isAnnotationPresent(GPController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    //key-value
                    //class类名的首字母小写
                    ioc.put(beanName, instance);
                }else if (clazz.isAnnotationPresent(GPService.class)){
                    //1、默认就根据beanName类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    //2、使用自定义的beanName
                    GPService service = clazz.getAnnotation(GPService.class);
                    if (!"".equals(service.value())) {
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    //3、根据 包名.类名 作为beanName
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The beanName is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }else {
                    continue;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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
}
