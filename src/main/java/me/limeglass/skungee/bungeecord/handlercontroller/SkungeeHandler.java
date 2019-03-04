package me.limeglass.skungee.bungeecord.handlercontroller;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Optional;

import java.util.Set;

import me.limeglass.skungee.UniversalSkungee;
import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.sockets.ServerTracker;
import me.limeglass.skungee.bungeecord.variables.SkungeeStorage;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import net.md_5.bungee.config.Configuration;

public abstract class SkungeeHandler {

	protected SkungeePacketType[] types = new SkungeePacketType[] {SkungeePacketType.CUSTOM};
	protected static Set<SkungeeHandler> registered = new HashSet<>();
	protected final SkungeeStorage storage;
	protected final Configuration configuration;
	protected final ServerTracker serverTracker;
	protected final Skungee instance;
	protected SkungeePacket packet;
	protected InetAddress address;
	protected String name;
	
	public SkungeeHandler() {
		this.instance = Skungee.getInstance();
		this.configuration = instance.getConfig();
		this.serverTracker = instance.getServerTracker();
		this.storage = instance.getVariableManager().getMainStorage();
	}
	
	protected static void registerHandler(SkungeeHandler handler, String name) {
		handler.setName(name);
		if (!registered.contains(handler)) registered.add(handler);
	}
	
	protected static void registerHandler(SkungeeHandler handler, SkungeePacketType... types) {
		handler.setTypes(types);
		for (SkungeePacketType type : types) {
			registerHandler(handler, type.name());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> getHandler(Class<? extends SkungeeHandler> type) {
		return (Optional<T>) registered.parallelStream()
				.filter(handler -> type.isAssignableFrom(handler.getClass()))
				.findFirst();
	}
	
	public static Optional<SkungeeHandler> getHandler(SkungeePacketType type) {
		for (SkungeeHandler handler : registered) {
			for (SkungeePacketType packetType : handler.getTypes()) {
				if (packetType == type) {
					return Optional.of(handler);
				}
			}
		}
		return Optional.empty();
	}
	
	public static Optional<SkungeeHandler> getHandler(String name) {
		return registered.parallelStream()
				.filter(handler -> handler.getName().equals(name))
				.findFirst();
	}
	
	public void setTypes(SkungeePacketType... types) {
		this.types = types;
	}
	
	public SkungeePacketType[] getTypes() {
		return types;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Called when the packet handler is requested.
	 * @param packet The incoming packet to handle.
	 * @param address The address the packet came from.
	 * @return If the packet should be accepted or not.
	 */
	public abstract boolean onPacketCall(SkungeePacket packet, InetAddress address);
	
	/**
	 * The main packet handler. This is what is defined to happen when the packet comes in.
	 * @param packet The incoming packet to handle.
	 * @param address The address the packet came from.
	 * @return The value to be send back to the sender if it's a returnable packet.
	 */
	public abstract Object handlePacket(SkungeePacket packet, InetAddress address);
	
	public Object callPacket(SkungeePacket packet, InetAddress address) {
		this.packet = packet;
		this.address = address;
		String string = toString(packet);
		if (string != null)
			Skungee.debugMessage("Recieved " + string);
		if (!onPacketCall(packet, address))
			return null;
		return handlePacket(packet, address);
	}
	
	protected String toString(SkungeePacket packet) {
		return UniversalSkungee.getPacketDebug(packet);
	}

}
