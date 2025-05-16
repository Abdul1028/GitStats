package org.gitstats.backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.io.IOException;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig {



     @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new ForwardedHeaderFilter());
        filterRegBean.setOrder(0);
        return filterRegBean;
    }

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {  
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000",allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Set-Cookie", "XSRF-TOKEN")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }


    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> sameSiteCookieFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                filterChain.doFilter(request, response);
                Collection<String> headers = response.getHeaders("Set-Cookie");
                boolean firstHeader = true;
                for (String header : headers) {
                    if (header.startsWith("JSESSIONID")) {
                        String newHeader = header + "; SameSite=None; Secure";
                        if (firstHeader) {
                            response.setHeader("Set-Cookie", newHeader);
                            firstHeader = false;
                        } else {
                            response.addHeader("Set-Cookie", newHeader);
                        }
                    }
                }
            }
        });

        registrationBean.setOrder(1); // After ForwardedHeaderFilter
        return registrationBean;
    }

}