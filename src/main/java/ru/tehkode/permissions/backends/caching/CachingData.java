package ru.tehkode.permissions.backends.caching;

import ru.tehkode.permissions.PermissionsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Data backend implementing a simple cache
 */
public abstract class CachingData implements PermissionsData {

    private final Executor executor;
    protected final Object lock;
    private Map<String, List<String>> permissions;
    private Map<String, Map<String, String>> options;
    private Map<String, List<String>> parents;
    private volatile Set<String> worlds;

    public CachingData(Executor executor, Object lock) {
        this.executor = executor;
        this.lock = lock;
    }

    protected void execute(final Runnable run) {
        executor.execute(() -> {
            synchronized (lock) {
                run.run();
            }
        });
    }

    protected abstract PermissionsData getBackingData();

    protected void loadPermissions() {
        synchronized (lock) {
            this.permissions = new HashMap<>(getBackingData().getPermissionsMap());
        }
    }

    protected void loadOptions() {
        synchronized (lock) {
            this.options = new HashMap<>();
            getBackingData().getOptionsMap().entrySet().stream().forEach((e) -> {
                this.options.put(e.getKey(), new HashMap<>(e.getValue()));
            });
        }
    }

    protected void loadInheritance() {
        synchronized (lock) {
            this.parents = new HashMap<>(getBackingData().getParentsMap());
        }
    }

    protected void clearCache() {
        synchronized (lock) {
            permissions = null;
            options = null;
            parents = null;
            clearWorldsCache();
        }
    }

    @Override
    public void load() {
        synchronized (lock) {
            getBackingData().load();
            loadInheritance();
            loadOptions();
            loadPermissions();
        }
    }

    @Override
    public String getIdentifier() {
        return getBackingData().getIdentifier();
    }

    @Override
    public List<String> getPermissions(String worldName) {
        if (permissions == null) {
            loadPermissions();
        }
        List<String> ret = permissions.get(worldName);
        return ret == null ? Collections.<String>emptyList() : Collections.unmodifiableList(ret);
    }

    @Override
    public void setPermissions(List<String> permissions, final String worldName) {
        if (this.permissions == null) {
            loadPermissions();
        }
        final List<String> safePermissions = new ArrayList<>(permissions);
        execute(() -> {
            clearWorldsCache();
            getBackingData().setPermissions(safePermissions, worldName);
        });
        this.permissions.put(worldName, safePermissions);
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        if (permissions == null) {
            loadPermissions();
        }

        Map<String, List<String>> ret = new HashMap<>();
        permissions.entrySet().stream().forEach((e) -> {
            ret.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        });
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public Set<String> getWorlds() {
        Set<String> worldsLocal = this.worlds;
        if (worldsLocal == null) {
            synchronized (lock) {
                this.worlds = worldsLocal = getBackingData().getWorlds();
            }
        }
        return worldsLocal;
    }

    protected void clearWorldsCache() {
        worlds = null;
    }

    @Override
    public String getOption(String option, String worldName) {
        if (options == null) {
            loadOptions();
        }
        Map<String, String> worldOpts = options.get(worldName);
        if (worldOpts == null) {
            return null;
        }
        return worldOpts.get(option);
    }

    @Override
    public void setOption(final String option, final String value, final String world) {
        if (options == null) {
            loadOptions();
        }
        execute(() -> {
            getBackingData().setOption(option, value, world);
        });
        if (options != null) {
            Map<String, String> optionsMap = options.get(world);
            if (optionsMap == null) {
                // TODO Concurrentify
                optionsMap = new HashMap<>();
                options.put(world, optionsMap);
                clearWorldsCache();
            }
            if (value == null) {
                optionsMap.remove(option);
            } else {
                optionsMap.put(option, value);
            }
        }
    }

    @Override
    public Map<String, String> getOptions(String worldName) {
        if (options == null) {
            loadOptions();
        }
        Map<String, String> opts = options.get(worldName);
        return opts == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(opts);
    }

    @Override
    public Map<String, Map<String, String>> getOptionsMap() {
        if (options == null) {
            loadOptions();
        }
        Map<String, Map<String, String>> ret = new HashMap<>();
        options.entrySet().stream().forEach((e) -> {
            ret.put(e.getKey(), Collections.unmodifiableMap(e.getValue()));
        });
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public List<String> getParents(String worldName) {
        if (parents == null) {
            loadInheritance();
        }

        List<String> worldParents = parents.get(worldName);
        return worldParents == null ? Collections.<String>emptyList() : worldParents;
    }

    @Override
    public void setParents(final List<String> rawParents, final String worldName) {
        if (this.parents == null) {
            loadInheritance();
        }
        final List<String> safeParents = new ArrayList<>(rawParents);
        execute(() -> {
            getBackingData().setParents(safeParents, worldName);
        });
        this.parents.put(worldName, Collections.unmodifiableList(safeParents));
    }

    @Override
    public boolean isVirtual() {
        return getBackingData().isVirtual();
    }

    @Override
    public void save() {
        execute(() -> {
            getBackingData().save();
        });
    }

    @Override
    public void remove() {
        synchronized (lock) {
            getBackingData().remove();
            clearCache();
        }
    }

    @Override
    public Map<String, List<String>> getParentsMap() {
        if (parents == null) {
            loadInheritance();
        }
        Map<String, List<String>> ret = new HashMap<>();
        parents.entrySet().stream().forEach((e) -> {
            ret.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        });
        return Collections.unmodifiableMap(ret);
    }
}
