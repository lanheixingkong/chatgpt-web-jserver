package com.shenxinduo.chatgpt.web.server.filter;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.shenxinduo.chatgpt.web.server.config.EnvConfig;
import com.shenxinduo.chatgpt.web.server.dto.ChatwebResult;

public class AuthFilter implements Filter {
	@Resource
	private EnvConfig envConfig;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String secretKey = envConfig.getAuthSecretKey();
		if (StringUtils.isNotBlank(secretKey)) {
			HttpServletRequest req = (HttpServletRequest) request;
			String token = req.getHeader("Authorization");
			String auth = "Bearer " + secretKey;
			if (!auth.equals(token)) {
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.setHeader("Content-Type", "application/json;charset=UTF-8");
				String json = JSONObject.toJSONString(ChatwebResult.newUnauthorizedResult());
				resp.getWriter().write(json);
				return;
			}
		}

		chain.doFilter(request, response);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
