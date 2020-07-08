package com.xxl.job.core.handler.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.Constants;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuxueli 2019-12-11 21:12:18
 */
public class MethodJobHandler extends IJobHandler {

    private final Object target;
    private final Method method;
    private Method initMethod;
    private Method destroyMethod;
    private Class<?>[] classParamValue;

    public MethodJobHandler(Object target, Method method, Method initMethod, Method destroyMethod,Class<?>[] classParamValue) {
        this.target = target;
        this.method = method;

        this.initMethod =initMethod;
        this.destroyMethod =destroyMethod;
        this.classParamValue = classParamValue;
    }

    /**
     * TODO 只处理了对象,没有处理基本数据类型
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        List<Object> listObject = doParams(param);
        return (ReturnT<String>) method.invoke(target, listObject.toArray());
    }

    /**
     * 处理参数的映射
     * @param param
     * @return
     */
    private List<Object> doParams(String param) {
        List<Object> listObject = new ArrayList<>();
        if (!StringUtils.isEmpty(param)) {
            if (param.contains(Constants.SYMBOL_SEMICOLON)) {
                String[] params = StringUtils.split(param, Constants.SYMBOL_SEMICOLON);
                for (int i = 0; i < params.length; i++) {
                    Class<?> paramClass = classParamValue[i];
                    String paramValue = params[i];
                    if (Constants.SYMBOL_LEFT_MIDDLE_BRACKETS.equals(params[i])) {
                        List<?> object = null;
                        if (!Constants.SYMBOL_MIDDLE_BRACKETS.equals(paramValue.trim())) {
                            object = JSONObject.parseArray(paramValue, paramClass);
                        }
                        listObject.add(object);
                    } else {
                        if (!Constants.SYMBOL_CURLY_BRACKETS.equals(paramValue.trim())) {
                            Object object = JSONObject.parseObject(paramValue, paramClass);
                            listObject.add(object);
                        }
                    }
                }
            } else {
                Object object = JSONObject.parseObject(param, classParamValue[0]);
                listObject.add(object);
            }
        }
        return listObject;
    }

    @Override
    public void init() throws InvocationTargetException, IllegalAccessException {
        if(initMethod != null) {
            initMethod.invoke(target);
        }
    }

    @Override
    public void destroy() throws InvocationTargetException, IllegalAccessException {
        if(destroyMethod != null) {
            destroyMethod.invoke(target);
        }
    }

    @Override
    public String toString() {
        return super.toString()+"["+ target.getClass() + "#" + method.getName() +"]";
    }


}
