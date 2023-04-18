package com.shenxinduo.chatgpt.web.server.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shenxinduo.chatgpt.web.server.filter.AuthFilter;
import com.shenxinduo.chatgpt.web.server.filter.RateLimitFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<AuthFilter> registerMyFilter() {
		FilterRegistrationBean<AuthFilter> bean = new FilterRegistrationBean<>();
		bean.setOrder(1);
		bean.setFilter(authFilter());
		bean.addUrlPatterns("/api/chat-process");
		return bean;
	}

	@Bean
	public FilterRegistrationBean<RateLimitFilter> registerMyAnotherFilter() {
		FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
		bean.setOrder(2);
		bean.setFilter(rateLimitFilter());
		bean.addUrlPatterns("/api/chat-process");
		return bean;
	}

	@Bean
	public AuthFilter authFilter() {
		return new AuthFilter();
	}

	@Bean
	public RateLimitFilter rateLimitFilter() {
		return new RateLimitFilter();
	}
}
