package ru.overwrite.protect.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.Main;
import ru.overwrite.protect.bukkit.commands.subcommands.*;
import ru.overwrite.protect.bukkit.configuration.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UspCommand implements TabExecutor {

    private final Main plugin;
    private final Config pluginConfig;

    private final Map<String, AbstractSubCommand> subCommands = new HashMap<>();

    public UspCommand(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.pluginConfig;
        registerSubCommands(plugin);
    }

    private void registerSubCommands(Main plugin) {
        registerSubCommand(new LogoutSubcommand(plugin));
        registerSubCommand(new ReloadSubcommand(plugin));
        registerSubCommand(new RebootSubcommand(plugin));
        registerSubCommand(new EncryptSubcommand(plugin));
        registerSubCommand(new SetpassSubcommand(plugin));
        registerSubCommand(new AddopSubcommand(plugin));
        registerSubCommand(new AddipSubcommand(plugin));
        registerSubCommand(new RempassSubcommand(plugin));
        registerSubCommand(new RemopSubcommand(plugin));
        registerSubCommand(new RemipSubcommand(plugin));
        registerSubCommand(new UpdateSubcommand(plugin));
    }

    private void registerSubCommand(AbstractSubCommand subCmd) {
        subCommands.put(subCmd.name, subCmd);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        var subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            if (subCommand.adminCommand) {
                if (!pluginConfig.mainSettings.enableAdminCommands()) {
                    sendHelp(sender, label);
                    return false;
                }
                if (pluginConfig.secureSettings.onlyConsoleUsp() && !(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(pluginConfig.uspMessages.consoleOnly());
                    return false;
                }
            }
            if (!sender.hasPermission(subCommand.permission)) {
                sendHelp(sender, label);
                return false;
            }
            return subCommand.execute(sender, label, args);
        }
        if (sender.hasPermission("serverprotector.protect")) {
            sendHelp(sender, label);
            return true;
        }
        sender.sendMessage("§6❖ §7Running §c§lUltimateServerProtector " + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        var uspMessages = pluginConfig.uspMessages;
        sendCmdMessage(sender, uspMessages.usage(), label, "protect");
        sendCmdMessage(sender, uspMessages.usageLogout(), label, "protect");
        if (!sender.hasPermission("admin")) {
            return;
        }
        sendCmdMessage(sender, uspMessages.usageReload(), label, "reload");
        sendCmdMessage(sender, uspMessages.usageReboot(), label, "reboot");
        if (pluginConfig.encryptionSettings.enableEncryption()) {
            sendCmdMessage(sender, uspMessages.usageEncrypt(), label, "encrypt");
        }
        if (!pluginConfig.mainSettings.enableAdminCommands()) {
            sender.sendMessage(uspMessages.otherDisabled());
            return;
        }
        sendCmdMessage(sender, uspMessages.setPassUsage(), label, "setpass");
        sendCmdMessage(sender, uspMessages.usageRemPass(), label, "rempass");
        sendCmdMessage(sender, uspMessages.usageAddOp(), label, "addop");
        sendCmdMessage(sender, uspMessages.usageRemOp(), label, "remop");
        sendCmdMessage(sender, uspMessages.usageAddIp(), label, "addip");
        sendCmdMessage(sender, uspMessages.usageRemIp(), label, "remip");
    }

    private void sendCmdMessage(CommandSender sender, String msg, String label, String permission) {
        if (sender.hasPermission("serverprotector." + permission)) {
            sender.sendMessage(msg.replace("%cmd%", label));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (pluginConfig.secureSettings.onlyConsoleUsp() && !(sender instanceof ConsoleCommandSender)) {
            return List.of();
        }
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("logout");
            completions.add("reload");
            completions.add("reboot");
            if (pluginConfig.encryptionSettings.enableEncryption()) {
                completions.add("encrypt");
            }
            if (pluginConfig.mainSettings.enableAdminCommands()) {
                completions.add("setpass");
                completions.add("rempass");
                completions.add("addop");
                completions.add("remop");
                completions.add("addip");
                completions.add("remip");
            }
        }
        return getResult(args, completions);
    }

    private List<String> getResult(String[] args, List<String> completions) {
        final List<String> result = new ArrayList<>();
        for (var c : completions) {
            if (StringUtil.startsWithIgnoreCase(c, args[args.length - 1])) {
                result.add(c);
            }
        }
        return result;
    }
}
