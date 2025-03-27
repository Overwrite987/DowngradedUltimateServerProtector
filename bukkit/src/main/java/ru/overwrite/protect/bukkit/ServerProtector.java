package ru.overwrite.protect.bukkit;

import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import java.time.LocalDateTime;

public final class ServerProtector extends ServerProtectorManager {

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        final ConfigurationSection mainSettings = config.getConfigurationSection("main-settings");
        setupLogger(config);
        setupProxy(config);
        loadConfigs(config);
        PluginManager pluginManager = server.getPluginManager();
        checkSafe(pluginManager);
        checkPaper();
        registerListeners(pluginManager);
        registerCommands(pluginManager, mainSettings);
        startTasks(config);
        logEnableDisable(pluginConfig.logMessages.enabled(), LocalDateTime.now());
        if (mainSettings.getBoolean("enable-metrics", true)) {
            new Metrics(this, 13347);
        }
        checkForUpdates(mainSettings);
        long endTime = System.currentTimeMillis();
        pluginLogger.info("Plugin started in " + (endTime - startTime) + " ms");
    }

    @Override
    public void onDisable() {
        if (messageFile != null) {
            logEnableDisable(pluginConfig.logMessages.disabled(), LocalDateTime.now());
        }
        if (pluginConfig.messageSettings.enableBroadcasts()) {
            for (Player onlinePlayer : server.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("serverprotector.admin") && messageFile != null) {
                    onlinePlayer.sendMessage(pluginConfig.broadcasts.disabled());
                }
            }
        }
        runner.cancelTasks();
        if (pluginMessage != null) {
            Messenger messenger = server.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }
        if (getConfig().getBoolean("secure-settings.shutdown-on-disable")) {
            server.shutdown();
        }
    }
}
