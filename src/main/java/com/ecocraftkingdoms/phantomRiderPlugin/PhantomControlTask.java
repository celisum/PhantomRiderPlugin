package com.example.phantomrider;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.world.entity.Mob;  // Updated from EntityInsentient to Mob
import org.bukkit.craftbukkit.mappings_1_20_R6.entity.CraftPhantom;  // Corrected package path
import org.bukkit.entity.Boat;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PhantomControlTask extends BukkitRunnable {
    private final Phantom phantom;
    private final Boat boat;
    private final Player player;
    private final ProtocolManager protocolManager;
    private final JavaPlugin plugin;
    private Vector targetVelocity;

    public PhantomControlTask(JavaPlugin plugin, Phantom phantom, Boat boat, Player player) {
        this.phantom = phantom;
        this.boat = boat;
        this.player = player;
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.targetVelocity = new Vector(0, 0, 0);
        setupPacketListener();
    }

    private void setupPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer().equals(player)) {
                    PacketContainer packet = event.getPacket();
                    float sideways = packet.getFloat().read(0);
                    float forward = packet.getFloat().read(1);

                    // Update the target velocity based on player input
                    Vector boatDirection = boat.getLocation().getDirection().multiply(forward);
                    targetVelocity = new Vector(boatDirection.getX(), 0, boatDirection.getZ());

                    // Adjust the boat's yaw for turning
                    Location boatLocation = boat.getLocation();
                    float newYaw = boatLocation.getYaw() + sideways * 5;
                    boatLocation.setYaw(newYaw);
                    boat.teleport(boatLocation);

                    // Sync phantom's yaw with the boat's yaw
                    ((CraftPhantom) phantom).getHandle().setYRot(newYaw);
                }
            }
        });
    }

    @Override
    public void run() {
        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Boat)) {
            protocolManager.removePacketListeners(plugin);
            this.cancel();
            return;
        }

        // Smoothly adjust phantom's velocity towards the target velocity
        Vector currentVelocity = phantom.getVelocity();
        Vector newVelocity = currentVelocity.clone().add(targetVelocity.clone().subtract(currentVelocity).multiply(0.1));
        phantom.setVelocity(newVelocity);

        // Ensure the phantom's yaw is continually updated to match the boat's direction
        Location phantomLocation = phantom.getLocation();
        phantomLocation.setYaw(((CraftPhantom) phantom).getHandle().getYRot());
        phantom.teleport(phantomLocation);
    }
}
