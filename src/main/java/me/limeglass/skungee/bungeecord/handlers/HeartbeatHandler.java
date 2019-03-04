package me.limeglass.skungee.bungeecord.handlers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import me.limeglass.skungee.UniversalSkungee;
import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeBungeeHandler;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import net.md_5.bungee.api.config.ServerInfo;

public class HeartbeatHandler extends SkungeeBungeeHandler {

	static {
		registerHandler(new HeartbeatHandler(), SkungeePacketType.HEARTBEAT);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() == null)
			return null;
		int port = (int) packet.getObject();
		for (Entry<String, ServerInfo> server : servers.entrySet()) {
			InetSocketAddress inetaddress = new InetSocketAddress(address, port);
			try {
				if (server.getValue().getAddress().equals(inetaddress) || address.isAnyLocalAddress() || address.isLoopbackAddress()) {
					return serverTracker.update(server.getKey());
				// Last hope checks.
				} else if (NetworkInterface.getByInetAddress(address) != null) {
					return serverTracker.update(server.getKey());
				} else if (Inet4Address.getLocalHost().getHostAddress().equals(address.getHostAddress())) {
					return serverTracker.update(server.getKey());
				}
			} catch (SocketException socket) {
				Skungee.exception(socket, "Socket unknown host: " + inetaddress);
			} catch (UnknownHostException host) {
				Skungee.exception(host, "Unknown host: " + inetaddress);
			}
		}
		return null;
	}

	@Override
	public String toString(SkungeePacket packet) {
		return configuration.getBoolean("IgnoreSpamPackets", true) ? null : UniversalSkungee.getPacketDebug(packet);
	}

}
