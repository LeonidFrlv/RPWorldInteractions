package org.s1queence.plugin.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.s1queence.plugin.RPWorldInteractions.PLUGIN_TITLE;

public class TextUtils {
    public static String getMsg(String path, YamlDocument config) {
        String msg = config.getString(path);

        if (msg == null)  {
            String nullMsgError = ofNullable(config.getString("msg_is_null")).orElse("&6%plugin% FATAL ERROR." + " We recommend that you delete the text.yml file from the plugin folder and use reload config.");
            return ChatColor.translateAlternateColorCodes('&', nullMsgError.replace("%plugin%", PLUGIN_TITLE).replace("%msg_path%", path));
        }

        return (ChatColor.translateAlternateColorCodes('&', msg.replace("%plugin%", PLUGIN_TITLE)));
    }

    public static String insertPlayerName(String str, String playerName) {
        return ChatColor.translateAlternateColorCodes('&', str.replace("%username%", playerName));
    }


    public static String getProgressBarMsg(String path, String pb, String percent, RPWorldInteractions plugin) {
        String msg = plugin.getTextConfig().getString(path);
        return (ChatColor.translateAlternateColorCodes('&', msg.replace("%progress_bar%", pb).replace("%percent%", ChatColor.getByChar(plugin.getOptionsConfig().getString("progress_bar.percent_color")) + percent)));
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

    public static void sendPlayerViewToPlayer(Player receiver, String holderName, RPWorldInteractions plugin) {
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        String permView = ofNullable(lookAtConfig.getString(String.join(".", "players", holderName, "perm"))).orElse(getMsg("lookat.no_perm", plugin.getTextConfig()));
        String tempView = ofNullable(lookAtConfig.getString(String.join(".", "players", holderName, "temp"))).orElse(getMsg("lookat.no_temp", plugin.getTextConfig()));
        receiver.sendMessage(getMsg("lookat.name_text", plugin.getTextConfig()) + ChatColor.RESET + holderName);
        receiver.sendMessage(getMsg("lookat.perm_view_text", plugin.getTextConfig()) + ChatColor.RESET + permView);
        receiver.sendMessage(getMsg("lookat.temp_view_text", plugin.getTextConfig()) + ChatColor.RESET + tempView);
        if (plugin.isLookAtSound()) receiver.playSound(receiver.getLocation(), "rpwi.lookat", 0.7f, 1.0f);
    }

    public static List<String> getRowsListFromString(String toParse, int maxRowLength) {
        List<String> buffer = new ArrayList<>();

        if (maxRowLength <= 5) maxRowLength = 14;
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < toParse.length(); i++) {
            char current = toParse.charAt(i);
            if (i % maxRowLength == 0 && i != 0) {
                buffer.add(row.toString());
                row = new StringBuilder();
            }
            row.append(current);
            if (i == toParse.length() - 1) buffer.add(row.toString());
        }
        return buffer;
    }
}
