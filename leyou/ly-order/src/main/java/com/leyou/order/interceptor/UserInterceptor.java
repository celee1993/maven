package com.leyou.order.interceptor;


import com.leyou.common.entity.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.order.config.OrderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 身份校验拦截器
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();
    private OrderProperties prop;

    public UserInterceptor(OrderProperties prop) {
        this.prop = prop;
    }


    /**
     * 提前拦截的逻辑
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            UserInfo info = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //传递info
            //  request.setAttribute("user",info);//(方案一)不推荐
            //通过线程与user绑定 可以避免线程安全问题
            tl.set(info);
            //放行
            return true;
        } catch (Exception e) {
            log.error("[购物车服务]用户解析失败");
            return false;
        }
    }

    /**
     * 进行完毕后进行删除
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    /**
     * 返回储存的user
     * @return
     */
    public static UserInfo getUser() {
        return tl.get();
    }
}
