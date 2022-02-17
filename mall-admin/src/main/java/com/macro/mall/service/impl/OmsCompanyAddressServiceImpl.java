package com.macro.mall.service.impl;

import com.macro.mall.mapper.OmsCompanyAddressMapper;
import com.macro.mall.model.OmsCompanyAddress;
import com.macro.mall.model.OmsCompanyAddressExample;
import com.macro.mall.service.OmsCompanyAddressService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hu
 * @create 2022/2/16
 */
public class OmsCompanyAddressServiceImpl implements OmsCompanyAddressService {
    @Autowired
    OmsCompanyAddressMapper omsCompanyAddressMapper;
    @Override
    public List<OmsCompanyAddress> list() {
        return omsCompanyAddressMapper.selectByExample(new OmsCompanyAddressExample());
    }
}