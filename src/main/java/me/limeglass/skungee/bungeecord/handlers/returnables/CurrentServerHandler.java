package me.limeglass.skungee.bungeecord.handlers.returnables;

import java.net.InetAddress;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeBungeeHandler;
import me.limeglass.skungee.objects.ConnectedServer;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;

public class CurrentServerHandler extends SkungeeBungeeHandler {

	static {
		registerHandler(new CurrentServerHandler(), SkungeePacketType.CURRENTSERVER);
	}
	
	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		Object object = packet.getObject();
		if (object == null)
			return null;
		ConnectedServer server = serverTracker.getByAddress(address, (int)object);
		if (server == null)
			return null;
		return server;
	}

}
