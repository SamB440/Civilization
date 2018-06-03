package com.SamB440.Civilization.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.SamB440.Civilization.Civilization;
import com.SamB440.Civilization.API.data.CivPlayer;
import com.SamB440.Civilization.API.data.Settlement;
import com.SamB440.Civilization.API.data.SettlementClaim;
import com.SamB440.Civilization.API.data.Technology;
import com.SamB440.Civilization.API.data.TechnologyType;
import com.SamB440.Civilization.utils.TechTree;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SettlementCommand implements CommandExecutor {
	
	Civilization plugin;
	HashMap<Player, Integer> cancel = new HashMap<Player, Integer>();

	public SettlementCommand(Civilization plugin)
	{
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(sender instanceof Player)
		{
			Player player = (Player) sender;
			CivPlayer civ = new CivPlayer(plugin, player);
			switch(args.length)
			{
				case 2:
					
					switch(args[0].toLowerCase())
					{
						case "create":
							if(civ.getSettlement() == null)
							{
								if(args[1].length() <= 16)
								{
									Settlement settlement = new Settlement(plugin, args[1], player);
									if(!settlement.exists())
									{
										SettlementClaim sc = new SettlementClaim(plugin, player.getLocation().getChunk(), settlement);
										if(sc.claim())
										{	 
											player.sendMessage(ChatColor.GREEN + "Settlement created! The surrounding you are standing in has been claimed.");
											settlement.create();
											civ.setSettlement(settlement);
										} else player.sendMessage(ChatColor.RED + "This area has already been claimed by " + sc.getOwner().getName() + "!");
									} else player.sendMessage(ChatColor.RED + "This settlement already exists! Try coming up with a new name.");
								} else player.sendMessage(ChatColor.RED + "Your settlement name cannot be longer than 16 characters.");
							} else player.sendMessage(ChatColor.RED + "You are already in a settlement!");
							
							break;
						
						case "research":
							if(civ.getSettlement() != null)
							{
								if(EnumUtils.isValidEnum(TechnologyType.class, args[1].toUpperCase()))
								{
									switch(args[1].toLowerCase())
									{
										case "writing":
											AdvancementProgress pottery = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/pottery")));
											if(pottery.isDone())
											{
												Technology tech = new Technology(plugin, civ.getSettlement(), player, 3, 200, TechnologyType.WRITING);
												tech.startResearch();
											} else player.sendMessage(ChatColor.RED + "You need to research Pottery first!");
											
											break;
											
										case "wheel":
											AdvancementProgress mining = player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(plugin, "tech/mining")));
											if(mining.isDone())
											{
												Technology tech = new Technology(plugin, civ.getSettlement(), player, 3, 200, TechnologyType.WHEEL);
												tech.startResearch();
											} else player.sendMessage(ChatColor.RED + "You need to research Mining first!");
											
											break;
											
										default:
											Technology tech = new Technology(plugin, civ.getSettlement(), player, 3, 200, TechnologyType.valueOf(args[1].toUpperCase()));
											tech.startResearch();
											break;
									}
								} else {
									player.sendMessage(ChatColor.RED + "That is not a valid technology! Choose from one of the following: ");
									player.sendMessage(ChatColor.GOLD + " List of all technologies:");
									for(TechnologyType tech : TechnologyType.values())
									{
										player.sendMessage(ChatColor.GREEN + " - " + StringUtils.capitalize(tech.toString().toLowerCase()));
									}
									
									player.sendMessage(ChatColor.GRAY + "" + ChatColor.UNDERLINE + "Note");
									player.sendMessage("Make sure to include underscores (_).");
								}
							} else player.sendMessage(ChatColor.RED + "You need to be in a settlement to research technologies!");
							
							break;
						
						case "invite":
							if(civ.getSettlement() != null)
							{
								Player target = Bukkit.getPlayer(args[1]);
								if(target != null && new CivPlayer(plugin, target).getSettlement() == null)
								{
									target.sendMessage(" ");
									target.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + player.getName() + " has invited you to join the Settlement of " + civ.getSettlement().getName() + "!");
									target.sendMessage(" ");
									TextComponent message = new TextComponent(ChatColor.YELLOW + " > " + ChatColor.GREEN + "Click here to accept the invitation.");
									message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sm accept " + civ.getSettlement().getName()));
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept the invitation!" ).create()));
									
									target.spigot().sendMessage(message);
									target.sendMessage(" ");
									
									player.sendMessage(ChatColor.GREEN + "The player has been invited!");
								} else player.sendMessage(ChatColor.RED + "That player is not online, or they are already in a settlement!");
							} else player.sendMessage(ChatColor.RED + "You are not in a settlement yet!");
							
							break;
							
						case "accept":
							if(civ.getSettlement() == null)
							{
								Settlement settlement = new Settlement(plugin, args[1]);
								if(settlement.exists())
								{
									player.sendMessage(ChatColor.GREEN + "You have joined the Settlement of " + settlement.getName() + "!");
									TechTree tech = new TechTree(plugin);
									tech.grantTech("root", player);
									
									for(OfflinePlayer op : settlement.getMembers())
									{
										if(op.isOnline())
										{
											Player member = Bukkit.getPlayer(op.getUniqueId());
											member.sendMessage(ChatColor.GREEN + player.getName() + " has joined your settlement.");
										}
									}
									civ.setSettlement(settlement);
								} else player.sendMessage(ChatColor.RED + "That settlement does not exist!");
							} else player.sendMessage(ChatColor.RED + "You are already in a settlement!");
							
							break;
							
						case "exile":
							Settlement settlement = civ.getSettlement();
							if(settlement != null)
							{
								if(settlement.isKing(player))
								{
									Player target = Bukkit.getPlayer(args[1]);
									if(target != player)
									{
										if(target != null)
										{
											player.sendMessage(ChatColor.RED + "Player has been exiled!");
											target.sendMessage(ChatColor.RED + "You have been exiled from the settlement of " + settlement.getName() + "!");
											target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
											settlement.exile(target);
										} else {
											settlement.exile(Bukkit.getOfflinePlayer(args[1]));
											player.sendMessage(ChatColor.RED + "Player has been exiled!");
										}
									} else player.sendMessage(ChatColor.RED + "You can't exile yourself!");
								} else player.sendMessage(ChatColor.RED + "You must be king of the settlement to do this!");
							} else player.sendMessage(ChatColor.RED + "You are not in a settlement yet!");
					}
					
				break;
					
				case 1:
					
					if(civ.getSettlement() != null)
					{
						switch(args[0].toLowerCase()) 
						{
							case "claim":
								SettlementClaim sc = new SettlementClaim(plugin, player.getLocation().getChunk(), civ.getSettlement());
								if(sc.claim()) 
								{
									player.sendMessage(ChatColor.GREEN + "Area claimed!");
								} else player.sendMessage(ChatColor.RED + "This area has already been claimed by " + sc.getOwner().getName() + "!");
								
								break;
								
							case "info":
								sendInfo(player, civ.getSettlement());
								break;
								
							case "help":
								sendHelp(player);
								break;
							case "disband":
								Settlement settlement = new Settlement(plugin, civ.getSettlement().getName(), player);
								
								if(settlement.isKing(player))
								{
									
									for(SettlementClaim claims : settlement.getClaims(player.getWorld()))
									{
										claims.remove();
									}
									
									settlement.delete();
								} else player.sendMessage(ChatColor.RED + "You must be king of the settlement to do this!");
								
								break;
								
							case "view":
								
								SettlementClaim news = new SettlementClaim(plugin, player.getLocation().getChunk());
								if(news.getOwner() != null)
								{
									if(news.getOwner().getName().equals(civ.getSettlement().getName()))
									{
										if(!cancel.containsKey(player))
										{
											final Location back = player.getLocation();
											Chunk chunk = player.getLocation().getChunk();
								            
								            for(Player pl : Bukkit.getOnlinePlayers())
								            {
								            	player.hidePlayer(plugin, pl);
								            }
								            
								            final GameMode gm = player.getGameMode();
								            
								            player.setGameMode(GameMode.ADVENTURE);
								            
								            Location center = new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4).add(7, 0, 7);
								            center.setY(center.getWorld().getHighestBlockYAt(center) + 23);
								            center.setPitch(90);
								            player.teleport(center);
								            
								            player.setInvulnerable(true);
								            player.setGravity(false);
								            player.setAllowFlight(true);
								            player.setFlying(true);
								            
								            civ.setViewing(true, back);
								            
								            List<Entity> holograms = new ArrayList<Entity>();
								            for(Chunk chunks : getChunksAroundPlayer(player))
							            	{
							            		Location loc = new Location(chunks.getWorld(), chunks.getX() << 4, 64, chunks.getZ() << 4).add(7, 0, 7);
							            		loc.setY(center.getY());
							            		
							            		for(Entity entities : chunks.getEntities())
							            		{
							            			if(entities instanceof Animals)
							            			{
							            				String addons = "";
							            				TechTree tech = new TechTree(plugin);
							            				if(!tech.hasTech("animalhusbandry", player)) addons = ChatColor.RED + " Requires " + ChatColor.UNDERLINE + "Animal Husbandry";
							            				ArmorStand as = spawnHologram(StringUtils.capitalize(entities.getType().toString().toLowerCase()).replace("_", " ") + addons, loc);
							            				loc.setY(loc.getY() - 0.3);
							            				
									            		holograms.add(as);
									            		plugin.getRemoveOnDisable().add(as);
							            			}
							            		}
							            	}
								            
								            final int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
								            	
								            	if(player.isOnline())
								            	{
									            	if(player.isSneaking())
									            	{
									            	
										            	player.setInvulnerable(false);
											            player.setGravity(true);
											            player.setAllowFlight(false);
											            player.setFlying(false);
											            
											            player.setGameMode(gm);
											            
											            player.teleport(back);
											            
											            for(Entity hologram : holograms)
											            {
											            	hologram.remove();
											            	plugin.getRemoveOnDisable().remove(hologram);
											            }

											            civ.setViewing(false, back);
											            Bukkit.getScheduler().cancelTask(cancel.get(player));
											            cancel.remove(player);
									            		
									            	} else {
										            	player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Hold shift to exit"));
										            	if(player.getLocation().getY() > center.getY() || player.getLocation().getY() < center.getY()) 
										            	{
										            		Location prevent = center;
										            		prevent.setX(player.getLocation().getX());
										            		prevent.setZ(player.getLocation().getZ());
										            		player.teleport(center);
										            	}
										            	
										            	SettlementClaim current = new SettlementClaim(plugin, player.getLocation().getChunk());
										            	if(player.getLocation().distance(center) > 24)
										            	{
										            		player.teleport(center);
										            	} else {
											            	if(current.getOwner() == null)
											            	{
											            		player.teleport(center);
											            	} else if(!current.getOwner().getName().equals(civ.getSettlement().getName())) player.teleport(center);
								            			}
									            	}
								            	} else {
								            		civ.setViewing(false, back);
								            		Bukkit.getScheduler().cancelTask(cancel.get(player));
										            cancel.remove(player);
								            	}
								            }, 0, 20);
								            
								            cancel.put(player, id);
										} else player.sendMessage(ChatColor.RED + "You are already viewing!");
									} else player.sendMessage(ChatColor.RED + "You need to be in your own land to do that.");
								} else player.sendMessage(ChatColor.RED + "You need to be in your own land to do that.");
					            
					            break;
								
							default:
								player.sendMessage(ChatColor.RED + "Invalid arguments supplied. Try " + ChatColor.UNDERLINE + "/sm help" + ChatColor.RED + ".");
								break;
						}
					} else player.sendMessage(ChatColor.RED + "You are not in a settlement yet!");
					
				break;
				
				case 0:
					
					if(civ.getSettlement() != null)
					{
						sendInfo(player, civ.getSettlement());
					} else player.sendMessage(ChatColor.RED + "You are not in a settlement yet!");
					
					break;
				
				default:
					player.sendMessage(ChatColor.RED + "Invalid arguments supplied. Try " + ChatColor.UNDERLINE + "/sm help" + ChatColor.RED + ".");
					break;
			}
		} else sender.sendMessage("You must be a player to run this command.");
		
		return true;
	}
	
	public void sendInfo(Player player, Settlement settlement)
	{
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "The Settlement of " + settlement.getName());
		player.sendMessage(" ");
		player.sendMessage(" " + ChatColor.GREEN + "King: " + ChatColor.LIGHT_PURPLE + settlement.getOwner().getName());
		player.sendMessage(" " + ChatColor.GREEN + "Level: " + ChatColor.LIGHT_PURPLE + settlement.getLevel());
		List<String> members = new ArrayList<String>();
		for(OfflinePlayer op : settlement.getMembers())
		{
			if(op != settlement.getOwner()) members.add(op.getName());
		}
		
		player.sendMessage(" " + ChatColor.GREEN + "Settlers " + ChatColor.AQUA + "[" + members.size() + "]" + ChatColor.GREEN + ": " + ChatColor.LIGHT_PURPLE + Arrays.asList(members).toString().replace("[", "").replace("]", ""));
	}
	
	public void sendHelp(Player player)
	{
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Settlement Commands:");
		player.sendMessage(" ");
		player.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GREEN + "Claim:");
		player.sendMessage(ChatColor.WHITE + "    Claim the area you are in.");
		player.sendMessage(ChatColor.WHITE + "    Costs 4 resources.");
		player.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GREEN + "Info:");
		player.sendMessage(ChatColor.WHITE + "    Information about your settlement.");
		player.sendMessage(ChatColor.WHITE + "    You can also use /settlement or /sm.");
		player.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GREEN + "Create [name]:");
		player.sendMessage(ChatColor.WHITE + "    Create a new settlement.");
		player.sendMessage(ChatColor.WHITE + "    A name must be provided.");
	}
	
	public ArmorStand spawnHologram(String name, Location loc)
	{
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.setGravity(false);
		as.setCanPickupItems(false);
		as.setCustomName(name);
		as.setCustomNameVisible(true);
		as.setVisible(false);
		
		return as;
	}
	
	public List<Chunk> getChunksAroundPlayer(Player player) 
	{
        int[] offset = {-1,0,1};

        World world = player.getWorld();
        int ox = player.getLocation().getChunk().getX();
        int oz = player.getLocation().getChunk().getZ();

        List<Chunk> chunks = new ArrayList<Chunk>();
        for(int x : offset) 
        {
            for(int z : offset)
            {
                Chunk chunk = world.getChunkAt(ox + x, oz + z);
                chunks.add(chunk);
            }
        } return chunks;
    }
}
