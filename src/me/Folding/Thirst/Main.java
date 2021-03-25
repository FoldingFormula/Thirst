package me.Folding.Thirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	Map<UUID,Integer> xpMap = new HashMap<>();

	@Override
	public void onEnable() {
		
		//RESTORING DATA 
		//this.saveDefaultConfig();
		//if(this.getConfig().contains("data")) {
			//this.restoreThirst();
			
			
		//}
		//-------------
		
		getServer().getPluginManager().registerEvents(this, this); 
		
		
		
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            //@Override 
            public void run() {   
        		
        		if(!xpMap.isEmpty()) { //If xpmap isnt empty, reduce the values of xp in it
        		
	            	for(Map.Entry<UUID,Integer> entry : xpMap.entrySet()) {
	            		
	            		if(entry.getValue() > 0) {
	            			entry.setValue(entry.getValue() - 1); //subtract experience from hashmap if thirst is more than 0
	            			Bukkit.getPlayer(entry.getKey()).setLevel(entry.getValue()); //Set experience from hashmap once the value decreases
	            		}
	            		
	            		
	            		
	            		if(entry.getValue() <= 0) { //if thirst is less than 0 and health is more than 1, take damage
	            			
	            			if(Bukkit.getPlayer(entry.getKey()).getHealth() > 1.0) {
	            				Bukkit.getPlayer(entry.getKey()).damage(1.0);
	            			}
	            			
	            			
	            			
	            		}
	            		
            		
	            	}
            		
            		
            		
            	}           	
            	
            	
            }
           
        }, 0L, 300L); //5 ticks * 60 sec * min
        
        
        
        
	}
	
	
	
	@Override
	public void onDisable() {
		if(!xpMap.isEmpty()) {
			this.saveThirstAll();
		}
	}
	
	
	
	public void saveThirstAll(){
		for(Map.Entry<UUID, Integer> entry: xpMap.entrySet()) {
			this.getConfig().set("data." + entry.getKey(), entry.getValue());
		}
		
		this.saveConfig();
	}
	
	
	
	
	
	
	
	public void restoreThirstAll() { //* DEPRECATED *
		this.getConfig().getConfigurationSection("data.").getKeys(false).forEach(key ->{
			xpMap.put(UUID.fromString(key), this.getConfig().getInt("data." + key));
		});
	}
	
	public void restoreThirst(UUID player) { //get player's xp from config and put it on hashmap
		Integer xp = this.getConfig().getConfigurationSection("data.").getInt(player.toString());
		
		xpMap.put(player, xp);
	}
	
	
	public void saveThirst(UUID player) {
		this.getConfig().set("data." + player, xpMap.get(player));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if(label.equalsIgnoreCase("water")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("No console!");
				return true;
			}
			
		Player player = (Player) sender;
		
		if(player.getInventory().firstEmpty() == -1) {
			Location loc = player.getLocation();
			World world = player.getWorld();
			world.dropItemNaturally(loc, getwaterBottle());
			return true;
		}
			
		player.getInventory().addItem(getwaterBottle());
		player.sendMessage(ChatColor.GOLD + "Water!");
		return true;
			
			
		}
		
		
		return false;       
	}

	
	
	public ItemStack getwaterBottle() {
		
		ItemStack waterBottle = new ItemStack(Material.POTION);
		ItemMeta meta = waterBottle.getItemMeta();
		
		PotionMeta pmeta = (PotionMeta) meta;
		PotionData pdata = new PotionData(PotionType.WATER);
		pmeta.setBasePotionData(pdata);
		
		//meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Water Bottle");
		meta.setDisplayName("Water");
		waterBottle.setItemMeta(meta); // Finalize everything
		
		
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7(Right Click) &a&oQuenches thirst!"));
		
		
		meta.setLore(lore); // create the lore using the lore array
		waterBottle.setItemMeta(meta);
		
		
		
		
		
		return waterBottle;
		
		
	}
	
	
	
	
	@EventHandler() //giving thirst if new and setting thirst after joining
	public void onJoin(PlayerJoinEvent event) {
		
		if(this.getConfig().contains("data." + event.getPlayer().getUniqueId().toString())) {
			restoreThirst(event.getPlayer().getUniqueId()); //If player is in config, restore data
			event.getPlayer().setLevel(xpMap.get(event.getPlayer().getUniqueId()));
		}
		
		event.getPlayer().sendMessage("Player Join event registered!");
		
		if(!(xpMap.containsKey(event.getPlayer().getUniqueId()))) {
			xpMap.put(event.getPlayer().getUniqueId(), 20); //If player is new, give thirst of 20
			event.getPlayer().sendMessage("You are well hydrated!");
			event.getPlayer().setLevel(xpMap.get(event.getPlayer().getUniqueId())); 
		}
		
		
	
	}
	
	
	public void onQuit(PlayerQuitEvent event) {
		
		saveThirst(event.getPlayer().getUniqueId());
		xpMap.remove(event.getPlayer().getUniqueId());
		
	}
	
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
	    Player player = event.getPlayer();
	    event.getPlayer().sendMessage("Player Interact event registered!!");
	 
    	if((player.getInventory()).getItemInMainHand() != null)
    		if((player.getInventory()).getItemInMainHand().getItemMeta().getDisplayName().contains("Water"))
    			if(player.getInventory().getItemInMainHand().getItemMeta().hasLore()) {
    				//player.setLevel(Math.min(20, player.getLevel() + 10)); dont set level directly, set the hashmap
    				
    				xpMap.put(player.getUniqueId(), Math.min(20, player.getLevel() + 10 )); //Set new level in xpmap
    				player.setLevel(Math.min(20, xpMap.get(player.getUniqueId()))); //update the level
    			}
    				
    				
    				
	    
	    }
	
	
	
}
