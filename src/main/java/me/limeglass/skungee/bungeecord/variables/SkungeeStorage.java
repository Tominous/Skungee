package me.limeglass.skungee.bungeecord.variables;

import java.io.File;
import java.util.TreeMap;

import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.objects.SkungeeVariable.Value;
import net.md_5.bungee.config.Configuration;

public abstract class SkungeeStorage {

	protected final TreeMap<String, Value[]> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	protected final VariableManager variableManager;
	protected final Configuration configuration;
	protected final String variablesFolder;
	protected final Skungee instance;
	private final String[] names;
	
	public SkungeeStorage(String... names) {
		this.instance = Skungee.getInstance();
		this.configuration = instance.getConfig();
		this.variableManager = instance.getVariableManager();
		this.variablesFolder = instance.getDataFolder().getAbsolutePath() + File.separator + "variables" + File.separator;
		this.names = names;
	}
	
	public int getSize() {
		return variables.size();
	}
	
	protected void registerStorage(SkungeeStorage storage) {
		variableManager.registerStorage(storage);
	}
	
	public String[] getNames() {
		return names;
	}
	
	public abstract void remove(Value[] objects, String... index);
	
	public abstract void set(String index, Value[] objects);
	
	public abstract void delete(String... index);
	
	public abstract Value[] get(String index);
	
	/**
	 * @returns true if initialization was successful.
	 */
	protected abstract boolean initialize();
	
	public abstract void shutdown();
	
	/**
	 * When a backup is called to be processed based on the configuration time.
	 */
	protected abstract void backup();

}
