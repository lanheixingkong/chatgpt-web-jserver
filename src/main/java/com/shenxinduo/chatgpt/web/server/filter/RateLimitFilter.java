package com.shenxinduo.chatgpt.web.server.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.shenxinduo.chatgpt.web.server.config.EnvConfig;
import com.shenxinduo.chatgpt.web.server.dto.ChatwebResult;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class RateLimitFilter implements Filter {

	@Resource
	private EnvConfig envConfig;

	private long limitTime = 60 * 60 * 1000;// 1小时

	private static ConcurrentHashMap<String, ExpiringMap<String, Integer>> map = new ConcurrentHashMap<>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		// 获取Map对象， 如果没有则返回默认值
		// 第一个参数是key， 第二个参数是默认值
		ExpiringMap<String, Integer> uc = map.getOrDefault(req.getRequestURI(),
				ExpiringMap.builder().variableExpiration().build());
		Integer uCount = uc.getOrDefault(req.getRemoteAddr(), 0);
		int rateLimitCount = envConfig.getRateLimitCount();
		if (rateLimitCount > 0 && uCount >= rateLimitCount) { // 超过次数，不执行目标方法
			HttpServletResponse resp = (HttpServletResponse) response;
			resp.setHeader("Content-Type", "application/json;charset=UTF-8");
			String json = JSONObject
					.toJSONString(ChatwebResult.newFailMsgResult("Too many request from this IP in 1 hour"));
			resp.getWriter().write(json);
			return;
		} else if (uCount == 0) { // 第一次请求时，设置有效时间
			uc.put(req.getRemoteAddr(), uCount + 1, ExpirationPolicy.CREATED, limitTime, TimeUnit.MILLISECONDS);
		} else { // 未超过次数， 记录加一
			uc.put(req.getRemoteAddr(), uCount + 1);
		}
		map.put(req.getRequestURI(), uc);

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
