package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public class ChatListener implements Listener {

    private final Main plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.pluginConfig;
        this.passwordHandler = plugin.passwordHandler;
        this.api = plugin.api;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        var player = e.getPlayer();
        if (!api.isCaptured(player)) {
            return;
        }
        if (!pluginConfig.mainSettings.useCommand()) {
            var message = e.getMessage();
            passwordHandler.checkPassword(player, message, true);
        }
        e.setMessage("");
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        var player = e.getPlayer();
        if (!api.isCaptured(player)) {
            return;
        }
        var message = e.getMessage();
        var label = cutCommand(message);
        if (pluginConfig.mainSettings.useCommand()) {
            if (label.equalsIgnoreCase("/" + pluginConfig.mainSettings.pasCommand())) {
                if (!plugin.paper) {
                    passwordHandler.checkPassword(player, message.split(" ", 1)[1], false);
                }
                return;
            }
        }
        var allowedCommands = pluginConfig.accessData.allowedCommands();
        for (var command : allowedCommands) {
            if (label.equalsIgnoreCase(command) || message.equalsIgnoreCase(command)) {
                return;
            }
        }
        e.setCancelled(true);
    }

    private String cutCommand(String str) {
        var index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }
}
