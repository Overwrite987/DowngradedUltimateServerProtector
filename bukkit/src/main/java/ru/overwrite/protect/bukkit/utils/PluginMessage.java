package ru.overwrite.protect.bukkit.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.Main;

public class PluginMessage implements PluginMessageListener {

    private final Main plugin;

    public PluginMessage(Main plugin) {
        this.plugin = plugin;
    }

    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;
        var input = ByteStreams.newDataInput(message);
        var subchannel = input.readUTF();
        if (subchannel.equalsIgnoreCase("serverprotector")) {
            var msg = input.readUTF();
            for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("serverprotector.admin")) {
                    onlinePlayer.sendMessage(msg);
                }
            }
        }
    }

    public void sendCrossProxy(Player player, String message) {
        var out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("serverprotector");
        out.writeUTF(message);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
