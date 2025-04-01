package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.configuration.data.UspMessages;

public class RempassSubcommand extends AbstractSubCommand {

    public RempassSubcommand(Main plugin) {
        super(plugin, "rempass", "serverprotector.rempass", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        var uspMessages = pluginConfig.uspMessages;
        if (args.length > 1) {
            if (!plugin.isAdmin(args[1]) && !plugin.isAdmin(pluginConfig.geyserSettings.prefix() + args[1])) {
                sender.sendMessage(uspMessages.notInConfig());
                return true;
            }
            if (args.length < 3) {
                removeAdmin(args[1]);
                sender.sendMessage(uspMessages.playerRemoved());
                return true;
            }
        }
        sendCmdUsage(sender, uspMessages.remPassUsage(), label);
        return true;
    }

    private void removeAdmin(String nick) {
        var dataFile = pluginConfig.getFile(plugin.dataFilePath, plugin.dataFileName);
        if (!pluginConfig.encryptionSettings.enableEncryption()) {
            dataFile.set("data." + nick + ".pass", null);
            dataFile.set("data." + nick, null);
        } else {
            dataFile.set("data." + nick + ".encrypted-pass", null);
        }
        dataFile.set("data." + nick, null);
        pluginConfig.save(plugin.dataFilePath, dataFile, plugin.dataFileName);
        plugin.dataFile = dataFile;
    }
}
