package com.leyou.zuul.filters;

import com.leyou.common.entity.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.zuul.config.FiltersProperties;
import com.leyou.zuul.config.ZuulProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 登录过滤器
 */
@Component
@EnableConfigurationProperties({ZuulProperties.class, FiltersProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private ZuulProperties prop;

    @Autowired
    private FiltersProperties filterProp;

    //过滤器的类型
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    //过滤顺序
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;
    }

    /**
     * 判断请求路径
     * 是否进行过滤
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //获取上下文和request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //所有的GET请求都放行
//        String method = request.getMethod();
//        if (StringUtils.equals(method, "GET")) {
//            return false;
//        }
        //获取url
        String path = request.getRequestURI();
        //判断是否在白名单
        return !isAllowPath(path);
    }

    /**
     * 判断路径是否在白名单
     * @param path
     * @return
     */
    private Boolean isAllowPath(String path) {
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (path.startsWith(allowPath)) {
                //在白名单
                return true;
            }
        }
        return false;
    }

    //过滤逻辑

    @Override
    public Object run() throws ZuulException {
        //获取request
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //解析token
            UserInfo user = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //查看权限..此处不做
        } catch (Exception e) {
            //登陆失败不放行 拦截 默认true 放行
            ctx.setSendZuulResponse(false);

            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
