package com.example.tio;

import java.nio.ByteBuffer;
import org.tio.core.Tio;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerHandler;

public class HelloTioServerHandler implements TioServerHandler {

	@Override
	public HelloPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws TioDecodeException {
		return HelloCodec.decode(buffer, limit, position, readableLength, channelContext);
	}

	@Override
	public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
		return HelloCodec.encode(packet, tioConfig, channelContext);
	}

	@Override
	public void handler(Packet packet, ChannelContext channelContext) throws Exception {
		HelloPacket helloPacket = (HelloPacket) packet;
		byte[] body = helloPacket.getBody();
		if (body != null) {
			String str = new String(body, HelloPacket.CHARSET);
			// System.out.println("receive msg：" + str);
			HelloPacket resppacket = new HelloPacket();
			resppacket.setBody(("Received your message, your message is:" + str).getBytes(HelloPacket.CHARSET));
			Tio.send(channelContext, resppacket);
		}
		return;
	}
}