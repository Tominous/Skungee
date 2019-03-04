package me.limeglass.skungee.bungeecord.storages;

import me.limeglass.skungee.bungeecord.sockets.BungeeSockets;
import me.limeglass.skungee.bungeecord.sockets.ServerTracker;
import me.limeglass.skungee.bungeecord.variables.SkungeeStorage;
import me.limeglass.skungee.objects.SkungeeVariable.Value;
import me.limeglass.skungee.objects.packets.BungeePacket;
import me.limeglass.skungee.objects.packets.BungeePacketType;

public class H2Storage extends SkungeeStorage {
	
	private final ServerTracker serverTracker;
	
	public H2Storage() {
		super("H2", "h2");
		this.serverTracker = instance.getServerTracker();
	}
	
	@Override
	public boolean initialize() {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public Value[] get(String index) {
		//TODO
		return null;
	}

	@Override
	public void delete(String... indexes) {
		//TODO
	}
	
	@Override
	public void remove(Value[] objects, String... indexes) {
		//TODO
	}

	@Override
	public void backup() {
		//TODO
	}
	
	@Override
	public void set(String name, Value[] values) {
		if (configuration.getBoolean("NetworkVariables.AutomaticSharing", false)) {
			if (!serverTracker.getServers().isEmpty()) {
				BungeeSockets.sendAll(new BungeePacket(false, BungeePacketType.UPDATEVARIABLES, name, values));
			}
		}
		//TODO
	}

	@Override
	public void shutdown() {
		// TODO
	}

}
