package ru.overwrite.protect.bukkit.utils.logging;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.overwrite.protect.bukkit.ServerProtector;


public class PaperLogger implements Logger {

    private final ServerProtector plugin;

    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public PaperLogger(ServerProtector plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getComponentLogger().info(legacySection.deserialize(msg));
    }

    @Override
    public void warn(String msg) {
        plugin.getComponentLogger().warn(legacySection.deserialize(msg));
    }

}
