/**
 * 
 */
package com.elitecore.jmx;

import java.io.Serializable;

public class ResponseObject<T> implements Serializable {

	private static final long serialVersionUID = -1557621003788546595L;
	private boolean isSuccess; // true - success, false - fail
	private int responseCode;
	private T data;

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}