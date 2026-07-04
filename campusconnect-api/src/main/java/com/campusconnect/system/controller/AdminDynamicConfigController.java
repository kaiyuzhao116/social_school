package com.campusconnect.system.controller;

import com.campusconnect.common.Result;
import com.campusconnect.common.dynamic.DynamicConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dynamic-config")
@RequiredArgsConstructor
public class AdminDynamicConfigController {

    private final DynamicConfigService dynamicConfigService;

    @PostMapping("/update")
    public Result<?> updateConfig(
            @RequestParam String key,
            @RequestParam String value
    ) {
        dynamicConfigService.updateConfig(key, value);
        return Result.success("配置修改成功");
    }

    @GetMapping("/get")
    public Result<?> getConfig(@RequestParam String key) {
        return Result.success(dynamicConfigService.getConfig(key));
    }
}