package ru.sparkcraft.eventhelper.listeners;

import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;
import ru.sparkcraft.eventhelper.activators.objects.Door;

public class EventListener implements Listener {

    @EventHandler // Button / Lever / Plate USE
    public void onButtonOrLeverOrPlateUse(@NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Activator activator = Activator.getActivator(clickedBlock.getLocation());
            if (activator != null && ((event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    (Tag.BUTTONS.isTagged(clickedBlock.getType()) ||
                            clickedBlock.getType() == Material.LEVER)) ||
                    (event.getAction() == Action.PHYSICAL && Tag.PRESSURE_PLATES.isTagged(clickedBlock.getType())))) {
                if (Tag.BUTTONS.isTagged(clickedBlock.getType()) && ((Powerable) clickedBlock.getBlockData()).isPowered()) {
                    return;
                }
                runActions(activator, event.getPlayer(), EventType.USE);
            }
        }
    }

    @EventHandler // Door USE
    public void onDoorUse(@NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && Tag.DOORS.isTagged(clickedBlock.getType())) {
            Activator activator = Activator.getActivator(Door.getTop(clickedBlock));
            if (activator != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                runActions(activator, event.getPlayer(), EventType.USE);
            }
        }
    }

    @EventHandler // Lever ON / OFF
    public void onLeverOnOff(@NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Activator activator = Activator.getActivator(clickedBlock.getLocation());
            if (activator != null && event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    clickedBlock.getType() == Material.LEVER) {
                if (!((Switch) clickedBlock.getBlockData()).isPowered()) {
                    runActions(activator, event.getPlayer(), EventType.ON);
                } else {

                    runActions(activator, event.getPlayer(), EventType.OFF);
                }
            }
        }
    }

    @EventHandler // Door OPEN / CLOSE
    public void onDoorOpenClose(@NotNull PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && Tag.DOORS.isTagged(clickedBlock.getType())) {
            Activator activator = Activator.getActivator(Door.getTop(clickedBlock));
            if (activator != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!((Openable) clickedBlock.getBlockData()).isOpen()) {
                    runActions(activator, event.getPlayer(), EventType.OPEN);
                } else {
                    runActions(activator, event.getPlayer(), EventType.CLOSE);
                }
            }
        }
    }

    @EventHandler // Region ENTER
    public void onRegionEnter(@NotNull RegionEnteredEvent event) {
        if (event.getPlayer() != null) {
            runActions(Activator.getRegionActivator(event.getRegionName()), event.getPlayer(), EventType.ENTER);
        }
    }

    @EventHandler // Region LEAVE
    public void onRegionLeave(@NotNull RegionLeftEvent event) {
        if (event.getPlayer() != null) {
            runActions(Activator.getRegionActivator(event.getRegionName()), event.getPlayer(), EventType.LEAVE);
        }
    }

    private void runActions(Activator activator, Player player, EventType eventType) {
        if (activator != null) {
            EventProcessor eventProcessor = activator.getEventProcessor(eventType);
            if (eventProcessor != null) {
                eventProcessor.run(player);
            }
        }
    }
}
