package ru.sparkcraft.eventhelper.activators;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;

import java.util.*;

public abstract class Activator {

    private static final Map<String, Set<Activator>> activators = new HashMap<>();

    private final String owner;
    private final ActivatorType type;
    private final String name;
    private final Set<EventProcessor> eventProcessors = new HashSet<>();

    protected Activator(EventHelper plugin, String owner, ActivatorType type, String name) {
        this.owner = owner;
        this.type = type;
        this.name = name;

        Set<Activator> list = activators.get(owner);
        if (list != null) {
            list.add(this);
        } else {
            list = new HashSet<>();
            list.add(this);
            activators.put(owner, list);
        }

        plugin.getData().set(this.getOwner() + "." + this.getName() + ".type", this.type.name());
        plugin.saveData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activator)) return false;
        Activator activator = (Activator) o;
        return name.equals(activator.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getOwner() {
        return owner;
    }

    public ActivatorType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Set<EventProcessor> getEventProcessors() {
        return eventProcessors;
    }

    public abstract boolean addEventProcessor(EventProcessor eventProcessor);

    public EventProcessor getEventProcessor(EventType eventType) {
        return eventProcessors.stream()
                .filter(eventProcessor -> eventProcessor.getEventType().equals(eventType))
                .findFirst().orElse(null);
    }

    public boolean removeEventProcessor(EventType eventType) {
        return eventProcessors.removeIf(eventProcessor -> eventProcessor.getEventType().equals(eventType));
    }

    public static boolean removeActivator(EventHelper plugin, String owner, String name) {
        Activator activator = getActivator(owner, name);
        plugin.getData().set(owner + "." + name, null);
        plugin.saveData();
        return getActivators(owner).remove(activator);
    }

    public static Activator getActivator(String owner, String name) {
        try {
            return activators.get(owner).stream()
                    .filter(activator -> activator.getName().equals(name))
                    .findFirst().orElse(null);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Activator getActivator(Location location) {
        for (Set<Activator> list : activators.values()) {
            for (Activator activator : list) {
                if (activator instanceof HaveLocation &&
                        ((HaveLocation) activator).getLocation().equals(location)) {
                    return activator;
                }
            }
        }
        return null;
    }

    public static Set<Activator> getActivators(String owner) {
        return activators.get(owner);
    }
}
