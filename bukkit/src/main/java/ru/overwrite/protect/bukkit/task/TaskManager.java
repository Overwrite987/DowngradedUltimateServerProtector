package ru.overwrite.protect.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.CaptureReason;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorCaptureEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.BossbarSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;

public class TaskManager {

    private final Main plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;
    private final Runner runner;

    public TaskManager(Main plugin) {
        this.plugin = plugin;
        this.api = plugin.api;
        this.passwordHandler = plugin.passwordHandler;
        this.pluginConfig = plugin.pluginConfig;
        this.runner = plugin.runner;
    }

    public void startMainCheck(long interval) {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (pluginConfig.excludedPlayers != null && plugin.isExcluded(onlinePlayer, pluginConfig.excludedPlayers.adminPass())) {
                    continue;
                }
                if (api.isCaptured(onlinePlayer)) {
                    continue;
                }
                CaptureReason captureReason = plugin.checkPermissions(onlinePlayer);
                if (captureReason == null) {
                    continue;
                }
                if (!api.isAuthorised(onlinePlayer)) {
                    ServerProtectorCaptureEvent captureEvent = new ServerProtectorCaptureEvent(onlinePlayer, Utils.getIp(onlinePlayer), captureReason);
                    captureEvent.callEvent();
                    if (pluginConfig.apiSettings.allowCancelCaptureEvent() && captureEvent.isCancelled()) {
                        continue;
                    }
                    api.capturePlayer(onlinePlayer);
                    if (pluginConfig.soundSettings.enableSounds()) {
                        Utils.sendSound(pluginConfig.soundSettings.onCapture(), onlinePlayer);
                    }
                    if (pluginConfig.effectSettings.enableEffects()) {
                        plugin.giveEffects(onlinePlayer);
                    }
                    plugin.applyHide(onlinePlayer);
                    if (pluginConfig.loggingSettings.loggingPas()) {
                        plugin.logAction(pluginConfig.logMessages.captured(), onlinePlayer, LocalDateTime.now());
                    }
                    plugin.sendAlert(onlinePlayer, pluginConfig.broadcasts.captured());
                }
            }
        }, 20L, interval >= 0 ? interval : 40L);
    }

    public void startAdminCheck() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer) && !plugin.isAdmin(onlinePlayer.getName())) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.commands.notInConfig());
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesMessages(FileConfiguration config) {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (api.isCaptured(onlinePlayer)) {
                    onlinePlayer.sendMessage(pluginConfig.messages.message());
                    if (pluginConfig.messageSettings.sendTitle()) {
                        Utils.sendTitleMessage(pluginConfig.titles.message(), onlinePlayer);
                    }
                }
            }
        }, 5L, config.getInt("message-settings.delay") * 20L);
    }

    public void startOpCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isOp()
                        && !pluginConfig.accessData.opWhitelist().contains(onlinePlayer.getName())
                        && (pluginConfig.excludedPlayers == null || !plugin.isExcluded(onlinePlayer, pluginConfig.excludedPlayers.opWhitelist()))) {
                    plugin.checkFail(onlinePlayer.getName(), pluginConfig.commands.notInOpWhitelist());
                }
            }
        }, 5L, 20L);
    }

    public void startPermsCheck() {
        runner.runPeriodicalAsync(() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                for (String blacklistedPerm : pluginConfig.accessData.blacklistedPerms()) {
                    if (onlinePlayer.hasPermission(blacklistedPerm) &&
                            (pluginConfig.excludedPlayers == null || !plugin.isExcluded(onlinePlayer, pluginConfig.excludedPlayers.blacklistedPerms()))) {
                        plugin.checkFail(onlinePlayer.getName(), pluginConfig.commands.haveBlacklistedPerm());
                    }
                }
            }
        }, 5L, 20L);
    }

    public void startCapturesTimer() {
        runner.runPeriodicalAsync(() -> {
            if (!api.isAnybodyCaptured())
                return;
            BossbarSettings bossbarSettings = pluginConfig.bossbarSettings;
            int time = pluginConfig.punishSettings.time();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isDead() || !api.isCaptured(onlinePlayer)) {
                    return;
                }
                String playerName = onlinePlayer.getName();
                if (!plugin.perPlayerTime.containsKey(playerName)) {
                    plugin.perPlayerTime.put(playerName, 0);
                    if (bossbarSettings.enableBossbar()) {
                        BossBar bossbar = Bukkit.createBossBar(
                                bossbarSettings.bossbarMessage().replace("%time%", Integer.toString(time)),
                                bossbarSettings.barColor(),
                                bossbarSettings.barStyle());
                        bossbar.addPlayer(onlinePlayer);
                        passwordHandler.bossbars.put(playerName, bossbar);
                    }
                } else {
                    int newTime = plugin.perPlayerTime.compute(playerName, (k, currentTime) -> currentTime + 1);
                    BossBar bossBar = passwordHandler.bossbars.get(playerName);
                    if (bossbarSettings.enableBossbar() && bossBar != null) {
                        bossBar.setTitle(bossbarSettings.bossbarMessage().replace("%time%", Integer.toString(time - newTime)));
                        double percents = (time - newTime)
                                / (double) time;
                        if (percents > 0) {
                            bossBar.setProgress(percents);
                            bossBar.addPlayer(onlinePlayer);
                        }
                    }
                    if (time - newTime <= 0) {
                        plugin.checkFail(playerName, pluginConfig.commands.failedTime());
                        passwordHandler.bossbars.get(playerName).removePlayer(onlinePlayer);
                    }
                }
            }
        }, 5L, 20L);
    }
}
