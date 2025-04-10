package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.utils.Utils;

public class SetpassSubcommand extends AbstractSubCommand {

    public SetpassSubcommand(Main plugin) {
        super(plugin, "setpass", "serverprotector.setpass", true);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        var uspMessages = pluginConfig.uspMessages;
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
            if (plugin.isAdmin(nickname)) {
                sender.sendMessage(uspMessages.alreadyInConfig());
                return true;
            }
            if (args.length < 4) {
                addAdmin(nickname, args[2]);
                sender.sendMessage(uspMessages.playerAdded().replace("%nick%", nickname));
                return true;
            }
        }
        sendCmdUsage(sender, uspMessages.setPassUsage(), label);
        return true;
    }

    private void addAdmin(String nick, String pas) {
        var dataFile = pluginConfig.getFile(plugin.dataFilePath, plugin.dataFileName);
        var encryptionSettings = pluginConfig.encryptionSettings;
        if (!encryptionSettings.enableEncryption()) {
            dataFile.set("data." + nick + ".pass", pas);
        } else if (encryptionSettings.autoEncryptPasswords()) {
            var encryptedPas = Utils.encryptPassword(pas, Utils.generateSalt(encryptionSettings.saltLength()), encryptionSettings.encryptMethods());
            dataFile.set("data." + nick + ".encrypted-pass", encryptedPas);
        } else {
            dataFile.set("data." + nick + ".encrypted-pass", pas);
        }
        pluginConfig.save(plugin.dataFilePath, dataFile, plugin.dataFileName);
        plugin.dataFile = dataFile;
    }
}
