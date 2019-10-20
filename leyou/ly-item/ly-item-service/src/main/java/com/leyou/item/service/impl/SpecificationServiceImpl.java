package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查询规格组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> findSpecGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> groups = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(groups)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return groups;
    }


    @Override
    public List<SpecParam> findParamsList(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setSearching(searching);
        List<SpecParam> params = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.GROUP_PARAM_NOT_FOUND);
        }
        return params;
    }

    /**
     * 查询商品的规格参数组和组内属性
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> findGroupsAndParams(Long cid) {
        //查询规格参数组
        List<SpecGroup> groups = findSpecGroupByCid(cid);
        //查询参数
        List<SpecParam> params = findParamsList(null, cid, null);
        //将params变为map集合：key为 groupId value为相同groupId的值的数组
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam param : params) {
            if (!map.containsKey(param.getGroupId())) {
                //判断 如果map集合中key没有当前param的组id 说明还没有开始存当前param的集合
                map.put(param.getGroupId(), new ArrayList<SpecParam>());
            }
            //数组已经存在的情况下直接添加进数组
            map.get(param.getGroupId()).add(param);
        }
//        遍历groups将params添加进组
        for (SpecGroup group : groups) {
            group.setParams(map.get(group.getId()));
        }


        /*
        该方法多次查询数据库 不利于性能
        for (SpecGroup group : groups) {
            //遍历查询组内属性并添加
            SpecParam specParam = new SpecParam();
            specParam.setGroupId(group.getId());
            List<SpecParam> params = specParamMapper.select(specParam);
            group.setSpecParams(params);
        }

         */
        return groups;
    }

}
