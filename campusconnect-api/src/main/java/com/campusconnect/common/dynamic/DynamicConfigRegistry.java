package com.campusconnect.common.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class DynamicConfigRegistry {

    private final Map<String, List<FieldHolder>> registry = new ConcurrentHashMap<>();

    public void register(String key, Object bean, Field field, String defaultValue) {
        field.setAccessible(true);

        registry.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                .add(new FieldHolder(bean, field, defaultValue));

        log.info("【动态配置】注册字段 key={}, bean={}, field={}",
                key,
                bean.getClass().getSimpleName(),
                field.getName());
    }

    public void updateValue(String key, String value) {
        List<FieldHolder> holders = registry.get(key);

        if (holders == null || holders.isEmpty()) {
            log.warn("【动态配置】没有找到绑定字段，key={}", key);
            return;
        }

        for (FieldHolder holder : holders) {
            setFieldValue(holder.bean(), holder.field(), value);
        }

        log.info("【动态配置】配置刷新成功 key={}, value={}", key, value);
    }

    public void setFieldValue(Object bean, Field field, String value) {
        try {
            Class<?> type = field.getType();

            Object realValue;

            if (type == Integer.class || type == int.class) {
                realValue = Integer.parseInt(value);
            } else if (type == Long.class || type == long.class) {
                realValue = Long.parseLong(value);
            } else if (type == Boolean.class || type == boolean.class) {
                realValue = "1".equals(value) || "true".equalsIgnoreCase(value);
            } else {
                realValue = value;
            }

            field.setAccessible(true);
            field.set(bean, realValue);
        } catch (Exception e) {
            log.error("【动态配置】反射设置字段失败，field={}, value={}",
                    field.getName(),
                    value,
                    e);
        }
    }

    public record FieldHolder(Object bean, Field field, String defaultValue) {
    }
}