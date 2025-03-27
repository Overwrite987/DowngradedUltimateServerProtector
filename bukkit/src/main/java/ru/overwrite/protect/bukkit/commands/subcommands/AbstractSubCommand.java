package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.PasswordHandler;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.ServerProtectorAPI;
import ru.overwrite.protect.bukkit.configuration.Config;

public abstract class AbstractSubCommand implements SubCommand {

    public final String name;
    public final String permission;
    public final boolean adminCommand;

    protected final Main plugin;
    protected final ServerProtectorAPI api;
    protected final Config pluginConfig;
    protected final PasswordHandler passwordHandler;

    protected AbstractSubCommand(Main plugin, String name, String permission, boolean adminCommand) {
        this.plugin = plugin;
        this.api = plugin.api;
        this.pluginConfig = plugin.pluginConfig;
        this.passwordHandler = plugin.passwordHandler;
        this.name = name;
        this.permission = permission;
        this.adminCommand = adminCommand;
    }

    protected void sendCmdUsage(CommandSender sender, String msg, String label) {
        sender.sendMessage(msg.replace("%cmd%", label));
    }

}
