package com.shenxinduo.chatgpt.web.server.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheChatMessage {

	private String id;
	private String parentMessageId;
	private String role;
	private String content;

}
