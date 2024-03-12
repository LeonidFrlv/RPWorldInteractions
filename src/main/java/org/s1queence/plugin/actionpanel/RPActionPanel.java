package org.s1queence.plugin.actionpanel;

import de.tr7zw.changeme.nbtapi.NBTItem;
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
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.libs.block.implementation.Section;

import java.io.IOException;
import java.util.*;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1TextUtils.*;
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
            plugin.getServer().getConsoleSender().sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"incorrect_inv_size", plugin.getName()));
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

    private void insertViewToItemLore(String viewType, ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        String view = ofNullable(plugin.getLookAtConfig().getString(String.join(".", "players", ((Player)holder).getName(), viewType))).orElse(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.no_" + viewType, plugin.getName()));
        List<String> lore = ofNullable(im.getLore()).orElse(new ArrayList<>());

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

        if (actions == null) return actionInv;

        for (String key : actions.keySet()) {
            if (key.contains(".")) continue;
            Map<String, Object> itemData = ((Section)actions.get(key)).getStringRouteMappedValues(true);

            ItemStack actionItem = createItemFromMap(itemData);
            Object action_type = itemData.get("action_type");

            if (actionItem == null || !(action_type instanceof String) || ActionItemUUID.fromString((String) action_type) == null) {
                consoleLog(getConvertedTextFromConfig(plugin.getTextConfig(),"alert_item_null", plugin.getName()), plugin);
                continue;
            }

            int slot;

            try {
                slot = Integer.parseInt(key);
            } catch (Exception exception) {
                slot = (int) (Math.random() * size);
            }
            if (slot < 0 || slot > size - 1) slot = (int) (Math.random() * size);

            NBTItem nbtItem = new NBTItem(actionItem);
            nbtItem.setString("rpwi_action_type", (String) action_type);
            nbtItem.applyNBT(actionItem);

            String uuid = getActionUUID(actionItem);
            String holderName = holder != null ? ((Player)holder).getName() : null;

            if (uuid != null && uuid.equals(ActionItemUUID.VIEW.toString()) && holderName != null) {
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