package com.jslink.wc.config;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class CorsFilter implements Filter{
    @Value("${access.control.allow.origin}")
    String accessContralAllowOrigin;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String reqOrigin = ((HttpServletRequest) req).getHeader("origin");
        String[] allowDomain = accessContralAllowOrigin.split(",");
        Set<String> allowedOrigins = new HashSet<>(Arrays.asList(allowDomain));

        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin",
                reqOrigin == null || reqOrigin.isEmpty() || !allowedOrigins.contains(reqOrigin) ? "*" : reqOrigin);
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
        chain.doFilter(req, res);
    }
}
