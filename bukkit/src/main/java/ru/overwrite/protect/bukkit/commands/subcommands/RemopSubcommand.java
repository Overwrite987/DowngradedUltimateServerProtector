package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

import java.util.List;

public class RemopSubcommand extends AbstractSubCommand {

    public RemopSubcommand(Main plugin) {
        super(plugin, "remop", "serverprotector.remop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        var uspMessages = pluginConfig.uspMessages;
        if (args.length > 1) {
            var nickname = args[1];
            var wl = pluginConfig.accessData.opWhitelist();
            if (!wl.remove(nickname)) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
            }
            plugin.getConfig().set("op-whitelist", wl);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.playerRemoved().replace("%nick%", nickname));
            return true;
        }
        sendCmdUsage(sender, uspMessages.remOpUsage(), label);
        return true;
    }
}
