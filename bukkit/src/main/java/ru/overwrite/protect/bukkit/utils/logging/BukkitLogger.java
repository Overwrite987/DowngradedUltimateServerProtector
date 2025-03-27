package ru.overwrite.protect.bukkit.utils.logging;


import ru.overwrite.protect.bukkit.ServerProtector;

public class BukkitLogger implements Logger {

    private final ServerProtector plugin;

    public BukkitLogger(ServerProtector plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

}
