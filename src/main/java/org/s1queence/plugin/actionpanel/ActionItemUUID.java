package org.s1queence.plugin.actionpanel;


import org.jetbrains.annotations.NotNull;

public enum ActionItemUUID {
    LOOK_AT ("lookat"),
    LIFT_AND_CARRY ("lift_and_carry"),
    LAY ("lay"),
    SIT ("sit"),
    CRAWL ("crawl"),
    PUT ("put"),
    RUMMAGE ("rummage"),
    PUSH ("push"),
    DROP_BLOCK ("dropblock"),
    CLOSE ("close"),
    NOTIFY ("notify"),
    VIEW ("view");
    private final String value;
    ActionItemUUID(@NotNull String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ActionItemUUID fromString(String str) {
        for (ActionItemUUID aiUUID : ActionItemUUID.values())
            if (aiUUID.toString().equals(str)) return aiUUID;

        return null;
    }
}
