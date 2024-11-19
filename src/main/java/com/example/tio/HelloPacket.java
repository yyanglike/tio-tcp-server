package com.example.tio;

import org.tio.core.intf.Packet;

public class HelloPacket extends Packet {
	private static final long serialVersionUID = -172060606924066412L;
	public static final int HEADER_LENGTH = 4; //length of header
	public static final String CHARSET = "utf-8";
	private byte[] body;

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}