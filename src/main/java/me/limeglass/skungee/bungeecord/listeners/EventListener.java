package me.limeglass.skungee.bungeecord.listeners;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.sockets.BungeeSockets;
import me.limeglass.skungee.bungeecord.sockets.ServerTracker;
import me.limeglass.skungee.objects.ConnectedServer;
import me.limeglass.skungee.objects.SkungeePlayer;
import me.limeglass.skungee.objects.packets.BungeePacket;
import me.limeglass.skungee.objects.packets.BungeePacketType;
import me.limeglass.skungee.objects.packets.ServerPingPacket;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

public class EventListener implements Listener {
	
	private final ServerTracker serverTracker;
	private final Configuration configuration;
	private final Skungee instance;
	
	public EventListener(Skungee instance) {
		this.serverTracker = instance.getServerTracker();
		this.configuration = instance.getConfig();
		this.instance = instance;
	}
	
	@EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (player == null)
			return;
		SkungeePlayer skungeePlayer = new SkungeePlayer(false, player.getUniqueId(), player.getName());
		Server server = player.getServer();
		if (server == null)
			return;
		BungeePacket packet = new BungeePacket(false, BungeePacketType.PLAYERSWITCH, server.getInfo().getName(), null, skungeePlayer);
		BungeeSockets.sendAll(packet);
    }
	
	@EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (player == null)
			return;
		SkungeePlayer skungeePlayer = new SkungeePlayer(false, player.getUniqueId(), player.getName());
		Server server = player.getServer();
		if (server == null)
			return;
		ConnectedServer[] servers = serverTracker.getServer(server.getInfo().getName());
		if (servers == null || servers.length <= 0)
			return;
		BungeePacket packet = new BungeePacket(false, BungeePacketType.PLAYERDISCONNECT, servers[0].getName(), null, skungeePlayer);
		BungeeSockets.sendAll(packet);
    }
	
	@EventHandler
    public void onCommand(ChatEvent event) {
		Optional<ProxiedPlayer> proxied = instance.getProxy().getPlayers().parallelStream()
				.filter(player -> player.getAddress().equals(event.getSender().getAddress()))
				.findAny();
		Optional<ServerInfo> serverInfo = instance.getProxy().getServers().values().parallelStream()
				.filter(server -> server.getAddress().equals(event.getReceiver().getAddress()))
				.findAny();
		if (!proxied.isPresent() || !serverInfo.isPresent())
			return;
		ProxiedPlayer player = proxied.get();
		ServerInfo server = serverInfo.get();
		SkungeePlayer skungeePlayer = new SkungeePlayer(false, player.getUniqueId(), player.getName());
		BungeePacket packet = new BungeePacket(true, BungeePacketType.PLAYERCHAT, event.getMessage(), server.getName(), skungeePlayer);
		if (event.isCommand())
			packet = new BungeePacket(true, BungeePacketType.PLAYERCOMMAND, event.getMessage(), server.getName(), skungeePlayer);
		if (BungeeSockets.sendAll(packet).contains(true))
			event.setCancelled(true);
    }
	
	@EventHandler
    public void onPing(ProxyPingEvent event) {
		if (configuration.getBoolean("DisablePingEvent", false))
			return;
		ServerPing response = event.getResponse();
		if (response == null)
			return;
		//This will return all the modified ping event-values from all the scripts on every server.
		//If a script wants the motd to be something but another server wants differently, this will default to the last server as that's not possible. The Bungeecord only has one motd.
		//Only one script should handle modifying the Bungeecord motd. If it's a global script and all the values are the same, Skungee will handle that.
		ServerPingPacket packet = new ServerPingPacket(true, BungeePacketType.SERVERLISTPING, ProtocolConstants.SUPPORTED_VERSION_IDS);
		Protocol version = response.getVersion();
		packet.setVersion(version.getName() + ":" + version.getProtocol());
		packet.setDescription(response.getDescriptionComponent().toLegacyText());
		if (response.getPlayers().getSample() != null) {
			PlayerInfo[] playerInfo = response.getPlayers().getSample();
			SkungeePlayer[] players = new SkungeePlayer[playerInfo.length];
			int i = 0;
			for (PlayerInfo info : playerInfo) {
				players[i] = new SkungeePlayer(false, info.getUniqueId(), info.getName());
				i++;
			}
			packet.setPlayers(players);
		}
		//send.setPingPlayers(serializer.serialize(ping.getPlayers()));
		//ping.getPlayers().getMax(), ping.getModinfo()
		for (Object object : BungeeSockets.sendAll(packet)) {
			if (object == null)
				continue;
			ServerPingPacket returned = (ServerPingPacket) object;
			//Because Skript and Bungeecord don't handle new line and its kept.
			String description = returned.getDescription();
			String input = Skungee.cc(description.replaceAll(Pattern.quote("\\n"), "\n"));
			if (description != null)
				response.setDescriptionComponent(new TextComponent(input));
			if (returned.getVersion() != null) {
				String[] protocol = returned.getVersion().split(Pattern.quote(":"));
				if (protocol.length > 1)
					response.setVersion(new Protocol(protocol[0], Integer.parseInt(protocol[1])));
			}
			String location = returned.getFavicon();
			if (location != null) {
				try {
					File file = new File(location);
					BufferedImage image;
					if (file.exists() && (location.endsWith(".png") || location.endsWith(".jpg"))) {
						image = ImageIO.read(file);
						if (image.getWidth() != 64 || image.getHeight() != 64 ) {
							Skungee.consoleMessage("The image at location " + location + " must be exactly 64x64 pixels.");
							image = null;
						}
					} else {
						image = ImageIO.read(new URL(location));
					}
					if (image != null)
						response.setFavicon(Favicon.create(image));
				} catch (IOException e) {
					Skungee.infoMessage("Could not find URL/Image under " + location + " or the website did not allow/return properly. You can use https://imgur.com/ which is a valid image hosting website.");
				}
			}
			SkungeePlayer[] players = returned.getPlayers();
			if (players != null && players.length > 0) {
				PlayerInfo[] info = new PlayerInfo[returned.getPlayers().length];
				int spot = 0;
				for (SkungeePlayer player : returned.getPlayers()) {
					info[spot] = new PlayerInfo(player.getName(), player.getUUID()); 
					spot++;
				}
				response.getPlayers().setSample(info);
			}
		}
		event.setResponse(response);
    }

}
