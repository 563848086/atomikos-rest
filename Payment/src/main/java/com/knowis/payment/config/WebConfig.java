package com.knowis.payment.config;


import com.atomikos.remoting.spring.rest.TransactionAwareRestContainerFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ParticipantsProviderMessageConverter());
    }

    @Bean
    public FilterRegistrationBean<TransactionAwareRestContainerFilter> transactionAwareFilter() {
        FilterRegistrationBean<TransactionAwareRestContainerFilter> registrationBean = new FilterRegistrationBean<>();
        TransactionAwareRestContainerFilter filter = new TransactionAwareRestContainerFilter();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

