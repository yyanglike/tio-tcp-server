package com.example.tio;

import java.nio.ByteBuffer;

import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;

public class HelloCodec {
	public static HelloPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws TioDecodeException {
		if (readableLength < HelloPacket.HEADER_LENGTH) {
			return null;
		}

		int bodyLength = buffer.getInt();
		if (bodyLength < 0) {
			throw new TioDecodeException("bodyLength [" + bodyLength + "] is not right, remote:" + channelContext.getClientNode());
		}

		int neededLength = HelloPacket.HEADER_LENGTH + bodyLength;
		int isDataEnough = readableLength - neededLength;
		if (isDataEnough < 0) {
			return null;
		} else
		{
			HelloPacket imPacket = new HelloPacket();
			if (bodyLength > 0) {
				byte[] dst = new byte[bodyLength];
				buffer.get(dst);
				imPacket.setBody(dst);
			}
			return imPacket;
		}
	}
	
	public static ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
		HelloPacket helloPacket = (HelloPacket) packet;
		byte[] body = helloPacket.getBody();
		int bodyLen = 0;
		if (body != null) {
			bodyLen = body.length;
		}

		int allLen = HelloPacket.HEADER_LENGTH + bodyLen;
		ByteBuffer buffer = ByteBuffer.allocate(allLen);
		buffer.order(tioConfig.getByteOrder());
		buffer.putInt(bodyLen);
		if (body != null) {
			buffer.put(body);
		}
		return buffer;
	}
}