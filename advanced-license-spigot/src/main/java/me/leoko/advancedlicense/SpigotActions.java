package me.leoko.advancedlicense;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SpigotActions implements LicenseActions {

	private final Plugin plugin;

	public SpigotActions(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getProductName() {
		return plugin.getName();
	}

	@Override
	public void doLicenseInvalidAction() {
		Bukkit.getScheduler().cancelTasks(plugin);
		Bukkit.getPluginManager().disablePlugin(plugin);
	}
}
