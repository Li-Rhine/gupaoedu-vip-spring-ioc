package com.gupaoedu.vip.spring.framework.beans;

/**
 * Created by Tom on 2019-12-08.
 */
public class GPBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrappedClass;

    public GPBeanWrapper(Object wrapperInstance) {
        this.wrappedClass = wrapperInstance.getClass();
        this.wrapperInstance = wrapperInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return wrappedClass;
    }
}
