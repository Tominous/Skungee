package me.limeglass.skungee.bungeecord.bungeetablistplus;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import me.limeglass.skungee.UniversalSkungee;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeBungeeHandler;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;

public class TablistHeaderHandler extends SkungeeBungeeHandler {

	//BungeeTabListPlus
	
	static {
		registerHandler(new TablistHeaderHandler(), SkungeePacketType.BTLP_TABLISTHEADER);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() == null) return null;
		CustomTablist[] tablists = (CustomTablist[]) packet.getObject();
		Set<String> headers = new HashSet<String>();
		for (CustomTablist tablist : tablists) {
			headers.add(tablist.getHeader());
		}
		return headers;
	}

	@Override
	public String toString(SkungeePacket packet) {
		return UniversalSkungee.getPacketDebug(packet);
	}
}
