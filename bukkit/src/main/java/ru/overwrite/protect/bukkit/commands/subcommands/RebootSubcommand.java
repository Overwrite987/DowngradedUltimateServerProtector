package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.protect.bukkit.ServerProtectorManager;

public class RebootSubcommand extends AbstractSubCommand {

    public RebootSubcommand(ServerProtectorManager plugin) {
        super(plugin, "reboot", "serverprotector.reboot", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        plugin.runner.cancelTasks();
        plugin.reloadConfigs();
        plugin.perPlayerTime.clear();
        api.clearCaptured();
        api.clearSaved();
        api.clearSessions();
        for (String playerName : passwordHandler.bossbars.keySet()) {
            passwordHandler.bossbars.get(playerName).removeAll();
        }
        passwordHandler.bossbars.clear();
        passwordHandler.attempts.clear();
        FileConfiguration newconfig = plugin.getConfig();
        plugin.startTasks(newconfig);
        sender.sendMessage(pluginConfig.uspMessages.rebooted());
        return true;
    }
}
