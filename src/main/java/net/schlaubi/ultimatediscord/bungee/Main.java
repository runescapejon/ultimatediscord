package net.schlaubi.ultimatediscord.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import com.google.common.io.ByteStreams;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.schlaubi.ultimatediscord.util.MySQL;

public class Main extends Plugin {

	private static Configuration configuration;
	public static JDA jda;
	private static Main instance;

	@Override
	public void onEnable() {	 
	instance = this;
	  getProxy().getScheduler().schedule(this, () -> start(), 15, TimeUnit.SECONDS);
	//CompletableFuture.runAsync(() -> start());
	}

	private void start() {
 
		loadConfig();
		MySQL.connect(getConfiguration());
		MySQL.createDatabase();
		startBot();
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new CommandDiscord("discord"));
	}
	
	private void startBot() {

		Configuration cfg = getConfiguration();
		JDABuilder bot = new JDABuilder(AccountType.BOT);
		bot.setAutoReconnect(true);
		bot.setToken(cfg.getString("Discord.token"));
		bot.setActivity(Activity.playing(cfg.getString("Discord.game")));
		bot.addEventListeners(new MessageListener());
		try {
			jda = bot.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			ProxyServer.getInstance().getConsole()
					.sendMessage(new TextComponent("§4§l[UltimateDiscord] Invalid discord token"));
			e.printStackTrace();
		}

	}

	public static void loadConfig() {
		try {
			configuration = ConfigurationProvider.getProvider(YamlConfiguration.class)
					.load(loadResource(instance, "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File loadResource(Plugin plugin, String resource) {
		File folder = plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
		File resourceFile = new File(folder, resource);
		try {
			if (!resourceFile.exists()) {
				resourceFile.createNewFile();
				try (InputStream in = plugin.getResourceAsStream(resource);
						OutputStream out = new FileOutputStream(resourceFile)) {
					ByteStreams.copy(in, out);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceFile;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}
}
