package com.leyou.search.pojo;

import java.util.Map;

/**
 * 搜索请求类
 */
public class SearchRequest {

    private Map<String, String> filter;//过滤项
    private String key;//搜索条件
    private Integer page;//当前页
    private static final int DEFAULT_SIZE = 20;//每页大小 不从页面接收 固定值
    private static final int DEFAULT_PAGE = 1;//默认页

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        //对页码进行校验
        return Math.max(page, DEFAULT_PAGE);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Integer getSize() {
        return DEFAULT_SIZE;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }
}
