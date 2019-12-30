package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import com.gupaoedu.vip.spring.framework.annotation.GPController;
import com.gupaoedu.vip.spring.framework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.framework.annotation.GPRequestParam;
import com.gupaoedu.vip.spring.framework.context.GPApplicationContext_1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: dahai.li
 * @Description:
 * @Date: Create in 10:05 2019/12/24
 */
public class GPDispatcherServlet_1 extends HttpServlet {


    GPApplicationContext_1 context = null;


    //保存url和Method的对应关系
    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();

    private Map<GPHandlerMapping, GPHandlerAdapter> handlerAdapters = new HashMap<GPHandlerMapping, GPHandlerAdapter>();

    private List<GPViewResolver> viewResolvers = new ArrayList<GPViewResolver>();


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
        //1、根据url拿到对应的Handler
        GPHandlerMapping handler = getHandler(req);

        if (null == handler) {
            processDispatchResult(req,resp,new GPModelAndView("404"));
            return;
        }

        //2、根据HandlerMapping拿到HandlerAdapter
        GPHandlerAdapter ha = getHandlerAdapter(handler);

        //3、根据GPHandlerAdapter拿到 ModelAndView
        GPModelAndView mv = ha.handle(req, resp, handler);

        //4、ViewResolver 拿到View
        //将View渲染成浏览器能够接收的结果 HTML字符串
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView mv) {
        if (null == mv) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (GPViewResolver viewResolver : this.viewResolvers) {
            GPView view = viewResolver.resolverViewName(mv.getViewName());
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        GPHandlerAdapter ha = this.handlerAdapters.get(handler);
        return ha;
     }

    private GPHandlerMapping getHandler(HttpServletRequest req) {

        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (GPHandlerMapping handlerMapping : this.handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            //url是用正则去匹配Controller中的配置信息
            if (!matcher.matches()) {
                continue;
            }
            return handlerMapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        context = new GPApplicationContext_1(config.getInitParameter("contextConfigLocation"));

        

        //初始化Spring MVC的九大组件，今天只实现
        //HandlerMapping
        //HandlerAdapter
        //ViewResolver
        initStrategies(context);

        System.out.println("GP Spring framework is init.");
    }

    private void initStrategies(GPApplicationContext_1 context) {

        //Url和Method的对应关系
        initHandlerMappings(context);
        //参数适配器
        initHandlerAdaters(context);
        //视图转换器
        initViewResolvers(context);
    }

    private void initViewResolvers(GPApplicationContext_1 context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            //高仿真
            this.viewResolvers.add(new GPViewResolver(templateRoot));
        }
    }

    private void initHandlerAdaters(GPApplicationContext_1 context) {
        for (GPHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new GPHandlerAdapter());
        }
        
    }

    private void initHandlerMappings(GPApplicationContext_1 context) {
        String [] beanNames = context.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object instance = context.getBean(beanName);
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
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");

                Pattern pattern = Pattern.compile(regex);
                this.handlerMappings.add(new GPHandlerMapping(pattern,instance,method));

                System.out.println("Mapped " + regex + "," + method);
            }

        }

    }

}
