package net.schlaubi.ultimatediscord.bungee;

import com.google.common.collect.ImmutableSet;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.managers.GuildController;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;
import net.schlaubi.ultimatediscord.util.MySQL;

import java.util.*;

public class CommandDiscord extends Command implements TabExecutor {

	public static HashMap<String, String> users = new HashMap<>();
	private GuildController guild = new GuildController(Main.jda.getGuilds().get(0));

	private String generateString() {
		String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567980";
		StringBuilder random = new StringBuilder();
		Random rnd = new Random();
		while (random.length() < 5) {
			int index = (int) (rnd.nextFloat() * CHARS.length());
			random.append(CHARS.charAt(index));
		}
		return random.toString();
	}

	public CommandDiscord(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			Configuration cfg = Main.getConfiguration();
			ProxiedPlayer pp = (ProxiedPlayer) sender;
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (pp.hasPermission("discord.reload")) {
						Main.loadConfig();
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.reload").replace("&", "§").replace("%nl", "\n")));
					}
				} else if (args[0].equalsIgnoreCase("verify")) {
					if (users.containsKey(pp.getName())) {
						pp.sendMessage(new TextComponent(cfg.getString("Messages.running").replace("&", "§")
								.replace("%nl", "\n").replace("%code%", users.get(pp.getName()))));
					} else if (MySQL.userExists(pp)) {
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.verified").replace("&", "§").replace("%nl", "\n")));
					} else {
						users.put(pp.getName(), generateString());
						pp.sendMessage(new TextComponent(cfg.getString("Messages.verify").replace("&", "§")
								.replace("%nl", "\n").replace("%code%", users.get(pp.getName()))));
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								if (users.containsKey(pp.getName()))
									users.remove(pp.getName());
							}
						}, 60 * 1000);

					}

				} else if (args[0].equalsIgnoreCase("unlink")) {
					if (!MySQL.userExists(pp)) {
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.notverified").replace("&", "§").replace("%nl", "\n")));
					} else {
						Member member = guild.getGuild().getMemberById(MySQL.getValue(pp, "discordid"));
						guild.removeRolesFromMember(member,
								guild.getGuild().getRoleById(cfg.getLong("Roles.defaultrole"))).queue();
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								cfg.getSection("Roles.group").getKeys().forEach(i -> {
									if (pp.hasPermission("group." + i)) {
										guild.removeSingleRoleFromMember(member,
												guild.getGuild().getRoleById(cfg.getLong("Roles.group." + i))).queue();
									}
								});
							}
						}, 1000);
						MySQL.deleteUser(pp);
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.unlinked").replace("&", "§").replace("%nl", "\n")));
					}
				} else if (args[0].equalsIgnoreCase("update")) {
					if (!MySQL.userExists(pp)) {
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.notverified").replace("&", "§").replace("%nl", "\n")));
					} else {
						Member member = guild.getGuild().getMemberById(MySQL.getValue(pp, "discordid"));
						cfg.getSection("Roles.group").getKeys().forEach(i -> {
							if (pp.hasPermission("group." + i)) {
								guild.addSingleRoleToMember(member,
										guild.getGuild().getRoleById(cfg.getLong("Roles.group." + i))).queue();
							}
						});
						pp.sendMessage(new TextComponent(
								cfg.getString("Messages.updated").replace("&", "§").replace("%nl", "\n")));
					}

				}
			} else {
				pp.sendMessage(
						new TextComponent(cfg.getString("Messages.help").replace("&", "§").replace("%nl", "\n")));
			}

		} else {
			ProxyServer.getInstance().getConsole()
					.sendMessage(new TextComponent("§4§lYou must be a player to run this command"));
		}

	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length > 1 || args.length == 0)
			return ImmutableSet.of();
		String[] subcommands = { "reload", "verify", "unlink", "update" };
		Set<String> matches = new HashSet<>();
		if (args.length > 0) {
			for (String subcommand : subcommands) {
				if (subcommand.startsWith(args[0]))
					matches.add(subcommand);
			}
			return matches;
		}
		return null;
	}
}
