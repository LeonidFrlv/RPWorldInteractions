package org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.libs.YamlDocument;

import java.io.IOException;
import java.util.Arrays;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.plugin.utils.TextUtils.sendPlayerViewToPlayer;

public class ViewCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public ViewCommand(RPWorldInteractions pl) {this.plugin = pl;}

    private final String[] enabledActions = new String[] {
            "get",
            "set",
            "remove"
    };

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return true;
        if (args.length < 2) return false;
        String action = args[0].toLowerCase();
        if (!Arrays.asList(enabledActions).contains(action)) return false;
        Player sender = (Player) commandSender;

        YamlDocument textConfig = plugin.getTextConfig();
        String pluginName = plugin.getName();

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getConvertedTextFromConfig(textConfig,"player_not_found", pluginName));
            return true;
        }

        boolean isHasBypass = sender.hasPermission("rpwi.perms.viewBypass");
        boolean isSelf = target.equals(sender);

        if (action.equals("get")) {
            if (!isSelf && !sender.hasPermission("rpwi.perms.getOtherView") && !isHasBypass) {
                sender.sendMessage(getConvertedTextFromConfig(textConfig,"no_permission_alert", pluginName));
                return true;
            }
            sendPlayerViewToPlayer(sender, target.getName(), plugin);
            return true;
        }

        if (args.length < 3) return false;

        String viewType = args[2].toLowerCase();
        if (!viewType.equals("perm") && !viewType.equals("temp")) return false;
        YamlDocument lookAtCfg = plugin.getLookAtConfig();

        boolean isNotCanChangePermView = viewType.equals("perm") && !sender.hasPermission("rpwi.perms.permView") && !isHasBypass;
        boolean isNotCanChangeTempView = viewType.equals("temp") && !sender.hasPermission("rpwi.perms.tempView") && !isHasBypass;

        if (isNotCanChangePermView || isNotCanChangeTempView || (!isSelf && !isHasBypass)) {
            sender.sendMessage(getConvertedTextFromConfig(textConfig,"no_permission_alert", pluginName));
            return true;
        }


        String senderMsg = getConvertedTextFromConfig(textConfig,"lookat." + viewType + "_" + action, pluginName);
        sender.sendMessage(senderMsg);
        if (!isSelf) target.sendMessage(getConvertedTextFromConfig(textConfig,"lookat.view_change_alert", pluginName));

        if (action.equals("set")) {
            if (args.length == 3) return false;

            StringBuilder permText = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i == args.length - 1) {
                    permText.append(args[i]);
                    break;
                }
                permText.append(args[i]).append(' ');
            }

            lookAtCfg.set(String.join(".", "players", target.getName(), viewType), permText.toString());
        }

        if (action.equals("remove")) {
            if (args.length != 3) return false;

            lookAtCfg.set(String.join(".", "players", target.getName(), viewType), null);
        }

        try {
            lookAtCfg.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Entity entity : target.getNearbyEntities(15.0d, 7.5d, 15.0d)) {
            if (!(entity instanceof Player)) continue;
            Player p = (Player) entity;
            if (p.equals(sender)) continue;
            p.sendMessage(getConvertedTextFromConfig(textConfig, "lookat.some_changes_near", pluginName));
            p.playSound(p.getLocation(), plugin.getOptionsConfig().getString("sounds.look_at"), 1.0f, 1.0f);
        }

        plugin.getPlayersAndPanels().replace(target.getUniqueId().toString(), new RPActionPanel(target, plugin));

        return true;
    }
}
