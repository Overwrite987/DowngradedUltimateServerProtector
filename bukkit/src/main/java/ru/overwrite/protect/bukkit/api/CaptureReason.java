package ru.overwrite.protect.bukkit.api;

public record CaptureReason(String permission) {

    public enum Reason {
        OPERATOR,
        PERMISSION
    }
}
