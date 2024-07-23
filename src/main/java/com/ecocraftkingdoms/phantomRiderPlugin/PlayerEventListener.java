package com.example.phantomrider;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerEventListener implements Listener, CommandExecutor {
    private final JavaPlugin plugin;

    public PlayerEventListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("phantomrider.summon")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        Phantom phantom = (Phantom) player.getWorld().spawnEntity(player.getLocation(), EntityType.PHANTOM);
        phantom.setCustomName("Ridable Phantom");
        phantom.setCustomNameVisible(true);

        player.sendMessage("Phantom summoned!");
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked() instanceof Phantom) {
            Phantom phantom = (Phantom) event.getRightClicked();
            Boat boat = (Boat) phantom.getWorld().spawnEntity(phantom.getLocation(), EntityType.BOAT);
            boat.addPassenger(player);

            new PhantomControlTask(plugin, phantom, boat, player).runTaskTimer(plugin, 0L, 1L);
        }
    }
}
