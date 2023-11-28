package org.s1queence.plugin.actionpanel.listeners.actions.coop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.actionpanel.utils.ProgressBar;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.utils.TextUtils.*;

public class CoopRPAction {
    private final Player player;
    private final Player target;
    private final RPWorldInteractions plugin;
    private final ItemStack launchItem;
    private final Float initialPlayerSpeed;
    private final Float initialTargetSpeed;
    public CoopRPAction(@NotNull Player player, @NotNull Player target, @NotNull RPWorldInteractions plugin) {
        this.player = player;
        this.target = target;
        this.plugin = plugin;
        this.launchItem = player.getInventory().getItemInMainHand();
        this.initialPlayerSpeed = player.getWalkSpeed();
        this.initialTargetSpeed = target.getWalkSpeed();
    }

    protected void cancelAction(String path) {
        plugin.getPlayersInAction().remove(player);
        String playerName = player.getName();
        sendActionBarMsg(player, getMsg(path + ".action_bar_both", plugin));
        sendActionBarMsg(target, getMsg(path + ".action_bar_both", plugin));

        String targetTitle = insertPlayerName(getMsg(path + ".target.title", plugin), playerName);
        String targetSubtitle = insertPlayerName(getMsg(path + ".target.subtitle", plugin), playerName);

        player.sendTitle(getMsg(path + ".player.title", plugin), getMsg(path + ".player.subtitle", plugin), 0, 75, 20);
        target.sendTitle(targetTitle, targetSubtitle, 0, 75, 20);
        player.setWalkSpeed(initialPlayerSpeed);
        target.setWalkSpeed(initialTargetSpeed);
        player.closeInventory();
        target.closeInventory();
    }

    protected void setWalkSpeedForAll() {
        player.setWalkSpeed(RPWorldInteractions.PLAYER_RP_ACTION_SPEED);
        target.setWalkSpeed(RPWorldInteractions.PLAYER_RP_ACTION_SPEED);
    }

    protected String getCountDownProgressBar(int initial, int current) {
        return ProgressBar.getProgressBar(initial - current, initial, plugin);
    }

    protected boolean isActionCanceled() {
        boolean isSneaking = player.isSneaking() || target.isSneaking();
        boolean isLaunchItemInitial = player.getInventory().getItemInMainHand().equals(launchItem);
        boolean isTargetNearby = player.getNearbyEntities(1.65f, 0.5f, 1.65f).contains(target);
        boolean isInAction = plugin.isPlayerInAction(player) || plugin.isPlayerInAction(target);
        boolean isOnline = player.isOnline() || target.isOnline();
        boolean isDead = player.isDead() || target.isDead();

        return isSneaking || !isLaunchItemInitial || !isTargetNearby || !isInAction || !isOnline || isDead;
    }

    protected void actionCountDown(@NotNull Integer seconds, @NotNull String textPath) {
        setWalkSpeedForAll();
        if (plugin.isPlayerInAction(player) || plugin.isPlayerInAction(target)) return;
        if (!plugin.isActionCoolDownExpired(player) || !plugin.isActionCoolDownExpired(target)) return;
        plugin.getPlayersInAction().put(player, target);
        plugin.getItemActionCoolDown().put(player, target);
        String playerName = player.getName();


        final float ACTION_TIME = seconds * 20;
        new BukkitRunnable() {
            int currentTicks = (int)ACTION_TIME;
            @Override
            public void run() {
                if (isActionCanceled()) {
                    cancelAction(textPath + ".preprocess.cancel");
                    plugin.getItemActionCoolDown().remove(player);
                    cancel();
                    return;
                }

                if (currentTicks % 20 == 0) {
                    player.sendTitle(getMsg(textPath + ".preprocess.every_second.player.title", plugin), getMsg(textPath + ".preprocess.every_second.player.subtitle", plugin), 0, 100, 0);

                    String targetTitle = insertPlayerName(getMsg(textPath + ".preprocess.every_second.target.title", plugin), playerName);
                    String targetSubtitle = insertPlayerName(getMsg(textPath + ".preprocess.every_second.target.subtitle", plugin), playerName);

                    target.sendTitle(targetTitle, targetSubtitle, 0, 100, 0);
                }

                if (currentTicks == 0) {
                    plugin.getItemActionCoolDown().remove(player);
                    sendActionBarMsg(player, getMsg(textPath + ".preprocess.complete.action_bar_both", plugin));
                    sendActionBarMsg(target, getMsg(textPath + ".preprocess.complete.action_bar_both", plugin));

                    String targetTitle = insertPlayerName(getMsg(textPath + ".preprocess.complete.target.title", plugin), playerName);
                    String targetSubtitle = insertPlayerName(getMsg(textPath + ".preprocess.complete.target.subtitle", plugin), playerName);

                    target.sendTitle(targetTitle, targetSubtitle, 0, 75, 15);
                    player.sendTitle(getMsg(textPath + ".preprocess.complete.player.title", plugin), getMsg(textPath + ".preprocess.complete.player.subtitle", plugin), 0, 75, 15);

                    cancel();
                    return;
                }

                currentTicks--;
                player.closeInventory();
                target.closeInventory();
                sendActionBarMsg(player, getProgressBarMsg(textPath + ".preprocess.every_tick.action_bar_both", getCountDownProgressBar((int)ACTION_TIME, currentTicks),  "" + (int)(((ACTION_TIME - currentTicks) / ACTION_TIME) * 100), plugin));
                sendActionBarMsg(target, getProgressBarMsg(textPath + ".preprocess.every_tick.action_bar_both", getCountDownProgressBar((int)ACTION_TIME, currentTicks),  "" + (int)(((ACTION_TIME - currentTicks) / ACTION_TIME) * 100), plugin));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    protected Player getPlayer() {return player;}
    protected Player getTarget() {return target;}
    protected RPWorldInteractions getPlugin() {return plugin;}
}
