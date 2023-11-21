package org.s1queence.plugin.utils;

import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.plugin.utils.TextUtils.createItemFromConfig;

public class MyUtils {
    public static ItemStack empty(RPWorldInteractions plugin) {
        return createItemFromConfig(plugin.getOptionsConfig().getSection("empty_item").getStringRouteMappedValues(true), false);
    }
}
