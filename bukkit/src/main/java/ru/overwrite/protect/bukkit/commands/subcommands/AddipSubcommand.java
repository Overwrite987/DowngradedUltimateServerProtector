package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

public class AddipSubcommand extends AbstractSubCommand {

    public AddipSubcommand(Main plugin) {
        super(plugin, "addip", "serverprotector.addip", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        UspMessages uspMessages = pluginConfig.uspMessages;
        if (args.length > 2 && (args[1] != null && args[2] != null)) {
            var ipwl = pluginConfig.accessData.ipWhitelist().get(args[1]);
            if (ipwl.isEmpty()) {
                sender.sendMessage(uspMessages.playerNotFound().replace("%nick%", args[1]));
            }
            ipwl.add(args[2]);
            plugin.getConfig().set("ip-whitelist." + args[1], ipwl);
            plugin.saveConfig();
            sender.sendMessage(uspMessages.ipAdded().replace("%nick%", args[1]).replace("%ip%", args[2]));
            return true;
        }
        sendCmdUsage(sender, uspMessages.addIpUsage(), label);
        return true;
    }
}
