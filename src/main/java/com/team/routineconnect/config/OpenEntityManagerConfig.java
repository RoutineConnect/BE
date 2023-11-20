package com.team.routineconnect.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

@Profile("!test")
@Configuration
public class OpenEntityManagerConfig {
    @Bean
    public FilterRegistrationBean<OpenEntityManagerInViewFilter> openEntityManagerInViewFilter() {
        FilterRegistrationBean<OpenEntityManagerInViewFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new OpenEntityManagerInViewFilter());
        bean.setOrder(Integer.MIN_VALUE);

        return bean;
    }
}
