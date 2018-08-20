package com.net;

public class ClientMain {

	private Client mClient;
	
	private void startClient() {
		mClient=new Client("127.0.0.1", 8009);
		mClient.start();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ClientMain().startClient();
	}

}
