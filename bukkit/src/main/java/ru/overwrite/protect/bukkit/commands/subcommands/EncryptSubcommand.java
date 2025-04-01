package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.configuration.data.EncryptionSettings;
import ru.overwrite.protect.bukkit.utils.Utils;

public class EncryptSubcommand extends AbstractSubCommand {

    public EncryptSubcommand(Main plugin) {
        super(plugin, "encrypt", "serverprotector.encrypt", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        var encryptionSettings = pluginConfig.encryptionSettings;
        if (encryptionSettings.enableEncryption() && args.length == 2) {
            sender.sendMessage(
                    Utils.encryptPassword(args[1],
                            Utils.generateSalt(encryptionSettings.saltLength()),
                            encryptionSettings.encryptMethods()));
            return true;
        }
        return false;
    }
}
