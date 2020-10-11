package com.oposproject;

public class CommandsChoice {
	private Shell.commandsChoice value;
	
	public CommandsChoice(Shell.commandsChoice value) {
		this.value = value;
	}

	public void Set(Shell.commandsChoice value) {
		this.value = value;
	}

	public Shell.commandsChoice Get() {
		return this.value;
	}
}