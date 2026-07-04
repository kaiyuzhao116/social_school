package com.campusconnect.common.dynamic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DccValueBeanPostProcessor implements BeanPostProcessor {

    private final DynamicConfigService dynamicConfigService;
    private final DynamicConfigRegistry dynamicConfigRegistry;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        ReflectionUtils.doWithFields(targetClass, field -> {
            DccValue dccValue = field.getAnnotation(DccValue.class);

            if (dccValue == null) {
                return;
            }

            String expression = dccValue.value();
            String[] parts = expression.split(":", 2);

            String key = parts[0];
            String defaultValue = parts.length > 1 ? parts[1] : "";

            String redisValue = dynamicConfigService.getConfig(key);
            String finalValue = redisValue == null ? defaultValue : redisValue;

            dynamicConfigRegistry.register(key, bean, field, defaultValue);
            dynamicConfigRegistry.setFieldValue(bean, field, finalValue);

            log.info("【动态配置】初始化注入成功 bean={}, field={}, key={}, value={}",
                    beanName,
                    field.getName(),
                    key,
                    finalValue);
        });

        return bean;
    }
}