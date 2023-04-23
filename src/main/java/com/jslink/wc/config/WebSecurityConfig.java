package com.jslink.wc.config;

import com.jslink.wc.security.CustomBasicAuthenticationEntryPoint;
import com.jslink.wc.service.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String REALM = "ALMS_REST_SERVICE";

    @Autowired
    private AccountServiceImpl accountService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MessageDigestPasswordEncoder("MD5");
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/wcapi/login").anonymous()
                .antMatchers("/wcapi/noauth").anonymous()
                .antMatchers("/wcapi/account/**").authenticated()
                .antMatchers("/wcapi/common/**").authenticated()
                .antMatchers("/wcapi/works/**").authenticated()
                .and().httpBasic().realmName(REALM).authenticationEntryPoint(getBasicAuthEntryPoint())
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);//We don't need sessions to be created.
    }

    @Bean
    public CustomBasicAuthenticationEntryPoint getBasicAuthEntryPoint() {
        CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint = new CustomBasicAuthenticationEntryPoint();
        customBasicAuthenticationEntryPoint.setRealmName(REALM);
        return customBasicAuthenticationEntryPoint;
    }

    //todo: 看起来没什么用.
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**").antMatchers(HttpMethod.POST, "/restservice/user")
                .antMatchers(HttpMethod.POST, "/user");
    }
}
