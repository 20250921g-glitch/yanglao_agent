package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.ServiceOrderSetting;
import com.care.module.service.worker.mapper.ServiceOrderSettingMapper;
import org.springframework.stereotype.Service;

@Service
public class ServiceOrderSettingService extends ServiceImpl<ServiceOrderSettingMapper, ServiceOrderSetting> {

    public void updateSetting(ServiceOrderSetting setting) {
        updateById(setting);
    }
}
