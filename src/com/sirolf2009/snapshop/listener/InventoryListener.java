package com.sirolf2009.snapshop.listener;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sirolf2009.snapshop.SnapShop;

public final class InventoryListener implements Listener {

	public SnapShop snapShop;

	public InventoryListener(SnapShop snapShop) {
		this.snapShop = snapShop;
	}

	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent e){
		if (e.getInventory().getHolder() instanceof Chest){
			if(e.getInventory().getName().startsWith("SnapCraftShop")) {
				List<String> shopName = new ArrayList<String>();
				if(e.getInventory().getName().split(" ").length > 1) {
					shopName.add(e.getInventory().getName().split(" ")[1]);
				} else {
					shopName.add("default");
				}

				ItemStack buy = new ItemStack(Material.WOOL, 1);
				ItemMeta meta = buy.getItemMeta();
				meta.setDisplayName("Buy Items");
				meta.setLore(shopName);
				buy.setItemMeta(meta);
				e.getInventory().setItem(12, buy);

				ItemStack sell = new ItemStack(Material.WOOL, 1, (short) 1);
				meta = sell.getItemMeta();
				meta.setDisplayName("Sell Items");
				meta.setLore(shopName);
				sell.setItemMeta(meta);
				e.getInventory().setItem(14, sell);
			}
		}
	}

	@EventHandler
	public void inventoryClick(final InventoryClickEvent e) {
		if(e == null || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
			return;
		}
		if(e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals("Sell Items")) {
			final Inventory inventory = Bukkit.createInventory(e.getWhoClicked(), 9, "Sell Items");
			Bukkit.getScheduler().runTask(snapShop, new Runnable() {
				public void run() {
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().openInventory(inventory);
				}
			});
			e.setCancelled(true);
		} else if(e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().getDisplayName().equals("Buy Items")) {
			final Inventory inventory = Bukkit.createInventory(e.getWhoClicked(), 9, "Buy Items");
			
			String[] items = snapShop.getShopItems(e.getCurrentItem().getItemMeta().getLore().get(0));
			if(items == null) {
				items = snapShop.getShopItems("Default");
			}
			for(int i = 0; i < items.length; i++) {
				ItemStack stack = getItemStackFromName(items[i]);
				ItemMeta meta = stack.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.add("Price: "+snapShop.prices.get(items[i]));
				meta.setLore(lore);
				stack.setItemMeta(meta);
				inventory.setItem(i, stack);
			}

			Bukkit.getScheduler().runTask(snapShop, new Runnable() {
				public void run() {
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().openInventory(inventory);
				}
			});

			e.setCancelled(true);
		} else if(e.getInventory().getName().equals("Buy Items")) {
			if(e.getSlot() < 9) {
				EconomyResponse purchase = SnapShop.economy.withdrawPlayer(e.getWhoClicked().getName(), snapShop.prices.get(e.getCurrentItem().getType().name().replace("_", "").toLowerCase()));
				if(purchase.transactionSuccess()) {
					ItemStack oldStack = new ItemStack(e.getCurrentItem());
					oldStack.setItemMeta(null);
					e.getWhoClicked().setItemOnCursor(oldStack);
				}
			}
			e.setCancelled(true);
		} else if(e.getInventory().getName().equals("Sell Items")) {
			if(e.getCurrentItem().getType() != Material.AIR) {
				EconomyResponse purchase = SnapShop.economy.depositPlayer(e.getWhoClicked().getName(), snapShop.prices.get(e.getCurrentItem().getType().name().replace("_", "").toLowerCase())*e.getCurrentItem().getAmount());
				if(!purchase.transactionSuccess()) {
					e.setCancelled(true);
				} else {
					e.setCurrentItem(new ItemStack(Material.AIR));
				}
			}
		} 
	}

	public ItemStack getItemStackFromName(String item) {
		if(Material.matchMaterial(item) != null) {
			return new ItemStack(Material.matchMaterial(item));
		} else {
			for(Material mtl : Material.values()) {
				if(mtl.name().replaceAll("_", "").equals(item.toUpperCase())) {
					return new ItemStack(mtl);
				}
			}
			snapShop.getLogger().severe("Could not add item "+item);
			return null;
		}
	}
}
