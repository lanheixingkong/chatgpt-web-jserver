package com.shenxinduo.chatgpt.web.server.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChatwebResult<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3142509992634672099L;

	private static final String STATE_SUCCESS = "Success";
	private static final String STATE_FAIL = "Fail";
	private static final String STATE_UNAUTHORIZED = "Unauthorized";

	private String status;
	private String message;
	private T data;

	private ChatwebResult() {
	}

	public static <T> ChatwebResult<T> newSuccessResult() {
		ChatwebResult<T> ret = new ChatwebResult<T>();
		ret.setStatus(STATE_SUCCESS);
		return ret;
	}

	public static <T> ChatwebResult<T> newSuccessMsgResult(String msg) {
		ChatwebResult<T> ret = new ChatwebResult<T>();
		ret.setStatus(STATE_SUCCESS);
		ret.setMessage(msg);
		return ret;
	}

	public static <T> ChatwebResult<T> newSuccessResult(String msg, T data) {
		ChatwebResult<T> ret = newSuccessResult();
		ret.setMessage(msg);
		ret.setData(data);
		return ret;
	}

	public static <T> ChatwebResult<T> newSuccessResult(T data) {
		ChatwebResult<T> ret = newSuccessResult();
		ret.setData(data);
		return ret;
	}

	public static <T> ChatwebResult<T> newFailMsgResult(String failMsg) {
		ChatwebResult<T> ret = new ChatwebResult<T>();
		ret.setStatus(STATE_FAIL);
		ret.setMessage(failMsg);
		return ret;
	}

	public static <T> ChatwebResult<T> newUnauthorizedResult() {
		ChatwebResult<T> ret = new ChatwebResult<T>();
		ret.setStatus(STATE_UNAUTHORIZED);
		ret.setMessage("Error: 无访问权限 | No access rights");
		return ret;
	}

	public static <T> ChatwebResult<T> newFailResult() {
		ChatwebResult<T> ret = new ChatwebResult<T>();
		ret.setStatus(STATE_FAIL);
		return ret;
	}

	public boolean success() {
		return STATE_SUCCESS.equals(this.status);
	}
}
