package org.dromara.milvus.plus.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringUtils 提供从Spring应用上下文中获取Bean的静态方法。
 *  @author xgc
 */

@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取应用上下文中的Bean实例。
     * @param <T> Bean类型
     * @param clazz Bean的Class
     * @return Bean实例，如果没有找到返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    // 如果项目中有多个相同类型的Bean，你可能需要一个更具体的获取方法，例如：
    /**
     * 获取应用上下文中指定名称的Bean实例。
     * @param <T> Bean类型
     * @param clazz Bean的Class
     * @param beanName Bean的名称
     * @return Bean实例，如果没有找到返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz, String beanName) {
        return (T) applicationContext.getBean(beanName, clazz);
    }
}