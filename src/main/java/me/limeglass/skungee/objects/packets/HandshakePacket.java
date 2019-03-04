package me.limeglass.skungee.objects.packets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.limeglass.skungee.objects.SkungeePlayer;

public class HandshakePacket extends SkungeePacket {

	private static final long serialVersionUID = 1118568736287179260L;
	private final Set<SkungeePlayer> whitelisted = new HashSet<>();
	private final int port, recieverPort, heartbeat, max;
	private final boolean reciever;
	private final String motd;
	
	public HandshakePacket(String motd, int max, boolean reciever, int port, int recieverPort, int heartbeat, Collection<SkungeePlayer> whitelisted) {
		super(true, SkungeePacketType.HANDSHAKE);
		this.whitelisted.addAll(whitelisted);
		this.recieverPort = recieverPort;
		this.heartbeat = heartbeat;
		this.reciever = reciever;
		this.motd = motd;
		this.port = port;
		this.max = max;
	}
	
	public Set<SkungeePlayer> getWhitelisted() {
		return whitelisted;
	}
	
	public int getMaximumPlayers() {
		return max;
	}
	
	public int getRecieverPort() {
		return recieverPort;
	}

	public boolean hasReciever() {
		return reciever;
	}
	
	public int getHeartbeat() {
		return heartbeat;
	}
	
	public String getMotd() {
		return motd;
	}

	public int getPort() {
		return port;
	}

}
