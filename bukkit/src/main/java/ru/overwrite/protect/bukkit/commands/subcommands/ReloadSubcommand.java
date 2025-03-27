package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;

public class ReloadSubcommand extends AbstractSubCommand {

    public ReloadSubcommand(Main plugin) {
        super(plugin, "reload", "serverprotector.reload", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        plugin.reloadConfigs();
        sender.sendMessage(pluginConfig.uspMessages.reloaded());
        return true;
    }
}
