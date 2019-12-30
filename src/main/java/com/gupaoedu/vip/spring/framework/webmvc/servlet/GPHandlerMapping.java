package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Author: dahai.li
 * @Description:
 * @Date: Create in 15:15 2019/12/30
 */
public class GPHandlerMapping {

    protected Object controller;	//保存方法对应的实例
    protected Method method;		//保存映射的方法
    protected Pattern pattern;      //${} url占位符解析

    public GPHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
