package org.s1queence.plugin.spots;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.plugin.spots.SpotsHandler.spots;

public class SpotsCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public SpotsCommand(RPWorldInteractions plugin) {
        this.plugin = plugin;
    }

    private boolean isNotEnabledAction(String str) {
        return !str.equals("create") && !str.equals("change") && !str.equals("get") && !str.equals("delete");
    }

    private String insertSpotName(String str, String name) {
        return str.replace("%spot_name%", name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player sender = (Player) commandSender;
        if (!sender.hasPermission("rpwi.perms.spots")) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(), "no_permission_alert", plugin.getName()));
            return true;
        }
        if (args.length == 0) return false;



        String action = args[0].toLowerCase();
        if (isNotEnabledAction(action)) return false;
        String spotName = "";

        switch (action) {
            case "create": {
                if (args.length < 3) return false;
                String name = args[1];

                if (spots.get(name) != null) {
                    sender.sendMessage(insertSpotName(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.name_is_exist", plugin.getName()), name));
                    return true;
                }

                if (name.length() > 17) {
                    sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.name_is_too_large", plugin.getName()));
                    return true;
                }

                int radius = plugin.getSpotsConfig().getInt("min_radius");
                try {
                    radius = Math.min(Math.max(radius, Integer.parseInt(args[2])), plugin.getSpotsConfig().getInt("max_radius"));
                } catch (NumberFormatException e) {
                    return false;
                }
                Location loc = sender.getLocation();
                Location center = new Location(sender.getWorld(), loc.getX(), loc.getY(), loc.getZ());

                if (args.length >= 4) {
                    StringBuilder textContent = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        if (i == args.length - 1) {
                            textContent.append(args[i]);
                            break;
                        }
                        textContent.append(args[i]).append(" ");
                    }
                    new Spot(name, radius, center, ChatColor.translateAlternateColorCodes('&', textContent.toString()), plugin);
                    spotName = name;
                    break;
                }

                new Spot(name, radius, center, plugin);
                spotName = name;
                break;
            }

            case "get": {
                if (args.length > 2) return false;

                if (spots.isEmpty()) {
                    sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.list_is_empty", plugin.getName()));
                    return true;
                }

                if (args.length == 2) {
                    String name = args[1];
                    Spot spotToGet = spots.get(name);

                    if (spotToGet == null) {
                        sender.sendMessage(insertSpotName(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.does_not_exist", plugin.getName()), name));
                        return true;
                    }

                    sender.spigot().sendMessage(spotToGet.toTextComponent(false));
                    return true;
                }

                if (spots.size() > 1) sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.list_title", plugin.getName()));
                spots.forEach((key, value) -> sender.spigot().sendMessage(value.toTextComponent(true)));
                return true;
            }

            case "delete": {
                if (args.length != 2) return false;
                String name = args[1];
                Spot spotToDelete = spots.get(name);

                if (spotToDelete == null) {
                    sender.sendMessage(insertSpotName(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.does_not_exist", plugin.getName()), name));
                    return true;
                }

                spotToDelete.delete();
                spotName = spotToDelete.getName();
                break;
            }

            case "change": {
                if (args.length < 3) return false;
                String name = args[1];
                Spot spotToChange = spots.get(name);

                if (spotToChange == null) {
                    sender.sendMessage(insertSpotName(getConvertedTextFromConfig(plugin.getTextConfig(), "spots.does_not_exist", plugin.getName()), name));
                    return true;
                }

                StringBuilder textContent = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i == args.length - 1) {
                        textContent.append(args[i]);
                        break;
                    }
                    textContent.append(args[i]).append(" ");
                }

                spotToChange.setTextContent(ChatColor.translateAlternateColorCodes('&', textContent.toString()));
                spotName = spotToChange.getName();
                break;
            }
        }

        String successMsg = insertSpotName(getConvertedTextFromConfig(plugin.getTextConfig(), "spots." + action + "_successfully", plugin.getName()), spotName);
        sender.sendMessage(successMsg);

        // ты можешь не мув ивент обрабатывать а просто создать один поток который ебашит всё время
        // и если в e.getWorld.getрядомсущности(радиус, радиус/2, радиус) появляется игрок значит ему отсылается сообщение и он добавляется в мемберсов,
        // его предмет среды меняется и всё остальное.

        // /spots create 20 Spot_Name - создаёт спот без текстового содержимого, с радиусом в 20, с именем Spot_Name
        // /spots change Spot_Name Странный дом. &lСамый &r&fстранный дом - меняет текстовое содержимое для Spot_Name спота на указанное
        // /spots get - показывает игроку все споты (только список имён, без текста или с текстом но с ... в конце, то есть обрезает) При клике по имени спота выполняется команда, которая ниже написана
        // /spots get Spot_Name - показывает игроку всю информацию о споте (радиус, текст - ты понел)
        // /spots delete Spot_Name - удаляет спот с именем Spot_Name



        return true;
    }
}
