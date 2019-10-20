package com.leyou.item.service;

import com.leyou.domain.SpecGroup;
import com.leyou.domain.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> findSpecGroupByCid(Long cid);

    List<SpecParam> findParamsList(Long gid, Long cid, Boolean searching);

    List<SpecGroup> findGroupsAndParams(Long cid);
}
