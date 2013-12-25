package com.sirolf2009.snapshop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sirolf2009.snapshop.listener.InventoryListener;

public final class SnapShop extends JavaPlugin implements Listener {

	public InventoryListener inventoryListener;
	public Map<String, Double> prices;
	public static Economy economy = null;

	@Override
	public void onEnable() {
		inventoryListener = new InventoryListener(this);
		getServer().getPluginManager().registerEvents(inventoryListener, this);
		prices = new HashMap<String, Double>();
		parseWorth();
		setupEconomy();
	}

	public void parseWorth() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("plugins/Essentials/worth.yml"));

			String line = reader.readLine();
			while (line != null) {
				if(!line.isEmpty() && !line.equals("worth:")) {
					String[] worth = line.split(":");
					String item = worth[0].replaceAll(" ", "");
					String price = worth[1].replaceAll(" ", "");
					prices.put(item, Double.parseDouble(price));
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[] getShopItems(String shopName) {
		File customConfigFile = new File(getDataFolder(), "shops.yml");
		YamlConfiguration customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = this.getResource("shops.yml");
		if (customConfigFile != null && defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
			try {
				customConfig.save(customConfigFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String items = customConfig.getString(shopName);
		if(items != null) {
			return items.split(" ");
		} else {
			return null;
		}
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	@Override
	public void onDisable() {
	}
}
