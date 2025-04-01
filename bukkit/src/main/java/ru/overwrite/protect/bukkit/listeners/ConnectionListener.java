package ru.overwrite.protect.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.task.Runner;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionListener implements Listener {

    private final Main plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    private final Runner runner;

    public ConnectionListener(Main plugin) {
        this.plugin = plugin;
        this.api = plugin.api;
        this.pluginConfig = plugin.pluginConfig;
        this.runner = plugin.runner;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (!plugin.safe) {
            plugin.logUnsafe();
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        var player = e.getPlayer();
        player.loadData();
        runner.runAsync(() -> {
            var playerName = player.getName();
            var captureReason = plugin.checkPermissions(player);
            if (api.isCaptured(playerName) && captureReason == null) {
                api.uncapturePlayer(playerName);
                return;
            }
            if (captureReason != null) {
                var ip = e.getAddress().getHostAddress();
                if (pluginConfig.secureSettings.enableIpWhitelist()) {
                    if (!isIPAllowed(ip, pluginConfig.accessData.ipWhitelist().get(playerName))) {
                        if (pluginConfig.excludedPlayers == null || !plugin.isExcluded(player, pluginConfig.excludedPlayers.ipWhitelist())) {
                            plugin.checkFail(playerName, pluginConfig.commands.notAdminIp());
                        }
                    }
                }
                if (pluginConfig.sessionSettings.session() && !api.hasSession(playerName, ip)) {
                    if (pluginConfig.excludedPlayers == null || !plugin.isExcluded(player, pluginConfig.excludedPlayers.adminPass())) {
                        var captureEvent = new ServerProtectorCaptureEvent(player, ip, captureReason);
                        captureEvent.callEvent();
                        if (pluginConfig.apiSettings.allowCancelCaptureEvent() && captureEvent.isCancelled()) {
                            return;
                        }
                        api.capturePlayer(playerName);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        runner.runAsync(() -> {
            var player = e.getPlayer();
            var captureReason = plugin.checkPermissions(player);
            if (captureReason != null) {
                if (api.isCaptured(player)) {
                    if (pluginConfig.effectSettings.enableEffects()) {
                        plugin.giveEffects(player);
                    }
                    plugin.applyHide(player);
                }
                if (pluginConfig.loggingSettings.loggingJoin()) {
                    plugin.logAction(pluginConfig.logMessages.joined(), player, LocalDateTime.now());
                }
                plugin.sendAlert(player, pluginConfig.broadcasts.joined());
            }
        });
    }

    private boolean isIPAllowed(String playerIp, List<String> allowedIps) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }

        outer:
        for (var allowedIp : allowedIps) {
            int playerIpLength = playerIp.length();
            int allowedIpLength = allowedIp.length();

            if (playerIpLength != allowedIpLength && !allowedIp.contains("*")) {
                continue;
            }

            for (int n = 0; n < allowedIpLength; n++) {
                var currentChar = allowedIp.charAt(n);
                if (currentChar == '*') {
                    return true;
                }

                if (n >= playerIpLength || currentChar != playerIp.charAt(n)) {
                    continue outer;
                }
            }

            if (playerIpLength == allowedIpLength) {
                return true;
            }
        }

        return false;
    }

    private final Map<String, Integer> rejoins = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        var player = event.getPlayer();
        handlePlayerLeave(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        var player = event.getPlayer();
        handlePlayerLeave(player);
    }

    private void handlePlayerLeave(Player player) {
        var playerName = player.getName();
        if (api.isCaptured(player)) {
            for (var effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            if (pluginConfig.punishSettings.enableRejoin()) {
                rejoins.put(playerName, rejoins.getOrDefault(playerName, 0) + 1);
                if (isMaxRejoins(playerName)) {
                    rejoins.remove(playerName);
                    plugin.checkFail(playerName, pluginConfig.commands.failedRejoin());
                }
            }
        }
        plugin.perPlayerTime.remove(playerName);
        api.unsavePlayer(playerName);
    }

    private boolean isMaxRejoins(String playerName) {
        if (!rejoins.containsKey(playerName))
            return false;
        return rejoins.get(playerName) > pluginConfig.punishSettings.maxRejoins();
    }
}
