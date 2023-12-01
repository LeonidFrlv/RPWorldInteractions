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
        if (plugin.isLookAtSound()) receiver.playSound(receiver.getLocation(), "rpwi.lookat", 0.9f, 1.0f);
    }

    public static void sendEntityViewToPlayer(Player receiver, String eType, RPWorldInteractions plugin) {
        String eView = ofNullable(plugin.getLookAtConfig().getString(String.join(".", "default_entities", eType))).orElse(getMsg("lookat.no_entity_view", plugin.getTextConfig()));
        receiver.sendMessage(getMsg("lookat.entity_view_text", plugin.getTextConfig()) + ChatColor.RESET + eView);
        if (plugin.isLookAtSound()) receiver.playSound(receiver.getLocation(), "rpwi.lookat", 0.9f, 1.0f);
    }

    public static List<String> getClearedFormatList(List<String> toParse) {
        toParse.replaceAll(row -> ("§r§f" + row.replace("&", "").replace("§", "")));
        return toParse;
    }

    private static String removeFirstSpace(String str) {
        return str.indexOf(" ") == 0 ? str.replaceFirst(" ", "") : str;
    }

    public static List<String> getRowsList(String toParse, int maxRowLength) {
        List<String> stringedCharacters = new ArrayList<>();
        StringBuilder character = new StringBuilder();

        for (int i = 0; i < toParse.length(); i++) {
            Character c1 = toParse.charAt(i);
            Character next = i != toParse.length() - 1 ? toParse.charAt(i + 1) : null;

            if (next != null && c1.equals(' ') || !Character.isLetterOrDigit(c1) && !c1.equals('\'')) {
                if (character.length() != 0) stringedCharacters.add(character.toString());
                character = new StringBuilder();
                stringedCharacters.add(c1.toString());
                continue;
            }

            character.append(c1);

            if (i == toParse.length() - 1) stringedCharacters.add(character.toString());
        }

        character = new StringBuilder();
        List<String> buffer = new ArrayList<>();

        if (maxRowLength < 5) maxRowLength = 13;

        for (int i = 0; i < stringedCharacters.size(); i++) {
            String current = stringedCharacters.get(i);
            String next = i != stringedCharacters.size() - 1 ? stringedCharacters.get(i + 1) : null;
            if (current.length() > maxRowLength) {
                for (int k = 0; k < current.length(); k++) {
                    Character c1 = current.charAt(k);
                    if (character.length() == maxRowLength) {
                        buffer.add(removeFirstSpace(character.toString()));
                        character = new StringBuilder();
                    }

                    character.append(c1);
                    if (k == character.length() - 1) buffer.add(removeFirstSpace(character.toString()));
                }
                continue;
            }

            if (character.length() + current.length() > maxRowLength) {
                buffer.add(removeFirstSpace(character.toString()));

                character = new StringBuilder();
            }

            if (next != null && next.length() == 1 && !Character.isLetterOrDigit(next.toCharArray()[0])) {
                character.append(current).append(next);
                i++;
                if (i == stringedCharacters.size() - 1) buffer.add(removeFirstSpace(character.toString()));
                continue;
            }

            character.append(current);
            if (i == stringedCharacters.size() - 1) buffer.add(removeFirstSpace(character.toString()));
        }

        return buffer;
    }
}
