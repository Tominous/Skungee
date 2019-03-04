package me.limeglass.skungee.bungeecord.sockets;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.objects.ConnectedServer;
import me.limeglass.skungee.objects.packets.BungeePacket;
import me.limeglass.skungee.objects.packets.BungeePacketType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;

public class ServerTracker {
	
	private final Set<ConnectedServer> notResponding = new HashSet<>();
	private final Map<ConnectedServer, Long> tracker = new HashMap<>();
	private final Set<ConnectedServer> servers = new HashSet<>();
	private final Configuration configuration;
	private final Skungee instance;
	
	public ServerTracker(Skungee instance) {
		this.instance = instance;
		this.configuration = instance.getConfig();
		ProxyServer.getInstance().getScheduler().schedule(instance, new Runnable() {
			@Override
			public void run() {
				if (servers.isEmpty())
					return;
				for (ConnectedServer server : servers) {
					if (tracker.containsKey(server)) {
						long trys = configuration.getLong("Tracker.allowedTrys", 4);
						long lastupdated = tracker.get(server) + (trys * server.getHeartbeat());
						if (lastupdated < System.currentTimeMillis()) {
							if (!notResponding.contains(server))
								notResponding(server);
						}
					} else {
						tracker.put(server, System.currentTimeMillis() + (5 * server.getHeartbeat()));
					}
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
	}
	
	public void notResponding(ConnectedServer server) {
		if (server == null)
			return;
		Skungee.debugMessage("Server " + server.getName() + " has stopped responding!");
		if (configuration.getBoolean("Tracker.DisableTracking", false)) {
			remove(server);
		} else {
			notResponding.add(server);
		}
	}
	
	public boolean update(String string) {
		if (getServer(string) == null)
			return true;
		ConnectedServer server = getServer(string)[0];
		if (server != null) {
			tracker.put(server, System.currentTimeMillis());
			if (notResponding.contains(server)) {
				notResponding.remove(server);
				Skungee.debugMessage(server.getName() + " started responding again!");
			}
			globalScripts(server);
			return false;
		} else {
			return true; //Tells the system this server isn't connected.
		}
	}
	
	public ConnectedServer[] getServer(String name) {
		if (servers.isEmpty())
			return null;
		for (ConnectedServer server : servers) {
			if (server.getName().equalsIgnoreCase(name))
				return new ConnectedServer[] {server};
		}
		if (name.contains(":")) {
			Set<ConnectedServer> connected = new HashSet<>();
			String[] addresses = (name.contains(",")) ? name.split(",") : new String[] {name};
			for (String address : addresses) {
				if (!address.contains(":")) {
					ConnectedServer possiblyNamed = getServer(address)[0];
					if (possiblyNamed != null)
						connected.add(possiblyNamed);
					continue;
				}
				String[] ipPort = address.split(":");
				try {
					for (ConnectedServer server : servers) {
						if (server.getAddress().equals(InetAddress.getByName(ipPort[0]))) {
							if (server.getPort() == Integer.parseInt(ipPort[1])) {
								connected.add(server);
							}
						}
					}
				} catch (UnknownHostException e) {
					Skungee.consoleMessage("There was no server found with the address: " + Arrays.toString(ipPort));
				}
			}
			if (!connected.isEmpty())
				return connected.toArray(new ConnectedServer[connected.size()]);
		}
		return null;
	}
	
	public ConnectedServer getByAddress(InetAddress address, int port) {
		if (servers.isEmpty())
			return null;
		for (ConnectedServer server : servers) {
			if (server.getAddress().equals(address) && server.getPort() == port) {
				return server;
			}
		}
		return null;
	}
	
	public ConnectedServer getLocalByPort(int port) {
		if (servers.isEmpty())
			return null;
		try {
			for (ConnectedServer server : servers) {
				InetAddress address = server.getAddress();
				if (address.isAnyLocalAddress() || address.isLoopbackAddress())
					return server;
				for (Enumeration<NetworkInterface> entry = NetworkInterface.getNetworkInterfaces(); entry.hasMoreElements();) {
					for (Enumeration<InetAddress> addresses = entry.nextElement().getInetAddresses(); addresses.hasMoreElements();) {
						if (addresses.nextElement().getHostAddress().equals(server.getAddress().getHostAddress()) && port == server.getPort()) {
							return server;
						}
					}
				}
			}
		} catch (SocketException exception) {
			Skungee.exception(exception, "Could not find the system's local host.");
		}
		return null;
	}
	
	private void globalScripts(ConnectedServer server) {
		if (!configuration.getBoolean("GlobalScripts.Enabled", true)) 
			return;
		File[] files = instance.getScriptsFolder().listFiles();
		if (files.length <= 0)
			return;
		Map<String, List<String>> data = new HashMap<>();
		String charset = configuration.getString("GlobalScripts.Charset", "default");
		Charset chars = Charset.defaultCharset();
		if (!charset.equals("default"))
			chars = Charset.forName(charset);
		for (File script : files) {
			try {
				if (script.isDirectory()) {
					if (script.getName().equalsIgnoreCase(server.getName())) {
						for (File directory : script.listFiles()) {
							data.put(directory.getName(), Files.readAllLines(directory.toPath(), chars));
						}
					}
					continue;
				}
				data.put(script.getName(), Files.readAllLines(script.toPath(), chars));
			} catch (IOException e) {
				Skungee.exception(e, "Charset " + charset + " does not support some symbols in script " + script.getName());
			}
		}
		BungeeSockets.send(server, new BungeePacket(false, BungeePacketType.GLOBALSCRIPTS, data));
	}
	
	public Set<ConnectedServer> getServers() {
		return servers;
	}
	
	public Boolean contains(ConnectedServer server) {
		return servers.contains(server);
	}
	
	public Boolean isResponding(ConnectedServer server) {
		return !notResponding.contains(server);
	}

	public void add(ConnectedServer server) {
		Iterator<ConnectedServer> iterator = servers.iterator();
		while (iterator.hasNext()) {
			ConnectedServer connected = iterator.next();
			if (!connected.getAddress().equals(server.getAddress()))
				continue;
			if (connected.getPort() != server.getPort())
				continue;
			iterator.remove();
		}
		servers.add(server);
		Skungee.consoleMessage("Connected to server " + server.getName() + " with port " + server.getPort());
	}
	
	public void remove(ConnectedServer server) {
		notResponding.remove(server);
		tracker.remove(server);
		servers.remove(server);
	}

}
