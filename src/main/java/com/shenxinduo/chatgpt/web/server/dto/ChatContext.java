package com.shenxinduo.chatgpt.web.server.dto;

import lombok.Data;

@Data
public class ChatContext {

	private String conversationId;
	private String parentMessageId;
}
