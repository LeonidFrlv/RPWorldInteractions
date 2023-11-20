package org.s1queence.plugin.actionpanel;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;
import org.s1queence.plugin.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.s1queence.plugin.utils.MyUtils.empty;
public class RPActionPanel {
    private final String title;
    private final Inventory inv;
    private final RPWorldInteractions plugin;
    private final List<ItemStack> actionsLi = new ArrayList<>();

    public RPActionPanel(@NotNull String title, @NotNull Map<String, Object> actions, @NotNull RPWorldInteractions plugin) {
        this.title = title;
        this.plugin = plugin;
        inv = create(actions);
    }

    public List<ItemStack> getActionsList() {
        return actionsLi;
    }

    public Inventory getInventory() {
        return inv;
    }

    private final static String[] enabledActions = new String[]{
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

    private Inventory create(Map<String, Object> actions) {
        Inventory actionInv = Bukkit.createInventory(null, 27, title);

        for (String key : actions.keySet()) {
            if (key.contains(".")) continue;
            Map<String, Object> itemData = ((Section)actions.get(key)).getStringRouteMappedValues(true);

            ItemStack is = TextUtils.createItemFromConfig(itemData, true);

            if (is == null) plugin.log(TextUtils.getMsg("alert_item_null", plugin));

            actionsLi.add(is);
        }

        ItemStack closeItem = null;
        
        for (ItemStack is : actionsLi) {
            if (ActionPanelUtil.getActionUUID(is) != null && ActionPanelUtil.getActionUUID(is).contains("#close")) {
                closeItem = is;
                continue;
            }
            actionInv.setItem(actionsLi.indexOf(is) + 9, is);
        }

        actionInv.setItem(22, closeItem);

        for (int i = 0; i < 27; i++) {
            if (actionInv.getItem(i) != null) continue;
            actionInv.setItem(i, empty(plugin));
        }

        return actionInv;
    }
}