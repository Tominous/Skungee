package me.limeglass.skungee.bungeecord.handlers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.Map.Entry;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeBungeeHandler;
import me.limeglass.skungee.objects.ConnectedServer;
import me.limeglass.skungee.objects.SkungeePlayer;
import me.limeglass.skungee.objects.packets.HandshakePacket;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import net.md_5.bungee.api.config.ServerInfo;

public class HandshakeHandler extends SkungeeBungeeHandler {

	static {
		registerHandler(new HandshakeHandler(), SkungeePacketType.HANDSHAKE);
	}

	@Override
	public Object handlePacket(SkungeePacket packet, InetAddress address) {
		HandshakePacket handshake = (HandshakePacket) packet;
		int port = handshake.getPort();
		String motd = handshake.getMotd();
		int max = handshake.getMaximumPlayers();
		int heartbeat = handshake.getHeartbeat();
		boolean reciever = handshake.hasReciever();
		int recieverPort = handshake.getRecieverPort();
		Set<SkungeePlayer> whitelisted = handshake.getWhitelisted();
		try {
			for (Entry<String, ServerInfo> server : servers.entrySet()) {
				String serverAddress = server.getValue().getAddress().getAddress().getHostAddress();
				for (Enumeration<NetworkInterface> entry = NetworkInterface.getNetworkInterfaces(); entry.hasMoreElements();) {
					for (Enumeration<InetAddress> addresses = entry.nextElement().getInetAddresses(); addresses.hasMoreElements();) {
						if (addresses.nextElement().getHostAddress().equals(serverAddress) && port == server.getValue().getAddress().getPort()) {
							ConnectedServer connect = new ConnectedServer(reciever, recieverPort, port, address, heartbeat, server.getKey(), motd, max, whitelisted);
							if (!serverTracker.contains(connect)) {
								serverTracker.add(connect);
								serverTracker.update(server.getKey());
								return "CONNECTED";
							}
						}
					}
				}
				if (serverAddress.equals(address.getHostAddress()) && port == server.getValue().getAddress().getPort()) {
					ConnectedServer connect = new ConnectedServer(reciever, recieverPort, port, address, heartbeat, server.getKey(), motd, max, whitelisted);
					if (!serverTracker.contains(connect)) {
						serverTracker.add(connect);
						serverTracker.update(server.getKey());
						return "CONNECTED";
					}
				}
			}
		} catch (SocketException exception) {
			Skungee.exception(exception, "Could not find the system's local host.");
		}
		return null;
	}
	
}
