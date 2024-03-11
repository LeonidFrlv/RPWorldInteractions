package org.s1queence.plugin.actionpanel;


import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;


public enum ActionItemUUID {
    LOOK_AT (ChatColor.DARK_GRAY + "#lookat"),
    LIFT_AND_CARRY (ChatColor.DARK_GRAY + "#lift_and_carry"),
    LAY (ChatColor.DARK_GRAY + "#lay"),
    SIT (ChatColor.DARK_GRAY + "#sit"),
    CRAWL (ChatColor.DARK_GRAY + "#crawl"),
    PUT (ChatColor.DARK_GRAY + "#put"),
    RUMMAGE (ChatColor.DARK_GRAY + "#rummage"),
    PUSH (ChatColor.DARK_GRAY + "#push"),
    DROP_BLOCK (ChatColor.DARK_GRAY + "#dropblock"),
    CLOSE (ChatColor.DARK_GRAY + "#close"),
    NOTIFY (ChatColor.DARK_GRAY + "#notify"),
    VIEW (ChatColor.DARK_GRAY + "#view");
    private final String value;
    ActionItemUUID(@NotNull String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ActionItemUUID fromString(String str) {
        for (ActionItemUUID aiUUID : ActionItemUUID.values()) {
            if (aiUUID.toString().equals(str)) return aiUUID;
        }
        return null;
    }
}
