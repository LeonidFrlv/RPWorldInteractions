package org.s1queence.plugin.actionpanel;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import java.util.*;

import static org.s1queence.plugin.utils.BarrierClickListener.empty;
import static org.s1queence.plugin.utils.TextUtils.createItemFromConfig;
import static org.s1queence.plugin.utils.TextUtils.getMsg;

public class RPActionPanel {
    private final String title;
    private final Inventory inv;
    private final RPWorldInteractions plugin;
    private final Map<String, Object> actions;
    private final int size;
    private final List<ItemStack> actionItemsList = new ArrayList<>();

    public RPActionPanel(@NotNull String title, @NotNull Map<String, Object> actions, int size, @NotNull RPWorldInteractions plugin) {
        this.title = title;
        this.plugin = plugin;
        this.size = size;
        this.actions = actions;
        inv = create();
    }

    public List<ItemStack> getActionItemsList() {
        return actionItemsList;
    }

    public Inventory getInventory() {
        return inv;
    }

    private final static String[] enabledActions = new String[] {
            ChatColor.DARK_GRAY + "#lookat",
            ChatColor.DARK_GRAY + "#lay",
            ChatColor.DARK_GRAY + "#sit",
            ChatColor.DARK_GRAY + "#crawl",
            ChatColor.DARK_GRAY + "#put",
            ChatColor.DARK_GRAY + "#rummage",
            ChatColor.DARK_GRAY + "#push",
            ChatColor.DARK_GRAY + "#dropblock",
            ChatColor.DARK_GRAY + "#close"
    };

    public static boolean isEnabledAction(String s1) {
        return Arrays.asList(enabledActions).contains(s1);
    }

    private Inventory create() {
        Inventory actionInv = Bukkit.createInventory(null, size, title);

        for (String key : actions.keySet()) {
            if (key.contains(".")) continue;
            Map<String, Object> itemData = ((Section)actions.get(key)).getStringRouteMappedValues(true);

            ItemStack actionItem = createItemFromConfig(itemData, true);

            if (actionItem == null) plugin.log(getMsg("alert_item_null", plugin.getTextConfig()));

            int slot;

            try {
                slot = Integer.parseInt(key);
            } catch (Exception exception) {
                slot = (int) (Math.random() * size);
            }
            if (slot < 0 || slot > size - 1) slot = (int) (Math.random() * size);
            actionInv.setItem(slot, actionItem);
            actionItemsList.add(actionItem);
        }

        for (int i = 0; i < size; i++) {
            if (actionInv.getItem(i) == null) actionInv.setItem(i, empty(plugin));
        }

        return actionInv;
    }
}