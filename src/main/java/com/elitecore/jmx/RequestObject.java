package com.elitecore.jmx;

import java.io.Serializable;

public class RequestObject<T> implements Serializable {
		
	private static final long serialVersionUID = 6017289036022564945L;

	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}