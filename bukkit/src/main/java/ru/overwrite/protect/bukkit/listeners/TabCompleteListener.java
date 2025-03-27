package ru.overwrite.protect.bukkit.listeners;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.protect.bukkit.ServerProtector;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public class TabCompleteListener implements Listener {

    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public TabCompleteListener(ServerProtector plugin) {
        this.api = plugin.api;
        this.pluginConfig = plugin.pluginConfig;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!(e.getSender() instanceof Player player))
            return;
        if (pluginConfig.blockingSettings.blockTabComplete()) {
            api.handleInteraction(player, e);
        }
    }
}
