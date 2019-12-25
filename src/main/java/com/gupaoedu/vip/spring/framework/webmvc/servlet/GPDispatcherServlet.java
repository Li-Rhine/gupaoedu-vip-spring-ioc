package com.gupaoedu.vip.spring.framework.webmvc.servlet;


import com.gupaoedu.vip.spring.framework.annotation.*;
import com.gupaoedu.vip.spring.framework.context.GPApplicationContext;

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
 * Created by Tom.
 */
public class GPDispatcherServlet extends HttpServlet {

    GPApplicationContext applicationContext = null;

    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //6、根据url调用method
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detail: " + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        Map<String,String[]> paramsMap = req.getParameterMap();

        //实参列表
        //实参列表要根据形参列表才能决定，首先得拿到形参列表
        Class<?> [] paramterTypes = method.getParameterTypes();

        Object [] parameValues = new Object[paramterTypes.length];
        for (int i = 0; i <paramterTypes.length; i ++){
            Class paramterType = paramterTypes[i];
            if(paramterType == HttpServletRequest.class){
                parameValues[i] = req;
                continue;
            }else if(paramterType == HttpServletResponse.class){
                parameValues[i] = resp;
                continue;
            }else if(paramterType == String.class){
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j ++){
                    for (Annotation a : pa[i]) {
                        if(a instanceof GPRequestParam){
                            String paramName = ((GPRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(paramsMap.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s",",");
                                parameValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        method.invoke(applicationContext.getBean(method.getDeclaringClass()),parameValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));

        //======= MVC =============
        //5、初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }

    //初始化url和Method的一对一对应关系
    private void doInitHandlerMapping() {
        if(applicationContext.getBeanDefinitionCount() == 0){return;}

        String [] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();
           if(!clazz.isAnnotationPresent(GPController.class)){ continue; }

            //保存写在类上面的@GPRequestMapping("/demo")
           String baseUrl = "";
           if(clazz.isAnnotationPresent(GPRequestMapping.class)){
               GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
               baseUrl = requestMapping.value();
           }

            //默认获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}

                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);

                //demoquery
                //  //demo//query
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);

                System.out.println("Mapped " + url + "," + method);

            }

        }
        
    }

}
