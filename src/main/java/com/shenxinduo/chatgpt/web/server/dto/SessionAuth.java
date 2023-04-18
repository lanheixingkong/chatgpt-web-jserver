package com.shenxinduo.chatgpt.web.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionAuth {

	private String model;
	private boolean auth;

}
