package com.app.exception;

public class SameAccountTransferException extends RuntimeException {
	public SameAccountTransferException(String msg){
		super(msg);
	}

}
