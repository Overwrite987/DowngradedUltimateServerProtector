package ru.overwrite.protect.bukkit.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.api.events.ServerProtectorLogoutEvent;
import ru.overwrite.protect.bukkit.utils.Utils;

public class LogoutSubcommand extends AbstractSubCommand {

    public LogoutSubcommand(Main plugin) {
        super(plugin, "logout", "serverprotector.protect", false);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        var uspMessages = pluginConfig.uspMessages;
        if (!(sender instanceof Player player)) {
            sender.sendMessage(uspMessages.playerOnly());
            return false;
        }
        if (api.isAuthorised(player)) {
            plugin.runner.run(() -> {
                new ServerProtectorLogoutEvent(player, Utils.getIp(player)).callEvent();
                api.deauthorisePlayer(player);
            });
            player.kickPlayer(uspMessages.logout());
            return true;
        }
        return false;
    }
}
