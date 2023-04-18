package com.shenxinduo.chatgpt.web.server.cache;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class ChatMessageCache {
	private static int maximumSize = 1000;

	private Cache<String, CacheChatMessage> cache = CacheBuilder.newBuilder().maximumSize(maximumSize) // 最多存储1000个元素
//            .expireAfterWrite(5, TimeUnit.SECONDS) // 写入后5秒钟过期
			.build();

	public CacheChatMessage getMessageById(String id) {
		return cache.getIfPresent(id);
	}

	public void putMessage(CacheChatMessage msg) {
		cache.put(msg.getId(), msg);
	}

}
