package me.limeglass.skungee.bungeecord.handlers;

import java.net.InetAddress;

import me.limeglass.skungee.bungeecord.handlercontroller.SkungeePlayerHandler;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionbarHandler extends SkungeePlayerHandler {

	static {
		registerHandler(new ActionbarHandler(), SkungeePacketType.ACTIONBAR);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() == null) return null;
		String message = (String) packet.getObject();
		players.forEach(p -> p.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message)));
		return null;
	}
	
}
