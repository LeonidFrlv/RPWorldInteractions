package org.s1queence.plugin.actionpanel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.s1queence.plugin.utils.MyUtils.empty;

public class RPActionPanel {
    private final String title;
    private final Inventory inv;
    private final List<ItemStack> actionsLi = new ArrayList<>();

    public RPActionPanel(@NotNull String title, @NotNull Map<String, Object> actions) {
        this.title = title;
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
            Map<String, Object> item = ((MemorySection)actions.get(key)).getValues(true);

            ItemStack is = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta im = is.getItemMeta();

            String name = (String)item.get("name");
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> loreList = (List<String>)item.get("lore");
            List<String> lore = new ArrayList<>();
            if (loreList != null || !loreList.isEmpty()) {
                for (String row : loreList) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', row));
                }
            }

            String uuid = (String) item.get("uniqueId");
            lore.add(ChatColor.translateAlternateColorCodes('&', uuid));

            im.setLore(lore);
            is.setItemMeta(im);

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
            actionInv.setItem(i, empty());
        }

        return actionInv;
    }
}