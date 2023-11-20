package org.s1queence.plugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.s1queence.plugin.RPWorldInteractions.PLUGIN_TITLE;

public class TextUtils {
    public static String getMsg(String path, RPWorldInteractions plugin) {
        String msg = plugin.getTextConfig().getString(path);

        if (msg == null)  {
            String nullMsgError = plugin.getTextConfig().getString("msg_is_null") == null ? "&6%plugin% FATAL ERROR." + " We recommend that you delete the text.yml file from the plugin folder and use reload config." : plugin.getTextConfig().getString("msg_is_null");
            return ChatColor.translateAlternateColorCodes('&', nullMsgError.replace("%plugin%", PLUGIN_TITLE).replace("%msg_path%", path));
        }

        return (ChatColor.translateAlternateColorCodes('&', msg.replace("%plugin%", PLUGIN_TITLE)));
    }

    public static String insertPlayerName(String str, String playerName) {
        return ChatColor.translateAlternateColorCodes('&', str.replace("%username%", playerName));
    }


    public static String getProgressBarMsg(String path, String pb, String percent, RPWorldInteractions plugin) {
        String msg = plugin.getTextConfig().getString(path);
        return (ChatColor.translateAlternateColorCodes('&', msg.replace("%progress_bar%", pb).replace("%percent%", ChatColor.getByChar(plugin.getTextConfig().getString("progress_bar.percent_color")) + percent)));
    }

    private static boolean isPropertyNonDefaultOrNull(String property) {
        return property != null && !property.equalsIgnoreCase("default");
    }

    public static ItemStack createItemFromConfig(Map<String, Object> mappedItem, boolean isHasActionUniqueId) {
        Material material = Material.getMaterial(mappedItem.get("material").toString());
        if (material == null) return null;

        ItemStack is = new ItemStack(material);

        ItemMeta im = is.getItemMeta();

        String name = (String)mappedItem.get("name");
        if (isPropertyNonDefaultOrNull(name))
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        try {
            String cmd = mappedItem.get("cmd").toString();
            if (isPropertyNonDefaultOrNull(name))
                im.setCustomModelData(Integer.parseInt(cmd));
        } catch (NumberFormatException ignored) {

        }

        try {
            String loreString = mappedItem.get("lore").toString();
            if (isPropertyNonDefaultOrNull(loreString)) {
                List<String> loreList = (List<String>)mappedItem.get("lore");
                if (loreList != null || !loreList.isEmpty()) {
                    List<String> lore = new ArrayList<>();
                    for (String row : loreList) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', row));
                    }

                    if (isHasActionUniqueId) {
                        String uuid = (String) mappedItem.get("uniqueId");
                        lore.add(ChatColor.translateAlternateColorCodes('&', uuid));
                    }
                    im.setLore(lore);
                }
            }
        } catch (Exception ignored) {

        }

        is.setItemMeta(im);
        return is;
    }
}
