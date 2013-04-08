package fi.sweetmoon.pexhelper;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.permission.Permission;

public class PEXhelper extends JavaPlugin
{
	private static PEXhelper plugin;
	protected static final Logger logger = Logger.getLogger("PEXhelper");
	public static Permission perms = null;
	public static String[] promGrps  = {
			"Uusi", "Kokenut", "Uskollinen", "Kunnioitettava"
		};
	
	@Override
	public void onEnable() {
		if (!setupPermissions() ) {
			logger.severe(String.format("[%s] - Poistettu käytöstä, koska Vault-pluginia ei pystytty tunnistamaan.", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		plugin = this;
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("pexhelper")) {
			if (args.length < 2) {
				return false;
			}
			
			if (args[0].equalsIgnoreCase("timepromote")) { //Check if promoting
				Player promo = getServer().getPlayer(args[1]);
					if (promo == null)
						return false;
					
					if (!(sender instanceof Player)) {
						if(timePromote(promo)) {
							sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvoa nostettu!", promo.getName()));
							return true;
						}
						return false;
					}
					
					Player pl = (Player) sender;
					
					if (perms.has(pl, "pexhelper.timepromote")) {
						if(timePromote(promo)) {
							sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvoa nostettu!", promo.getName()));
							return true;
						}
						return false;
					} else {
						sender.sendMessage(String.format("%sSinulla ei ole oikeutta tehdä tuota.", ChatColor.RED));
						return true;
					}
					
			} else if (args[0].equalsIgnoreCase("timedemote")) { //Check if demoting
				Player demo = getServer().getPlayer(args[1]);
				if (demo == null)
					return false;
				
				if (!(sender instanceof Player)) {
					if(timeDemote(demo)) {
						sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvoa laskettu!", demo.getName()));
						return true;
					} else {
						return false;
					}
				}
				
				Player pl = (Player) sender;
				
				if (perms.has(pl, "pexhelper.timedemote")) {
					if(timeDemote(demo)) {
						sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvoa laskettu!", demo.getName()));
						return true;
					} else {
						return false;
					}
				} else {
					sender.sendMessage(String.format("%sSinulla ei ole oikeuksia tehdä tuota.", ChatColor.RED));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("timegroupset")) {
				Player setPl = getServer().getPlayer(args[1]);
				if (setPl == null && args[2] == null)
					return false;
				
				if (!(sender instanceof Player)) {
					    try { 
					        Integer.parseInt(args[2]); 
					    } catch(NumberFormatException e) {
							if (timeGroupSetStr(setPl, args[2])) {
								sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvo asetettu arvoon %s!", setPl.getName(), args[2]));
								return true;
							} else {
								return false;
							}
					    }
					    if (timeGroupSetInt(setPl, Integer.parseInt(args[2]))) {
					    	sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvo asetettu arvoon %s!", setPl.getName(), promGrps[Integer.parseInt(args[2])]));
					    	return true;
					    } else {
					    	return false;
					    }
					} else if (perms.has((Player) sender, "pexhelper.timegroupset")) {
					    try { 
					        Integer.parseInt(args[2]); 
					    } catch(NumberFormatException e) {
							if (timeGroupSetStr(setPl, args[2])) {
								sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvo asetettu arvoon %s!", setPl.getName(), args[2]));
								return true;
							} else {
								return false;
							}
					    }
					    if (timeGroupSetInt(setPl, Integer.parseInt(args[2]))) {
					    	sender.sendMessage(String.format("Pelaajan %s \"ontime\" arvo asetettu arvoon %s!", setPl.getName(), promGrps[Integer.parseInt(args[2])]));
					    	return true;
					    } else {
					    	return false;
					    }
					} else {
						sender.sendMessage(String.format("%sSinulla ei ole oikeutta tehdä tuota.", ChatColor.RED));
						return true;
					}
				}
			}
		return false;
	}
	
	private boolean timePromote(Player pl) {
		String primGrp = perms.getPrimaryGroup(pl);
		
		for (int i=0; i < promGrps.length - 1; i++) {
			if (primGrp.split(" ")[0].equals(promGrps[i])) {
				perms.playerRemoveGroup(pl, primGrp);
				perms.playerAddGroup(pl, String.format("%s %s", promGrps[i+1], primGrp.split(" ")[1]));
				return true;
			}
		}
		return false;
	}
	
	private boolean timeDemote(Player pl) {
		String primGrp = perms.getPrimaryGroup(pl);
		
		for (int i = promGrps.length - 1; i>0; i--) {
			if (primGrp.split(" ")[0].equals(promGrps[i])) {
				perms.playerRemoveGroup(pl, primGrp);
				perms.playerAddGroup(pl, String.format("%s %s", promGrps[i-1], primGrp.split(" ")[1]));
				return true;
			}
		}
		return false;
	}
	
	private boolean timeGroupSetInt(Player pl, int grp) {
		if (grp >= 0 && grp <= 3) {
			String primGrp = perms.getPrimaryGroup(pl);
			
			perms.playerRemoveGroup(pl, primGrp);
			perms.playerAddGroup(pl, String.format("%s %s", promGrps[grp], primGrp.split(" ")[1]));
			return true;
		}
		return false;
	}
	
	private boolean timeGroupSetStr(Player pl, String grp) {
		String primGrp = perms.getPrimaryGroup(pl);
		
		for (int i=0; i < promGrps.length - 1; i++) {
			if (grp.equalsIgnoreCase(promGrps[i])) {
				perms.playerRemoveGroup(pl, primGrp);
				perms.playerAddGroup(pl, String.format("%s %s", promGrps[i], primGrp.split(" ")[1]));
				return true;
			}
		}		
		return false;
	}
}