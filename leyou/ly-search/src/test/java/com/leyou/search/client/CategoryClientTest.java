package com.leyou.search.client;


import com.leyou.domain.Category;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void testCategory() {
        List<Category> categories = categoryClient.findCategoryListByIds(Arrays.asList(1l, 2l, 3l));
        Assert.assertEquals(3, categories.size());
        for (Category category : categories) {
            System.out.println("category = " + category);
        }
    }
}