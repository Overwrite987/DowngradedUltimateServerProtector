package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public class PasCommand implements CommandExecutor {

    private final Main plugin;
    private final ServerProtectorAPI api;
    private final PasswordHandler passwordHandler;
    private final Config pluginConfig;

    public PasCommand(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.pluginConfig;
        this.passwordHandler = plugin.passwordHandler;
        this.api = plugin.api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.pluginLogger.info(pluginConfig.messages.playerOnly());
            return true;
        }
        if (!api.isCaptured(player)) {
            sender.sendMessage(pluginConfig.messages.noNeed());
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(pluginConfig.messages.cantBeNull());
            return true;
        }
        passwordHandler.checkPassword(player, args[0], false);
        return true;
    }
}
