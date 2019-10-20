package com.leyou.page.service.impl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceImplTest {

    @Autowired
    private PageServiceImpl service;
    @Test
    public void createHtml() {
        service.createHtml(122L);
    }
}