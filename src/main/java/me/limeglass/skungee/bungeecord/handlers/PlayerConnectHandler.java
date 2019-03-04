package me.limeglass.skungee.bungeecord.handlers;

import java.net.InetAddress;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeePlayerHandler;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.utils.Utils;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;

public class PlayerConnectHandler extends SkungeePlayerHandler {

	static {
		registerHandler(new PlayerConnectHandler(), SkungeePacketType.CONNECTPLAYER);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() == null)
			return null;
		ServerInfo server = instance.getProxy().getServerInfo((String) packet.getObject());
		if (server == null)
			return null;
		if (Utils.classExists("net.md_5.bungee.api.ServerConnectRequest")) {
			Reason reason = Reason.PLUGIN;
			if (packet.getSetObject() != null)
				reason = Reason.valueOf((String) packet.getSetObject());
			ServerConnectRequest connection = ServerConnectRequest.builder()
					.reason(reason)
					.target(server)
					.retry(true)
					.build();
			players.forEach(player -> player.connect(connection));
			return null;
		}
		players.forEach(player -> player.connect(server));
		return null;
	}

}
