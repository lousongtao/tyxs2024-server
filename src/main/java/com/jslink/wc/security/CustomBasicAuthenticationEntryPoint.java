package com.jslink.wc.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

//    @Override
//    public void commence(final HttpServletRequest request,
//                         final HttpServletResponse response,
//                         final AuthenticationException authException) throws IOException {
//        //Authentication failed, send error response.
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");
//
//        PrintWriter writer = response.getWriter();
//        writer.println("HTTP Status 401 : " + authException.getMessage());
//    }

    //如果返回401未认证信息, Chrome和Edge会弹出一个登录框(Firefox不会). 这会让前端看起来很难看, 不如直接redirect
    @Override
    public void commence(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final AuthenticationException authException) throws IOException {
        response.sendRedirect(request.getContextPath() + "/user/login");
    }
}
