package ru.sparkcraft.eventhelper.activators;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.objects.Function;
import ru.sparkcraft.eventhelper.activators.objects.Region;

import java.util.*;

public abstract class Activator {

    private static final Map<String, Set<Activator>> activators = new HashMap<>();
    private int id;
    private final String owner;
    private final ActivatorType type;
    private final String name;
    private final Location location;
    private final Map<EventType, EventProcessor> eventProcessors = new EnumMap<>(EventType.class);

    protected Activator(String owner, ActivatorType type, String name, Location location) {
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.location = location;

        Set<Activator> list = activators.get(owner);
        if (list != null) {
            list.add(this);
        } else {
            activators.put(owner, new HashSet<>(Collections.singleton(this)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activator activator)) return false;
        return name.equals(activator.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Location getLocation() {
        return location;
    }

    public abstract boolean addEventProcessor(EventType eventType);

    public void addEventProcessor(EventProcessor eventProcessor) {
        eventProcessors.put(eventProcessor.getEventType(), eventProcessor);
    }

    public Map<EventType, EventProcessor> getEventProcessors() {
        return eventProcessors;
    }

    public void putEventProcessor(@NotNull EventType eventType) {
        eventProcessors.computeIfAbsent(eventType, key -> {
            EventProcessor eventProcessor = new EventProcessor(this, key);
            ActivatorDAO.getInstance().saveEventProcessor(eventProcessor);
            return eventProcessor;
        });
    }

    public EventProcessor getEventProcessor(EventType eventType) {
        return eventProcessors.get(eventType);
    }

    public boolean removeEventProcessor(EventType eventType) {
        EventProcessor eventProcessor = eventProcessors.remove(eventType);
        if (eventProcessor != null) {
            ActivatorDAO.getInstance().deleteEventProcessor(eventProcessor);
            return true;
        }
        return false;
    }

    public static boolean removeActivator(String owner, String name) {
        Activator activator = getActivator(owner, name);
        if (activator != null) ActivatorDAO.getInstance().deleteActivator(activator);
        return getActivators(owner).remove(activator);
    }

    public static @Nullable Activator getActivator(String owner, String name) {
        Set<Activator> activators = getActivators(owner);
        if (activators != null) {
            for (Activator activator : activators) {
                if (activator.getName().equalsIgnoreCase(name)) {
                    return activator;
                }
            }
        }
        return null;
    }

    public static @Nullable Activator getActivator(Location location) {
        for (Set<Activator> list : activators.values()) {
            for (Activator activator : list) {
                if (activator.getLocation() != null && activator.getLocation().equals(location)) {
                    return activator;
                }
            }
        }
        return null;
    }

    public static @Nullable Activator getFunctionActivator(String functionName) {
        for (Set<Activator> list : activators.values()) {
            for (Activator activator : list) {
                if (activator instanceof Function function && function.getName().equalsIgnoreCase(functionName)) {
                    return activator;
                }
            }
        }
        return null;
    }

    public static @Nullable Activator getRegionActivator(String regionName) {
        for (Set<Activator> list : activators.values()) {
            for (Activator activator : list) {
                if (activator instanceof Region region && region.getRegionName().equalsIgnoreCase(regionName)) {
                    return activator;
                }
            }
        }
        return null;
    }

    public static Set<Activator> getActivators(String owner) {
        return activators.get(owner);
    }

    public static void reloadActivators() {
        for (Set<Activator> activatorSet : activators.values()) {
            activatorSet.clear();
        }
        activators.clear();
        ActivatorDAO.getInstance().loadDate();
    }
}
