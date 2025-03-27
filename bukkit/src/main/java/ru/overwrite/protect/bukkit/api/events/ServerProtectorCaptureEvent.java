package ru.overwrite.protect.bukkit.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.api.CaptureReason;

public class ServerProtectorCaptureEvent extends ServerProtectorPlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String ip;

    private final CaptureReason captureReason;

    private boolean isCancelled = false;

    public ServerProtectorCaptureEvent(Player player, String ip, CaptureReason captureReason) {
        super(player, true);
        this.player = player;
        this.ip = ip;
        this.captureReason = captureReason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

}
