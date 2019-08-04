package net.schlaubi.ultimatediscord;

 
import net.dv8tion.jda.api.JDA;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.schlaubi.ultimatediscord.bungee.Main;
import net.schlaubi.ultimatediscord.util.MySQL;

public class UltimateDiscordAPI {

	public static boolean isVerified(String discordid) {
		return MySQL.userExists(discordid);
	}

	public static boolean isVerified(ProxiedPlayer proxiedPlayer) {
		return MySQL.userExists(proxiedPlayer);
	}

	public static String getUserName(String discordid) {
		return MySQL.getValue(discordid, "uuid");
	}

	public static String getDiscordId(ProxiedPlayer proxiedPlayer) {
		return MySQL.getValue(proxiedPlayer, "discordid");
	}

	public static JDA getBungeeCordJDA() {
		return Main.jda;
	}

}
