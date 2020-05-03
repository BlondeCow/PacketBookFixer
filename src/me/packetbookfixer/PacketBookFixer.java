package me.packetbookfixer;


import org.bukkit.plugin.java.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.plugin.*;
import com.comphenix.protocol.*;
import java.io.*;
import java.text.SimpleDateFormat;

import com.comphenix.protocol.events.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


import org.bukkit.event.player.*;

public class PacketBookFixer extends JavaPlugin implements Listener, CommandExecutor
{
	
	
	public static PacketBookFixer instance;
    private List<PacketType> packets;
    
    private Cache<String, Integer> ignore = CacheBuilder.newBuilder().concurrencyLevel(2).initialCapacity(20).expireAfterWrite(550, TimeUnit.MILLISECONDS).build();
   
    
    PacketType a = PacketType.Play.Client.BLOCK_PLACE;
    PacketType b = PacketType.Play.Client.WINDOW_CLICK;
    PacketType c = PacketType.Play.Client.FLYING;
    PacketType d = PacketType.Play.Client.HELD_ITEM_SLOT;
    PacketType e = PacketType.Play.Client.POSITION_LOOK;
    PacketType f = PacketType.Play.Client.CUSTOM_PAYLOAD;
    PacketType g = PacketType.Play.Client.SET_CREATIVE_SLOT;
    List<String> isim = new ArrayList<>();
    public PacketBookFixer() {
        this.packets = new ArrayList<PacketType>();
    }
    public void onEnable() {
    	 this.packets.add(a);
         this.packets.add(b);
         this.packets.add(c);
         this.packets.add(d);
         this.packets.add(e);
         this.packets.add(f);
         this.packets.add(g);
         
         
        saveDefaultConfig();
        PacketBookFixer.instance = this;
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        
        Bukkit.getConsoleSender().sendMessage("§7--§4|§7----------------------§4||§7-----------------------§4|§7--");
        Bukkit.getConsoleSender().sendMessage("§dPacketBookFixer §8» §7Active §cnow");
        Bukkit.getConsoleSender().sendMessage("§7--§4|§7----------------------§4||§7-----------------------§4|§7--");
        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this, ListenerPriority.HIGHEST, this.packets, new ListenerOptions[] { ListenerOptions.INTERCEPT_INPUT_BUFFER}) {
            public void onPacketReceiving(final PacketEvent event) {
            	final int remaining = event.getNetworkMarker().getInputBuffer().remaining();
            	int count = plugin.getConfig().getInt("r_packet");
                if ((remaining > count)) {
                    try {
                        throw new IOException();
                    }
                    catch (IOException e) {
                        PacketBookFixer.instance.handleInvalidPacket(event.getPlayer());
                        PacketBookFixer.instance.handleInvalidPacket2(event.getPlayer());
                        event.setCancelled(true);
                    	
                    }
                }
               
            }    
        });
        
        
    }
    @EventHandler
    public void PBF(AsyncPlayerChatEvent event)
    {
      Player player = event.getPlayer();
      String msg = event.getMessage();
      if (msg.equalsIgnoreCase(":pbf"))
      {
        player.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
        player.sendMessage("");
        player.sendMessage("§dPacketBookFixer §emade by §5BlondeCow");
        player.sendMessage("§2Plugin version §6: §e" + instance.getDescription().getVersion()+ " ");
        player.sendMessage("§4Discord §6: §chttps://discord.gg/h3A4xku");
        player.sendMessage("");
        player.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
        event.setCancelled(true);
      }
    }
    public String date(String datef)
    {
     Calendar date = Calendar.getInstance();
     SimpleDateFormat sdf = new SimpleDateFormat(datef);
     return sdf.format(date.getTime());
    }
    public void handleInvalidPacket(final Player player) {
    	PacketBookFixer.instance.getServer().getScheduler().runTask((Plugin)PacketBookFixer.instance, (Runnable)new Runnable() {
            @Override
            public void run() {
                try {                               	                	            			
                    for (final Player all : Bukkit.getOnlinePlayers()) {
                        if (all.hasPermission("packetbookfixer.notify")) {	
                        	all.sendMessage("§dPacketBookFixer §8» §4" + player.getName() +" §7 the named player tried to send a bad book packet to the server. §cIP §7adress: §c " +player.getPlayer().getAddress().getAddress().getHostAddress());    
                        }
                                        
                       }
                    }
                catch (NullPointerException ex) {}
            }
        });
    }
    public void handleInvalidPacket2(final Player player) {	
        try {   
        	forceKick(player);
       	    Bukkit.getConsoleSender().sendMessage("§7--§4|§7----------------------§4||§7-----------------------§4|§7--");
            Bukkit.getConsoleSender().sendMessage("§dPacketBookFixer §8» §7averted an incoming book attack. ");
            Bukkit.getConsoleSender().sendMessage("§7Name§6 : §c "+player.getPlayer().getName());
            Bukkit.getConsoleSender().sendMessage("§7IP §6: §c "+player.getPlayer().getAddress().getAddress().getHostAddress());
            Bukkit.getConsoleSender().sendMessage("§7Time §6: §c "+date("yyyy.MM.dd H:mm:ss"));
            Bukkit.getConsoleSender().sendMessage("§7--§4|§7----------------------§4||§7-----------------------§4|§7--");
        } catch (NullPointerException ex) {}
    }
	  @Override
	    public void onDisable() {
	      ProtocolLibrary.getProtocolManager().removePacketListeners((Plugin)this);
	      Bukkit.getPluginManager().disablePlugin(this);
	    }
	  private void forceKick(Player p) {
	        ignore.put(p.getName().toLowerCase(), 100);
	        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
	        if (connection == null)
	            return;
	        NetworkManager manager = connection.networkManager;
	        if (manager == null)
	            return;
	        Channel ch = manager.channel;
	        if (ch == null)
	            return;
	        ch.close();
	    }
	  
    public static PacketBookFixer getPlugin() {
        return PacketBookFixer.instance;
    }
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	   if(cmd.getName().equalsIgnoreCase("pbf")&& args.length == 0  && sender.hasPermission("packetbokkfixer.notify")) {
		           sender.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
		           sender.sendMessage("");   
		           sender.sendMessage("§8/§dpbf §5reload");
				   sender.sendMessage("§dPacket limit: §8"+getConfig().getString("r_packet"));
				   sender.sendMessage("");  
				   sender.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
				   return true;
	        }
	   if((args.length == 1) && (args[0].equalsIgnoreCase("dev"))) {
		   sender.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
		   sender.sendMessage("");
		   sender.sendMessage("§dPacketBookFixer §emade by §5BlondeCow");
		   sender.sendMessage("§2Plugin version §6: §e" + instance.getDescription().getVersion()+ " ");
		   sender.sendMessage("§4Discord §6: §chttps://discord.gg/h3A4xku");
		   sender.sendMessage("");
		   sender.sendMessage("§7--§4!§7---§dPacketBookFixer§7---§4!§7--");
	        return true;
	   }
		   if((args.length == 1) && (args[0].equalsIgnoreCase("reload") && sender.hasPermission("packetbookfixer.notify"))){
			   reloadConfig();
			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("reload_message")));
		   }
	return true;
	   
   }
}

