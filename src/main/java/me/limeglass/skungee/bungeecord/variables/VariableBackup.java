package me.limeglass.skungee.bungeecord.variables;

import me.limeglass.skungee.bungeecord.Skungee;

public class VariableBackup implements Runnable {

	private final VariableManager variableManager;
	private final boolean messages;
	
	public VariableBackup(Skungee instance, boolean messages) {
		this.variableManager = instance.getVariableManager();
		this.messages = messages;
	}
	
	@Override
	public void run() {
		if (messages)
			Skungee.consoleMessage("Variables have been saved!");
		variableManager.backup();
	}

}
