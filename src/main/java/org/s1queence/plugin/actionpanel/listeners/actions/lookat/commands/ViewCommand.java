package org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import java.io.IOException;
import java.util.Arrays;

import static org.s1queence.plugin.utils.TextUtils.getMsg;
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

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMsg("player_not_found", plugin));
            return true;
        }

        boolean isHasBypass = sender.hasPermission("rpwi.perms.viewBypass");
        boolean isSelf = target.equals(sender);

        if (action.equals("get")) {
            if (!isSelf && !sender.hasPermission("rpwi.perms.getOtherView") && !isHasBypass) {
                sender.sendMessage(getMsg("no_permission_alert", plugin));
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
            sender.sendMessage(getMsg("no_permission_alert", plugin));
            return true;
        }

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

            String senderMsg = getMsg("lookat." + viewType + "_add", plugin);
            sender.sendMessage(senderMsg);

            if (!isSelf) target.sendMessage(getMsg("lookat.view_change_alert", plugin));

            lookAtCfg.set(String.join(".", "players", target.getName(), viewType), permText.toString());
            try {
                lookAtCfg.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        if (action.equals("remove")) {
            if (args.length != 3) return false;

            String senderMsg = getMsg("lookat." + viewType + "_remove", plugin);
            sender.sendMessage(senderMsg);

            if (!isSelf) target.sendMessage(getMsg("lookat.view_change_alert", plugin));

            lookAtCfg.set(String.join(".", "players", target.getName(), viewType), null);
        }

        try {
            lookAtCfg.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
