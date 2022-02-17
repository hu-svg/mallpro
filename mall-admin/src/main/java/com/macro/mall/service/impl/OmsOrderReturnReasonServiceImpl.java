package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.OmsOrderReturnReasonMapper;
import com.macro.mall.model.OmsOrderReturnReason;
import com.macro.mall.model.OmsOrderReturnReasonExample;
import com.macro.mall.service.OmsOrderReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hu
 * @create 2022/2/17
 */
public class OmsOrderReturnReasonServiceImpl implements OmsOrderReturnReasonService {
    @Autowired
    OmsOrderReturnReasonMapper returnReasonMapper;
    @Override
    public int create(OmsOrderReturnReason returnReason) {
        return returnReasonMapper.insert(returnReason);
    }

    @Override
    public int update(Long id, OmsOrderReturnReason returnReason) {
        returnReason.setId(id);
        return returnReasonMapper.updateByPrimaryKey(returnReason);
    }

    @Override
    public int delete(List<Long> ids) {
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        OmsOrderReturnReasonExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        return returnReasonMapper.deleteByExample(example);
    }

    @Override
    public List<OmsOrderReturnReason> list(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        List<OmsOrderReturnReason> omsOrderReturnReasons = returnReasonMapper.selectByExample(new OmsOrderReturnReasonExample());
        return omsOrderReturnReasons;
    }

    @Override
    public int updateStatus(List<Long> ids, Integer status) {
        OmsOrderReturnReasonExample example = new OmsOrderReturnReasonExample();
        OmsOrderReturnReasonExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        OmsOrderReturnReason reason = new OmsOrderReturnReason();
        reason.setStatus(status);
        return returnReasonMapper.updateByExampleSelective(reason,example);
    }

    @Override
    public OmsOrderReturnReason getItem(Long id) {
        return returnReasonMapper.selectByPrimaryKey(id);
    }
}