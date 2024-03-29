package fi.sweetmoon.pexhelper;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import mondocommand.MondoCommand;
import mondocommand.CallInfo;
import mondocommand.dynamic.Sub;
import net.milkbowl.vault.permission.Permission;

public class PEXhelper extends JavaPlugin
{
	protected static final Logger logger = Logger.getLogger("PEXhelper");
	public static Permission perms = null;
	public static final String[] PROMOTION_GROUPS  = {
			"Uusi", "Kokenut", "Uskollinen", "Kunnioitettava"
		};
	public static final String[] PRIMARY_GROUPS = {
		"pelaaja", "lahjoittaja", "moderaattori", "operaattori", "admin"
	};
	
	@Override
	public void onEnable() {
		
		// Setup Vault perms, if fails, disable this plugin
		if (!setupPermissions() ) {
			logger.severe(String.format("[%s] - Poistettu käytöstä, koska Vault-pluginia ei pystytty tunnistamaan.", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// We are using MondoCommand for subcommand handling: registering it
		MondoCommand base = new MondoCommand();
		base.autoRegisterFrom(this);
		getCommand("pexhelper").setExecutor(base);
		
	}
	
	// /pexhelper timepromote -command
	@Sub(description="Nostaa ontime-rank.", minArgs=1, usage="<pelaaja>", permission="pexhelper.timepromote")
	public void timepromote(CallInfo call) {
		String player = call.getArg(0);
		// Need to cast null to object World, null is an ok arg according to javadocs
		String primaryGroup = perms.getPrimaryGroup((World) null, player);
		
		// Increment the group of the player (iterates through possible groups and then adds)
		for (int i=0; i < PROMOTION_GROUPS.length - 1; i++) {
			if (primaryGroup.split(" ")[0].equals(PROMOTION_GROUPS[i])) {
				perms.playerRemoveGroup((World) null, player, primaryGroup);
				perms.playerAddGroup((World) null, player, String.format("%s %s", PROMOTION_GROUPS[i+1], primaryGroup.split(" ")[1]));
				call.reply("Pelaajan %s ontime-rank asetettu arvoon %s!", player, PROMOTION_GROUPS[i+1]);
				return;
			}
		}
		call.reply("{RED}Pelaajaa %s ei ole olemassa tai hän on jo maksimiarvossa.", player);
	}
	
	// /pexhelper timedemote -command, mostly same stuff as the timepromote one
	@Sub(description="Laskee ontime-rank.", minArgs=1, usage="<pelaaja>", permission="pexhelper.timedemote")
	public void timedemote(CallInfo call){
		String player = call.getArg(0);
		String primaryGroup = perms.getPrimaryGroup((World) null, player);
		
		// Decrement the group of the player -- iterates the other way compared to timepromote
		for (int i = PROMOTION_GROUPS.length - 1; i>0; i--) {
			if (primaryGroup.split(" ")[0].equals(PROMOTION_GROUPS[i])) {
				perms.playerRemoveGroup((World) null, player, primaryGroup);
				perms.playerAddGroup((World) null, player, String.format("%s %s", PROMOTION_GROUPS[i-1], primaryGroup.split(" ")[1]));
				call.reply("Pelaajan %s ontime-rank asetettu arvoon %s!", player, PROMOTION_GROUPS[i-1]);
				return;
			}
		}
		call.reply("{RED}Pelaajaa %s ei ole olemassa tai hän on jo minimiarvossa.", player);
	}
	
	// /pexhelper timegroupset -command
	@Sub(description="Asettaa ontime-rank.", minArgs=2, usage="<pelaaja> <rank (0-3)>", permission="pexhelper.timegroupset")
	public void timegroupset(CallInfo call) {
		String player = call.getArg(0);
		String rank = call.getArg(1);
		String primaryGroup = perms.getPrimaryGroup((World) null, player);
		
		// Try if the string is actually a number, catch the exception if it can't be converted
	    try { 
	        Integer.parseInt(rank); 
	    } catch(NumberFormatException e) {
	    	
	    	// If it's string, we get exception and catch it. We treat the arg as string and iterate through groups
			for (int i=0; i < PROMOTION_GROUPS.length - 1; i++) {
				if (rank.equalsIgnoreCase(PROMOTION_GROUPS[i])) {
					perms.playerRemoveGroup((World) null, player, primaryGroup);
					perms.playerAddGroup((World) null, player, String.format("%s %s", PROMOTION_GROUPS[i], primaryGroup.split(" ")[1]));
					call.reply("Pelaajan %s ontime-rank asetettu arvoon %s!", player, PROMOTION_GROUPS[i]);
					return;
				}
			}
			call.reply("{RED}Pelaajalle %s ei voitu asettaa ontime-rankkia, koska annoit virheellisen arvon.", player);
			return;
	    }
	    
	    // If it's an int
	    int rankInt = Integer.parseInt(rank);
		if (rankInt >= 0 && rankInt <= 3) { // If the number represents a group
			String primGrp = perms.getPrimaryGroup((World) null, player);
			perms.playerRemoveGroup((World) null, player, primaryGroup);
			perms.playerAddGroup((World) null, player, String.format("%s %s", PROMOTION_GROUPS[rankInt], primGrp.split(" ")[1]));
			call.reply("Pelaajan %s ontime-rank asetettu arvoon %s!", player, PROMOTION_GROUPS[rankInt]);
			return;
		}
		call.reply("{RED}Pelaajalle %s ei voitu asettaa ontime-rankkia, koska annoit virheellisen arvon.", player);
	}
	
	// /pexhelper rankpromote -command
	@Sub(description="Nostaa rankkia.", minArgs=1, usage="<pelaaja>", permission="pexhelper.rankpromote")
	public void rankpromote(CallInfo call) {
		String player = call.getArg(0);
		String primaryGroup = perms.getPrimaryGroup((World) null, player);
		
		// Same stuff as the ontime ones, just changing the latter group instead of the first one
		for (int i=0; i < PRIMARY_GROUPS.length - 1; i++) {
			if (primaryGroup.split(" ")[1].equals(PRIMARY_GROUPS[i])) {
				perms.playerRemoveGroup((World) null, player, primaryGroup);
				perms.playerAddGroup((World) null, player, String.format("%s %s", primaryGroup.split(" ")[0], PRIMARY_GROUPS[i+1]));
				call.reply("Pelaajan %s rank asetettu arvoon %s!", player, PRIMARY_GROUPS[i+1]);
				return;
			}
		}
		call.reply("{RED}Pelaajaa %s ei ole olemassa tai hän on jo maksimiarvossa.", player);
	}
	
	// /pexhelper rankdemote -command
	@Sub(description="Laskee rankkia.", minArgs=1, usage="<pelaaja>", permission="pexhelper.rankdemote")
	public void rankdemote(CallInfo call) {
		String player = call.getArg(0);
		String primaryGroup = perms.getPrimaryGroup((World) null, player);
		
		for (int i = PRIMARY_GROUPS.length - 1; i>0; i--) {
			if (primaryGroup.split(" ")[1].equals(PRIMARY_GROUPS[i])) {
				perms.playerRemoveGroup((World) null, player, primaryGroup);
				perms.playerAddGroup((World) null, player, String.format("%s %s", primaryGroup.split(" ")[0], PRIMARY_GROUPS[i-1]));
				call.reply("Pelaajan %s ontime-rank asetettu arvoon %s!", player, PRIMARY_GROUPS[i-1]);
				return;
			}
		}
		call.reply("{RED}Pelaajaa %s ei ole olemassa tai hän on jo minimiarvossa.", player);
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}
}