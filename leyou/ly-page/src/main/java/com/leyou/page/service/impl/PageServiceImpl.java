package com.leyou.page.service.impl;

import com.leyou.common.utils.ThreadUtils;
import com.leyou.domain.Spu;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine engine;
    /**
     * 加载详情页面的模型
     *
     * @param spuId
     * @return
     */
    @Override
    public Map<String, Object> loadModel(Long spuId) {
        //准备map
        Map<String, Object> map = new HashMap<>();
        Spu spu = goodsClient.findSpuById(spuId);
        Long cid3 = spu.getCid3();
        //封装map模型
        map.put("skus", spu.getSkus());
        map.put("categories", categoryClient.findCategoriesByCid3(cid3));
        map.put("brand", brandClient.findBrandsById(spu.getBrandId()));
        map.put("subTitle", spu.getSubTitle());
        map.put("title", spu.getTitle());
        map.put("detail", goodsClient.findDetail(spuId));
        map.put("specs", specificationClient.findGroupsAndParams(cid3));
        return map;
    }



    /**
     * 通过魔板引擎将页面静态化到本地文件（后期部署时候可以将该项目
     * 和nginx同时部署 直接将页面静态化到服务器本地）
     *
     */
    public void createHtml(Long spuId) {
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        //目标文件
        File file = new File("D:\\IdeaProjects2\\upload_leyou", spuId + ".html");
        //判断是否存在
        if (file.exists()) {
            file.delete();
        }
//        打印流
        try (PrintWriter writer = new PrintWriter(file, "utf-8")) {
            //生成静态页
            engine.process("item", context, writer);
        } catch (Exception e) {
            log.error("生成静态页异常",e);
        }
    }

    /**
     * 删除静态页面
     * @param spuId
     */
    @Override
    public void deleteHtml(Long spuId) {
        //目标文件
        File file = new File("D:\\IdeaProjects2\\upload_leyou", spuId + ".html");
        //删除
        file.delete();
    }

    /**
     * 新线程创建静态页面
     * @param spuId
     */
    @Override
    public void asyncExcute(Long spuId) {
        ThreadUtils.execute(()->createHtml(spuId));
    }
}
