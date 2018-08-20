package com.net;
/*
 * 客户端维护一个状态机
 * */
public class MachineState {

	public final static int NET_STATUS_INVALID = -1;
	public final static int NET_STATUS_IDLE = 0;
	public final static int NET_STATUS_DISCONNECTION = 1;
	public final static int NET_STATUS_CONNECTING = 2;
	public final static int NET_STATUS_CONNECTED = 3;
	public final static int NET_STATUS_IDENTITY = 4;
	
}
