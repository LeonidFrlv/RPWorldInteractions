package org.s1queence.plugin.actionpanel;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.s1queence.plugin.RPWorldInteractions;

import java.io.IOException;
import java.util.*;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1TextUtils.createItemFromMap;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.getActionUUID;
import static org.s1queence.plugin.utils.BarrierClickListener.empty;
import static org.s1queence.plugin.utils.TextUtils.*;

public class RPActionPanel {
    private final String title;
    private final Inventory inv;
    private final RPWorldInteractions plugin;
    private final Map<String, Object> actions;
    private int size;
    private final List<ItemStack> actionItemsList = new ArrayList<>();
    private final InventoryHolder holder;

    public RPActionPanel(@Nullable InventoryHolder holder, @NotNull RPWorldInteractions plugin) {
        YamlDocument cfg = plugin.getActionInventoryConfig();
        this.plugin = plugin;
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("action_inv.title"));
        size = cfg.getInt("action_inv.size");
        if (size != 54 && size != 9 && size != 27) {
            size = 54;
            cfg.set("action_inv.size", 54);
            try {
                cfg.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            plugin.getServer().getConsoleSender().sendMessage(getTextFromCfg("incorrect_inv_size", plugin.getTextConfig()));
        }


        this.holder = holder;
        actions = cfg.getSection("action_inv.actions").getStringRouteMappedValues(true);
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
            ChatColor.DARK_GRAY + "#close",
            ChatColor.DARK_GRAY + "#notify",
            ChatColor.DARK_GRAY + "#view"
    };

    public static boolean isEnabledAction(String s1) {
        return Arrays.asList(enabledActions).contains(s1);
    }

    private void insertViewToItemLore(String viewType, ItemStack is) {
        ItemMeta im = is.getItemMeta();
        String view = ofNullable(plugin.getLookAtConfig().getString(String.join(".", "players", ((Player)holder).getName(), viewType))).orElse(getTextFromCfg("lookat.no_" + viewType, plugin.getTextConfig()));
        List<String> lore = ofNullable(is.getItemMeta().getLore()).orElse(new ArrayList<>());

        if (lore.isEmpty()) return;

        List<String> rowsList = getClearedFormatList(getRowsList(view, 30));

        String special = "%" + viewType + "_view%";
        int startIndex = 0;
        for (int i = 0; i < lore.size(); i++) {
            String loreRow = lore.get(i);
            if (loreRow.contains(special)) {
                lore.set(i, loreRow.replace(special, rowsList.get(0)));
                startIndex = i + 1;
                break;
            }
        }

        for (int i = 1; i < rowsList.size(); i++) {
            lore.add(startIndex++, ChatColor.translateAlternateColorCodes('&', rowsList.get(i)));
        }

        im.setLore(lore);
        is.setItemMeta(im);
    }

    private Inventory create() {
        Inventory actionInv = Bukkit.createInventory(holder, size, title);

        for (String key : actions.keySet()) {
            if (key.contains(".")) continue;
            Map<String, Object> itemData = ((Section)actions.get(key)).getStringRouteMappedValues(true);

            ItemStack actionItem = createItemFromMap(itemData);

            if (actionItem == null) {
                plugin.log(getTextFromCfg("alert_item_null", plugin.getTextConfig()));
                continue;
            }

            int slot;

            try {
                slot = Integer.parseInt(key);
            } catch (Exception exception) {
                slot = (int) (Math.random() * size);
            }
            if (slot < 0 || slot > size - 1) slot = (int) (Math.random() * size);
            
            String uuid = getActionUUID(actionItem);
            String holderName = holder != null ? ((Player)holder).getName() : null;

            if (uuid != null && uuid.contains("#view") && holderName != null) {
                insertViewToItemLore("perm", actionItem);
                insertViewToItemLore("temp", actionItem);
            }

            actionInv.setItem(slot, actionItem);
            actionItemsList.add(actionItem);
        }

        for (int i = 0; i < size; i++) {
            if (actionInv.getItem(i) == null) actionInv.setItem(i, empty(plugin));
        }

        return actionInv;
    }
}