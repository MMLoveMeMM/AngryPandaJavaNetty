package com.net;

public interface INetListener<T> {
	void onReceiver(T data);
	void onStatusChange(int code);
}
