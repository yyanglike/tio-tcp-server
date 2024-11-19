package com.example.tio;

import java.io.IOException;

import org.tio.server.DefaultTioServerListener;
import org.tio.server.TioServerConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.TioServerHandler;
import org.tio.server.intf.TioServerListener;

public class HelloServerStarter {
	public static TioServerHandler tioHandler = new HelloTioServerHandler();
	public static TioServerListener tioListener = new DefaultTioServerListener();
	public static TioServerConfig tioServerConfig = new TioServerConfig("hello-tio-server", tioHandler, tioListener);
	public static TioServer tioServer = new TioServer(tioServerConfig);
	public static String serverIp = null;
	public static int serverPort = Const.PORT;

	public static void main(String[] args) throws IOException {
		tioServerConfig.setHeartbeatTimeout(Const.TIMEOUT);
		tioServer.start(serverIp, serverPort);
	}
}