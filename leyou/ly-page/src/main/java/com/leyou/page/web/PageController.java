package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String toPage(@PathVariable("id") Long spuId, Model model) {
        Map<String, Object> attributes = pageService.loadModel(spuId);
        //准备模型
        model.addAllAttributes(attributes);
        //新线程创建静态页面
        pageService.asyncExcute(spuId);
        //返回试图
        return "item";
    }
}
