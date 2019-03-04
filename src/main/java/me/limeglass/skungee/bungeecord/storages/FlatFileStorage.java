package me.limeglass.skungee.bungeecord.storages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.sockets.BungeeSockets;
import me.limeglass.skungee.bungeecord.sockets.ServerTracker;
import me.limeglass.skungee.bungeecord.variables.SkungeeStorage;
import me.limeglass.skungee.objects.SkungeeVariable.Value;
import me.limeglass.skungee.objects.packets.BungeePacket;
import me.limeglass.skungee.objects.packets.BungeePacketType;

public class FlatFileStorage extends SkungeeStorage {
	
	private final ServerTracker serverTracker;
	private final File folder, file, backups;
	private final String DELIMITER = "@: ";
	private FileWriter writer;
	private boolean loading;
	private final Gson gson;
	
	public FlatFileStorage() {
		super("CSV", "flatfile");
		this.serverTracker = instance.getServerTracker();
		this.folder = new File(variablesFolder);
		folder.mkdirs();
		this.backups = new File(folder + File.separator + "backups" + File.separator);
		backups.mkdirs();
		this.file = new File(folder, "variables.csv");
		this.gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
				.setLenient()
				.create();
	}
	
	private void header() throws IOException {
		writer.append("\n# Skungee's variable database.\n");
		writer.append("# Please do not modify this file manually, thank you!\n\n");
	}
	
	@Override
	public boolean initialize() {
		if (file.exists()) {
			load();
			return true;
		}
		try {
			writer = new FileWriter(file);
			header();
			writer.flush();
			Skungee.debugMessage("Successfully created CSV variables database!");
		} catch (IOException e) {
			Skungee.exception(e, "Failed to create a CSV variable database.");
			return false;
		}
		return true;
	}
	
	@Override
	public Value[] get(String index) {
		if (index.endsWith("::*")) {
			List<Value> values = new ArrayList<>();
			index = index.substring(0, index.length() - 1);
			for (Entry<String, Value[]> entry : variables.entrySet()) {
				if (entry.getKey().startsWith(index)) {
					Value[] data = variables.get(entry.getKey());
					if (data == null)
						continue;
					for (Value value : data) {
						values.add(value);
					}
				}
			}
			Value[] data = variables.get(index);
			if (data != null) {
				for (Value value : data) {
					values.add(value);
				}
			}
			return values.toArray(new Value[values.size()]);
		}
		return variables.get(index);
	}

	@Override
	public void delete(String... indexes) {
		List<String> list = Lists.newArrayList(indexes);
		for (String index : indexes) {
			if (!index.endsWith("::*"))
				continue;
			String varIndex = index.substring(0, index.length() - 1);
			for (Entry<String, Value[]> entry : variables.entrySet()) {
				if (entry.getKey().startsWith(varIndex)) {
					list.add(entry.getKey());
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			Skungee.exception(e, "Failed to close writer for removing index");
		}
		for (String index : list) {
			if (variables.containsKey(index) && !loading) {
				variables.remove(index);
			}
		}
		loadFromHash();
	}
	
	@Override
	public void remove(Value[] objects, String... indexes) {
		List<String> list = Lists.newArrayList(indexes);
		for (String index : indexes) {
			if (!index.endsWith("::*"))
				continue;
			index = index.substring(0, index.length() - 1);
			for (Entry<String, Value[]> entry : variables.entrySet()) {
				if (entry.getKey().startsWith(index)) {
					list.add(entry.getKey());
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			Skungee.exception(e, "Failed to close writer for removing index");
		}
		if (!loading) {
			for (String index : list) {
				if (!variables.containsKey(index))
					continue;
				Value[] v = variables.get(index);
				if (v == null) continue;
				List<Value> values = Lists.newArrayList(v);
				for (Value value : v) {
					for (Value object : objects) {
						if (value.isSimilar(object)) {
							values.remove(value);
						}
					}
				}
				if (values.isEmpty()) {
					variables.remove(index);
				} else {
					variables.put(index, values.toArray(new Value[values.size()]));
				}
			}
			loadFromHash();
		}
	}

	@Override
	public void backup() {
		try {
			writer.close();
		} catch (IOException e) {
			Skungee.exception(e, "Error closing the variable flatfile writter");
		}
		Date date = new Date();
		File newFile = new File(backups, date.toString().replaceAll(":", "-") + ".csv");
		try {
			Files.copy(file.toPath(), newFile.toPath());
		} catch (IOException e) {
			Skungee.exception(e, "Failed to backup flatfile");
		}
		load();
	}
	
	private void load() {
		String line = "";
		BufferedReader reader = null;
		try {
			//Key = index, Value = string serialized value.
			Map<String, String> map = new HashMap<>();
			reader = new BufferedReader(new FileReader(file));
			//Skip the information at the top of the variables.csv file.
			for (int i = 0; i < 4; i ++) {
				reader.readLine();
			}
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(DELIMITER, 2);
				if (values.length == 2)
					map.put(values[0], values[1]);
			}
			writer = new FileWriter(file);
			header();
			for (Entry<String, String> data : map.entrySet()) {
				Value[] values = gson.fromJson(data.getValue(), Value[].class);
				set(data.getKey(), values);
			}
			reader.close();
		} catch (IOException e) {
			Skungee.exception(e, "Failed to load and write variables.");
		}
	}
	
	@Override
	public void set(String name, Value[] values) {
		if (configuration.getBoolean("NetworkVariables.AutomaticSharing", false)) {
			if (!serverTracker.getServers().isEmpty()) {
				BungeeSockets.sendAll(new BungeePacket(false, BungeePacketType.UPDATEVARIABLES, name, values));
			}
		}
		if (variables.containsKey(name) && !loading) {
			if (!configuration.getBoolean("NetworkVariables.AllowOverrides", true))
				return;
			try {
				writer.close();
			} catch (IOException e) {
				Skungee.exception(e, "Failed to close the writer while setting the value: " + name);
			}
			variables.remove(name);
			loadFromHash();
		}
		variables.put(name, values);
		try {
			writer.append(name);
			writer.append(DELIMITER);
			writer.append(gson.toJson(values));
			writer.append("\n");
		} catch (IOException e) {
			try {
				writer = new FileWriter(file);
			} catch (IOException e1) {}
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				Skungee.debugMessage("Error flushing data while writing!");
				e.printStackTrace();
			}
		}
	}
	
	private void loadFromHash() {
		loading = true;
		try {
			writer = new FileWriter(file);
			header();
			if (!variables.isEmpty()) {
				ArrayList<String> ids = Lists.newArrayList(variables.keySet());
				Iterator<String> iterator = ids.iterator();
				while (iterator.hasNext()) {
					String ID = iterator.next();
					set(ID, variables.get(ID));
				}
			}
		} catch (IOException e) {
			Skungee.exception(e, "Error flushing writer while loading from hash!");
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				Skungee.exception(e, "Error flushing writer while loading from hash!");
			}
		}
		loading = false;
	}

	@Override
	public void shutdown() {
		backup();
		variables.clear();
	}

}
