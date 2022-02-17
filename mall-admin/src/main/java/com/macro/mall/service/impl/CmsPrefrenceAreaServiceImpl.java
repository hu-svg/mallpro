package com.macro.mall.service.impl;

import com.macro.mall.mapper.CmsPrefrenceAreaMapper;
import com.macro.mall.model.CmsPrefrenceArea;
import com.macro.mall.model.CmsPrefrenceAreaExample;
import com.macro.mall.service.CmsPrefrenceAreaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hu
 * @create 2022/2/16
 */
public class CmsPrefrenceAreaServiceImpl implements CmsPrefrenceAreaService {
    @Autowired
    CmsPrefrenceAreaMapper cmsPrefrenceAreaMapper;
    @Override
    public List<CmsPrefrenceArea> listAll() {
        return cmsPrefrenceAreaMapper.selectByExample(new CmsPrefrenceAreaExample());
    }
}