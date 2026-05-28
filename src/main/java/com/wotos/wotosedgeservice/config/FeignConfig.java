package com.wotos.wotosedgeservice.config;

import feign.Feign;
import feign.Logger;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
public class FeignConfig {

    @Bean
    public Feign.Builder springMvcContract() {
        return Feign.builder().contract(new SpringMvcContract());
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        // BASIC logs only method, URL, status and timing — never headers or
        // bodies — so downstream auth tokens and PII are not leaked to logs.
        return Logger.Level.BASIC;
    }

}
