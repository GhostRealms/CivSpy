package com.programmerdan.minecraft.civspy;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.HandlerList;


public class CivSpy extends JavaPlugin implements Listener {

	private Config config;
	private Database db;

	private HashMap<UUID, CivSpyPlayer> spydata;

	private BukkitTask savedata;

	private CivSpySaver saver;

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Initializing CivSpy config");
		this.config = new Config(getLogger()).setupConfig(this);
		getLogger().log(Level.INFO, "Initializing CivSpy database");
		this.db = config.parseDatabase();
		try {
			if (this.db == null || !this.db.available()){
				getLogger().log(Level.SEVERE, "Failed to acquire database, skipping listeners");
				return;
			}
			getLogger().log(Level.INFO, "Preparing CivSpy datastructures");
			this.spydata = new ConcurrentHashMap<UUID, CivSpyPlayer>();

			getLogger().log(Level.INFO, "Registering CivSpy listeners");
			this.getServer().getPluginManager().registerEvents(this, this);
			
			getLogger().log(Level.INFO, "Registering CivSpy saver");
			this.saver = new CivSpySaver(db);
			this.savedata = this.getServer().getScheduler().runTaskTimerAsynchronously(
					this, this.saver, this.config.getInterval(), this.config.getInterval());
		} catch (SQLException se) {
			getLogger().log(Level.SEVERE, "Failed to acquire database, skipping listeners");
		}
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Deregistering CivSpy listeners");
		HandlerList.unregisterAll((Plugin) this);
		getLogger().log(Level.INFO, "Forcing saver to run and deactivating");
		this.saver.saveAll();
		this.saver = null;
		this.cancelTasks(this);
		getLogger().log(Level.INFO, "Clearing CivSpy datastructures");
		this.spydata.removeAll();
		this.spydata = null;
		getLogger().log(Level.INFO, "Closing CivSpy database");
		if (this.db != null) this.db.close();
	}

	/* Listeners */

	@EventHandler
	public void onRegularBreak(BlockBreakEvent event) {
		if (event.getPlayer() == null)

}