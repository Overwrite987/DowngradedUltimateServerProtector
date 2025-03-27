package ru.overwrite.protect.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordEnterEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordFailEvent;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorPasswordSuccessEvent;
import ru.overwrite.protect.bukkit.configuration.Config;
import ru.overwrite.protect.bukkit.configuration.data.EncryptionSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PasswordHandler {

    private final ServerProtector plugin;
    private final ServerProtectorAPI api;
    private final Config pluginConfig;

    public final Map<String, Integer> attempts = new HashMap<>();

    public final Map<String, BossBar> bossbars = new HashMap<>();

    public PasswordHandler(ServerProtector plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.pluginConfig;
        this.api = plugin.api;
    }

    public void checkPassword(Player player, String input, boolean resync) {
        Runnable run = () -> {
            ServerProtectorPasswordEnterEvent enterEvent = new ServerProtectorPasswordEnterEvent(player, input);
            if (pluginConfig.apiSettings.callEventOnPasswordEnter()) {
                enterEvent.callEvent();
            }
            if (enterEvent.isCancelled()) {
                return;
            }
            String playerPass = pluginConfig.perPlayerPasswords.get(player.getName());
            if (playerPass == null) {
                this.failedPassword(player);
                return;
            }
            EncryptionSettings encryptionSettings = pluginConfig.encryptionSettings;
            String salt = playerPass.split(":")[0];
            String pass = encryptionSettings.enableEncryption()
                    ? Utils.encryptPassword(input, salt, encryptionSettings.encryptMethods())
                    : input;
            if (pass.equals(playerPass)) {
                this.correctPassword(player);
                return;
            }
            if (encryptionSettings.enableEncryption() && !encryptionSettings.oldEncryptMethods().isEmpty()) {
                for (List<String> oldEncryptMethod : encryptionSettings.oldEncryptMethods()) {
                    String oldgenPass = Utils.encryptPassword(input, salt, oldEncryptMethod);
                    if (oldgenPass.equals(playerPass)) {
                        this.correctPassword(player);
                        return;
                    }
                }
            }
            this.failedPassword(player);
            if (pluginConfig.punishSettings.enableAttempts() && isAttemptsMax(player.getName())) {
                plugin.checkFail(player.getName(), pluginConfig.commands.failedPass());
            }
        };
        if (resync) {
            plugin.runner.runPlayer(run, player);
        } else {
            run.run();
        }
    }

    private boolean isAttemptsMax(String playerName) {
        int playerAttempts = attempts.getOrDefault(playerName, 0);
        return playerAttempts >= pluginConfig.punishSettings.maxAttempts();
    }

    public void failedPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        String playerName = player.getName();
        if (pluginConfig.punishSettings.enableAttempts()) {
            attempts.put(playerName, attempts.getOrDefault(playerName, 0) + 1);
        }
        ServerProtectorPasswordFailEvent failEvent = new ServerProtectorPasswordFailEvent(player, attempts.get(playerName));
        failEvent.callEvent();
        if (failEvent.isCancelled()) {
            return;
        }
        player.sendMessage(pluginConfig.messages.incorrect());
        if (pluginConfig.messageSettings.sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.titles.incorrect(), player);
        }
        if (pluginConfig.soundSettings.enableSounds()) {
            Utils.sendSound(pluginConfig.soundSettings.onPasFail(), player);
        }
        if (pluginConfig.loggingSettings.loggingPas()) {
            plugin.logAction(pluginConfig.logMessages.failed(), player, LocalDateTime.now());
        }
        plugin.sendAlert(player, pluginConfig.broadcasts.failed());
    }

    public void correctPassword(Player player) {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        ServerProtectorPasswordSuccessEvent successEvent = new ServerProtectorPasswordSuccessEvent(player);
        successEvent.callEvent();
        if (successEvent.isCancelled()) {
            return;
        }
        String playerName = player.getName();
        api.uncapturePlayer(playerName);
        player.sendMessage(pluginConfig.messages.correct());
        if (pluginConfig.messageSettings.sendTitle()) {
            Utils.sendTitleMessage(pluginConfig.titles.correct(), player);
        }
        plugin.perPlayerTime.remove(playerName);
        if (pluginConfig.soundSettings.enableSounds()) {
            Utils.sendSound(pluginConfig.soundSettings.onPasCorrect(), player);
        }
        if (pluginConfig.effectSettings.enableEffects()) {
            plugin.removeEffects(player);
        }
        this.showPlayer(player);
        api.authorisePlayer(player);
        if (pluginConfig.sessionSettings.sessionTimeEnabled()) {
            plugin.runner.runDelayedAsync(() -> {
                if (!api.isAuthorised(player)) {
                    api.deauthorisePlayer(player);
                }
            }, pluginConfig.sessionSettings.sessionTime() * 20L);
        }
        if (pluginConfig.loggingSettings.loggingPas()) {
            plugin.logAction(pluginConfig.logMessages.passed(), player, LocalDateTime.now());
        }
        if (pluginConfig.bossbarSettings.enableBossbar() && bossbars.get(playerName) != null) {
            bossbars.get(playerName).removeAll();
            bossbars.remove(playerName);
        }
        plugin.sendAlert(player, pluginConfig.broadcasts.passed());
    }

    private void showPlayer(Player player) {
        if (pluginConfig.blockingSettings.hideOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    onlinePlayer.showPlayer(plugin, player);
                }
            }
        }
        if (pluginConfig.blockingSettings.hideOtherOnEntering()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, onlinePlayer);
            }
        }
    }
}
