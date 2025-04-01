package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;
import ru.overwrite.protect.bukkit.utils.Utils;

import java.util.List;

public class AddopSubcommand extends AbstractSubCommand {

    public AddopSubcommand(Main plugin) {
        super(plugin, "addop", "serverprotector.addop", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.uspMessages;
        if (args.length > 1) {
            var nickname = args[1];

            if (Utils.SUB_VERSION >= 16) {
                var targetPlayer = Bukkit.getOfflinePlayerIfCached(nickname);
                if (targetPlayer == null) {
                    sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", nickname));
                    return true;
                }
                nickname = targetPlayer.getName();
            }

            var whitelist = pluginConfig.accessData.opWhitelist();
            whitelist.add(nickname);
            plugin.getConfig().set("op-whitelist", whitelist);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.playerAdded().replace("%nick%", nickname));
            return true;
        }

        sendCmdUsage(sender, uspMessages.addOpUsage(), label);
        return true;
    }
}
