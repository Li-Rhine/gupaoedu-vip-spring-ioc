package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import com.gupaoedu.vip.spring.framework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tom on 2019-12-09.
 */
public class GPHandlerAdapter {


    public GPModelAndView handle(HttpServletRequest request, HttpServletResponse response, GPHandlerMapping handler) throws Exception{

        //拼接形参列表
        //保存了Controller的方法上配置的参数名字以及和参数位置的对应关系
        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        //提取方法中加了注解的参数
        Annotation [] [] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof GPRequestParam){
                    String paramName = ((GPRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //提取方法中的request和response参数
        Class<?> [] paramsTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }

        //拼接实参列表

        Map<String,String[]> paramsMap = request.getParameterMap();

        Object [] parameValues = new Object[paramsTypes.length];

        for (Map.Entry<String, String[]> param : paramsMap.entrySet()) {
            String value = Arrays.toString(paramsMap.get(param.getKey()))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s",",");
            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());
            parameValues[index] = caseStringValue(value,paramsTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            parameValues[index] = request;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            parameValues[index] = response;
        }

        Object result = handler.getMethod().invoke(handler.getController(),parameValues);
        if(result == null || result instanceof Void){return null;}

        boolean isModelAndView = handler.getMethod().getReturnType() == GPModelAndView.class;
        if(isModelAndView){
            return (GPModelAndView)result;
        }
        return null;
    }

    private Object caseStringValue(String value,Class<?> paramType){
        if(String.class == paramType){
            return value;
        }
        if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        } else{
            if(value != null){
                return value;
            }
            return null;
        }
    }
}
