package ru.sparkcraft.eventhelper.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sparkcraft.eventhelper.activators.*;

public class EventListener implements Listener {

    // ON, OFF, USE, OPEN, CLOSE, PUT, TAKE, ENTER, LEAVE


    @EventHandler // Button || Lever USE
    public void onButtonOrLeverUse(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && Activator.getActivator(clickedBlock.getLocation()) != null &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                (clickedBlock.getType().name().endsWith("_BUTTON") || clickedBlock.getType() == Material.LEVER)) {
            runActions(clickedBlock.getLocation(), event.getPlayer(), EventType.USE);
        }
    }

    @EventHandler // Lever ON || OFF
    public void onLeverOnOff(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && Activator.getActivator(clickedBlock.getLocation()) != null &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                clickedBlock.getType() == Material.LEVER) {

            if (!((Switch) clickedBlock.getBlockData()).isPowered()) {
                runActions(clickedBlock.getLocation(), event.getPlayer(), EventType.ON);
            } else {
                runActions(clickedBlock.getLocation(), event.getPlayer(), EventType.OFF);
            }
        }
    }

/*    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && getActivator(clickedBlock.getLocation()) != null &&

                ((event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        clickedBlock.getType().name().endsWith("_BUTTON")) ||

                        (clickedBlock.getType() == Material.LEVER) ||

                        (event.getAction() == Action.PHYSICAL &&
                                clickedBlock.getType().name().endsWith("_PLATE")))) {

            for (EventProcessor eventProcessor : getActivator(clickedBlock.getLocation()).getEventProcessors()) {
                eventProcessor.run(event.getPlayer());
            }
        }
    }*/

    private void runActions(Location location, Player player, EventType eventType) {
        Activator activator = Activator.getActivator(location);
        if (activator != null) {
            EventProcessor eventProcessor = activator.getEventProcessor(eventType);
            if (eventProcessor != null) {
                eventProcessor.run(player);
            }
        }
    }
}
