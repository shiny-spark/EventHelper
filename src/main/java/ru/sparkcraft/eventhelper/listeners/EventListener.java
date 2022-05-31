package ru.sparkcraft.eventhelper.listeners;

import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;
import ru.sparkcraft.eventhelper.activators.objects.Door;

public class EventListener implements Listener {

    // ON, OFF, USE, OPEN, CLOSE, PUT, TAKE, ENTER, LEAVE

    @EventHandler // Button / Lever / Plate USE
    public void onButtonOrLeverOrPlateUse(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Activator activator = Activator.getActivator(clickedBlock.getLocation());
            if (activator != null && ((event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    (clickedBlock.getType().name().endsWith("_BUTTON") ||
                            clickedBlock.getType() == Material.LEVER)) ||
                    (event.getAction() == Action.PHYSICAL && clickedBlock.getType().name().endsWith("_PLATE")))) {

                runActions(activator, event.getPlayer(), EventType.USE);
            }
        }
    }

    @EventHandler // Door USE
    public void onDoorUse(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getType().name().endsWith("_DOOR")) {
            Activator activator = Activator.getActivator(Door.getTop(clickedBlock));
            if (activator != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                runActions(activator, event.getPlayer(), EventType.USE);
            }
        }
    }

    @EventHandler // Lever ON / OFF
    public void onLeverOnOff(PlayerInteractEvent event) {
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
    public void onDoorOpenClose(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getType().name().endsWith("_DOOR")) {
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
    public void onRegionEnter(RegionEnteredEvent event) {
        runActions(Activator.getActivator(event.getRegion().getId()), event.getPlayer(), EventType.ENTER);
    }

    @EventHandler // Region LEAVE
    public void onRegionLeave(RegionLeftEvent event) {
        runActions(Activator.getActivator(event.getRegion().getId()), event.getPlayer(), EventType.LEAVE);
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
