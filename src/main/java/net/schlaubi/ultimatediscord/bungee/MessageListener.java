package net.schlaubi.ultimatediscord.bungee;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.schlaubi.ultimatediscord.util.MySQL;

public class MessageListener extends ListenerAdapter {

	private HashMap<String, String> users = CommandDiscord.users;

	private String getUser(String code) {
		for (String key : users.keySet()) {
			String value = users.get(key);
			if (value.equalsIgnoreCase(code)) {
				return key;
			}
		}
		return null;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Configuration cfg = Main.getConfiguration();
		if (!event.isFromType(ChannelType.PRIVATE)) {
			JDA jda = event.getJDA();
			String message = event.getMessage().getContentDisplay();
			String[] args = message.split(" ");
			if (message.startsWith("!roles")) {
				if (!event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
					return;
				}

				StringBuilder sb = new StringBuilder();
				for (Role r : jda.getGuilds().get(0).getRoles()) {
					sb.append("[R: " + r.getName() + "(" + r.getId() + ")");
				}
				event.getChannel().sendMessage(sb.toString()).queue();
			} else if (message.startsWith("!verify")) {
				if (users.containsValue(args[1])) {
					ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(getUser(args[1]));
					Guild guild = event.getGuild();
					Member member = jda.getGuilds().get(0).getMember(event.getAuthor());
					guild.addRoleToMember(member, guild.getRoleById(cfg.getLong("Roles.defaultrole"))).queue();
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							cfg.getSection("Roles.group").getKeys().forEach(i -> {
								if (pp.hasPermission("group." + i)) {
									guild.addRoleToMember(member, guild.getRoleById(cfg.getLong("Roles.group." + i)))
											.queue();
								}
							});
						}
					}, 1000);
					MySQL.createUser(pp, event.getAuthor().getId());
					event.getChannel()
							.sendMessage(cfg.getString("Messages.success")
									.replace("%discord%", event.getAuthor().getAsMention())
									.replace("%minecraft%", pp.getName()))
							.complete();
					users.remove(pp.getName());
					event.getMessage().delete().queue();
				} else {
					event.getChannel().sendMessage(cfg.getString("Messages.invalidcode")).queue();
					event.getMessage().delete().queue();
				}
			}
		}
	}
}
