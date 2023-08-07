package minealex.tcommandblocker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class CommandBlocker extends JavaPlugin implements CommandExecutor, Listener {

    private FileConfiguration config;
    private String blockedCommandMessage;
    private String configReloadedMessage;
    private List<String> blockedCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        getCommand("tcommandblocker").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tcommandblocker")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("tcommandblocker.reload")) {
                    reloadConfig();
                    loadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configReloadedMessage));
                } else {
                    String noPermissionMessage = config.getString("messages.no-permission-message", "&5TCommandBlocker &e> &cYou do not have permission to use this command.");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0].substring(1); // Get the command without "/"
        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', blockedCommandMessage));
        }
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        config.options().copyDefaults(true);
        saveConfig();

        // Load custom messages and blocked commands from the configuration file
        blockedCommandMessage = config.getString("messages.blocked-command", "&c&5TCommandBlocker &e> &cYou do not have permission to execute this command.");
        configReloadedMessage = config.getString("messages.config-reloaded", "&5TCommandBlocker &e> &aConfiguration reloaded correctly.");
        String defaultNoPermissionMessage = "&5TCommandBlocker &e> &cYou do not have permission to use this command.";
        blockedCommands = config.getStringList("blocked-commands");
        blockedCommands.replaceAll(command -> command.toLowerCase()); // Ensure all blocked commands are lowercase
        config.addDefault("messages.no-permission-message", defaultNoPermissionMessage);
        config.options().copyDefaults(true);
        saveConfig();
    }
}
