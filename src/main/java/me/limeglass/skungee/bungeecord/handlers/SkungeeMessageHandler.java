package me.limeglass.skungee.bungeecord.handlers;

import java.net.InetAddress;

import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeExecutor;
import me.limeglass.skungee.bungeecord.sockets.BungeeSockets;
import me.limeglass.skungee.objects.BungeePacket;
import me.limeglass.skungee.objects.BungeePacketType;
import me.limeglass.skungee.objects.SkungeePacket;
import me.limeglass.skungee.objects.SkungeePacketType;

public class SkungeeMessageHandler extends SkungeeExecutor {

	static {
		registerPacket(new SkungeeMessageHandler(), SkungeePacketType.SKUNGEEMESSAGES);
	}

	@Override
	public void executePacket(SkungeePacket packet, InetAddress address) {
		if (packet.eitherObjectsAreNull()) return;
		String[] messages = (String[]) packet.getObject();
		String[] channels = (String[]) packet.getSetObject();
		BungeeSockets.sendAll(new BungeePacket(false, BungeePacketType.SKUNGEEMESSAGES, messages, channels));
	}
}