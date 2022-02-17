package com.macro.mall.security.component;

import com.macro.mall.security.config.IgnoreUrlsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author hu
 * @create 2022/2/13
 * 动态权限过滤器，用于实现基于路径的权限过滤
 */
public class DynamicSecurityFilter extends AbstractSecurityInterceptor implements Filter {
    @Autowired
    private DynamicSecurityMetadataSource dynamicSecurityMetadataSource;
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Autowired
    public void setDynamicDescionManage(DynamicAccessDecisionManager dynamicDescionManage){
        super.setAccessDecisionManager(dynamicDescionManage);
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        FilterInvocation filterInvocation=new FilterInvocation(request, servletResponse, filterChain);
        if(request.getMethod().equals(HttpMethod.OPTIONS)){
            filterInvocation.getChain().doFilter(filterInvocation.getRequest(),filterInvocation.getResponse());
            return;
        }
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        String requestURI = request.getRequestURI();

        for (String url : ignoreUrlsConfig.getUrls()) {
            if(antPathMatcher.match(url,requestURI)){
                filterInvocation.getChain().doFilter(filterInvocation.getRequest(),filterInvocation.getResponse());
                return;
            }
        }
        InterceptorStatusToken token = super.beforeInvocation(filterInvocation);
        try{
            filterInvocation.getChain().doFilter(filterInvocation.getRequest(),filterInvocation.getResponse());
        }finally {
            super.afterInvocation(token, null);
        }
    }

    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return dynamicSecurityMetadataSource;
    }
}