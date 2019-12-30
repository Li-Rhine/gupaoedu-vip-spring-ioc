package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @Author: dahai.li
 * @Description:
 * @Date: Create in 15:22 2019/12/30
 */
public class GPModelAndView {
    private String viewName;
    private Map<String, ?> model;

    public GPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public GPModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
