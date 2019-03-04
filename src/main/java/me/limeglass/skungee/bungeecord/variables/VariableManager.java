package me.limeglass.skungee.bungeecord.variables;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.spigot.utils.ReflectionUtil;
import net.md_5.bungee.config.Configuration;

public class VariableManager {
	
	private final Set<SkungeeStorage> storages = new HashSet<>();
	private final Configuration configuration;
	private SkungeeStorage main;
	
	@SuppressWarnings("unchecked")
	public VariableManager(Skungee instance) {
		this.configuration = instance.getConfig();
		if (configuration.getBoolean("NetworkVariables.Backups.Enabled", false)) {
			long time = configuration.getLong("NetworkVariables.Backups.IntervalTime", 120);
			boolean messages = configuration.getBoolean("NetworkVariables.Backups.ConsoleMessage", false);
			instance.getProxy().getScheduler().schedule(instance, new VariableBackup(instance, messages), time, time, TimeUnit.MINUTES);
		}
		try {
			Set<Class<?>> classes = ReflectionUtil.getClasses(new JarFile(instance.getFile()), "me.limeglass.skungee.bungeecord.storages");
			classes.parallelStream()
					.filter(clazz -> SkungeeStorage.class.isAssignableFrom(clazz))
					.map(clazz -> (Class<? extends SkungeeStorage>)clazz)
					.map(clazz -> {
						try {
							return clazz.newInstance();
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
							Skungee.exception(e, "Class " + clazz.getName() + " canèt be initalized.");
						}
						return null;
					})
					.filter(constructor -> constructor != null)
					.forEach(storage -> registerStorage(storage));
		} catch (IOException e) {
			Skungee.exception(e, "Failed to find any classes for Skungee Storage types");
		}
		boolean initialized = false;
		String type = configuration.getString("NetworkVariables.StorageType", "CSV");
		for (SkungeeStorage storage : storages) {
			for (String name : storage.getNames()) {
				if (type.equalsIgnoreCase(name)) {
					initialized = storage.initialize();
					main = storage;
				}
			}
		}
		if (!initialized) {
			Skungee.consoleMessage("Failed to initialize storage type: " + type);
			return;
		}
	}
	
	public void registerStorage(SkungeeStorage storage) {
		storages.add(storage);
		Skungee.debugMessage("Registered storage type: " + storage.getNames()[0]);
	}
	
	public SkungeeStorage getMainStorage() {
		return main;
	}
	
	public void backup() {
		main.backup();
	}

}
