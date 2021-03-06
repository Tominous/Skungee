package me.limeglass.skungee.bungeecord.handlers;

import java.net.InetAddress;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeePlayerHandler;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChatHandler extends SkungeePlayerHandler {

	static {
		registerHandler(new ChatHandler(), SkungeePacketType.PLAYERCHAT);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() == null) return null;
		for (ProxiedPlayer player : players) {
			for (String msg : (String[]) packet.getObject()) {
				player.chat(ChatColor.stripColor(msg));
			}
		}
		return null;
	}
	
}