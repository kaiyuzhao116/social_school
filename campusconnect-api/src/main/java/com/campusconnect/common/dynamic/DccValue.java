package com.campusconnect.common.dynamic;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DccValue {

    /**
     * 格式：key:默认值
     * 例如：traffic.qps:30
     */
    String value();
}