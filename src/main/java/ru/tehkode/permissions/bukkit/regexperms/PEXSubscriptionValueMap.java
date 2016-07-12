package ru.tehkode.permissions.bukkit.regexperms;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PEXSubscriptionValueMap implements Map<Permissible, Boolean> {

    private final String permission;
    final Map<Permissible, Boolean> backing;
    private final PEXPermissionSubscriptionMap outer;

    public PEXSubscriptionValueMap(String permission, Map<Permissible, Boolean> backing, final PEXPermissionSubscriptionMap outer) {
        this.outer = outer;
        this.permission = permission;
        this.backing = backing;
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return backing.containsKey(key) || (key instanceof Permissible && ((Permissible) key).isPermissionSet(permission));
    }

    @Override
    public boolean containsValue(Object value) {
        return backing.containsValue(value);
    }

    @Override
    public Boolean put(Permissible key, Boolean value) {
        return backing.put(key, value);
    }

    @Override
    public Boolean remove(Object key) {
        return backing.remove(key);
    }

    @Override
    public void putAll(Map<? extends Permissible, ? extends Boolean> m) {
        backing.putAll(m);
    }

    @Override
    public void clear() {
        backing.clear();
    }

    @Override
    public Boolean get(Object key) {
        if (key instanceof Permissible) {
            Permissible p = (Permissible) key;
            if (p.isPermissionSet(permission)) {
                return p.hasPermission(permission);
            }
        }
        return backing.get(key);
    }

    @Override
    public Set<Permissible> keySet() {
        Collection<? extends Player> players = outer.plugin.getServer().getOnlinePlayers();
        Set<Permissible> pexMatches = new HashSet<>(players.size());
        players.stream().filter((player) -> (player.hasPermission(permission))).forEach((player) -> {
            pexMatches.add(player);
        });
        return Sets.union(pexMatches, backing.keySet());
    }

    @Override
    public Collection<Boolean> values() {
        return backing.values();
    }

    @Override
    public Set<Map.Entry<Permissible, Boolean>> entrySet() {
        return backing.entrySet();
    }
}

