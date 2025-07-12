package com.hsurvey.userservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            System.out.println("FeignConfig - Intercepting request to: " + requestTemplate.url());
            
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                System.out.println("FeignConfig - Request URI: " + request.getRequestURI());
                
                // Forward authentication headers from Gateway
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    
                    // Forward user context headers
                    if (headerName.startsWith("X-") || 
                        headerName.equals("Authorization") || 
                        headerName.equals("Cookie")) {
                        requestTemplate.header(headerName, headerValue);
                        System.out.println("FeignConfig - Forwarding header: " + headerName + " = " + headerValue);
                    }
                }
            } else {
                System.out.println("FeignConfig - No request attributes found");
            }
        };
    }
} 