package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.utils.FieldReplacer;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PermissibleMap for the permissions subscriptions data in Bukkit's
 * {@link PluginManager} so we can put in our own data too.
 */
public class PEXPermissionSubscriptionMap extends HashMap<String, Map<Permissible, Boolean>> {

    private static FieldReplacer<PluginManager, Map> INJECTOR;
    private static final AtomicReference<PEXPermissionSubscriptionMap> INSTANCE = new AtomicReference<>();
    private static final long serialVersionUID = 1L;

    /**
     * Inject a PEX permission subscription map into the provided plugin
     * manager. This allows some PEX functions to work with the plugin manager.
     *
     * @param plugin
     * @param manager The manager to inject into
     * @return 
     */
    @SuppressWarnings(value = "unchecked")
    public static PEXPermissionSubscriptionMap inject(PermissionsEx plugin, PluginManager manager) {
        PEXPermissionSubscriptionMap map = INSTANCE.get();
        if (map != null) {
            return map;
        }

        if (INJECTOR == null) {
            INJECTOR = new FieldReplacer<>(manager.getClass(), "permSubs", Map.class);
        }

        Map backing = INJECTOR.get(manager);
        if (backing instanceof PEXPermissionSubscriptionMap) {
            return (PEXPermissionSubscriptionMap) backing;
        }
        PEXPermissionSubscriptionMap wrappedMap = new PEXPermissionSubscriptionMap(plugin, manager, backing);
        if (INSTANCE.compareAndSet(null, wrappedMap)) {
            INJECTOR.set(manager, wrappedMap);
            return wrappedMap;
        } else {
            return INSTANCE.get();
        }
    }
    final PermissionsEx plugin;
    private final PluginManager manager;

    private PEXPermissionSubscriptionMap(PermissionsEx plugin, PluginManager manager, Map<String, Map<Permissible, Boolean>> backing) {
        super(backing);
        this.plugin = plugin;
        this.manager = manager;
    }

    /**
     * Uninject this PEX map from its plugin manager
     */
    public void uninject() {
        if (INSTANCE.compareAndSet(this, null)) {
            Map<String, Map<Permissible, Boolean>> unwrappedMap = new HashMap<>(this.size());
            for (Map.Entry<String, Map<Permissible, Boolean>> entry : this.entrySet()) {
                if (entry.getValue() instanceof PEXSubscriptionValueMap) {
                    Map<Permissible, Boolean> put = unwrappedMap.put(entry.getKey(), ((PEXSubscriptionValueMap) entry.getValue()).backing);
                }
            }
            INJECTOR.set(manager, unwrappedMap);
        }
    }

    @Override
    public Map<Permissible, Boolean> get(Object key) {
        if (key == null) {
            return null;
        }

        Map<Permissible, Boolean> result = super.get(key);
        if (result == null) {
            result = new PEXSubscriptionValueMap((String) key, new WeakHashMap<Permissible, Boolean>(), this);
            super.put((String) key, result);
        } else if (!(result instanceof PEXSubscriptionValueMap)) {
            result = new PEXSubscriptionValueMap((String) key, result, this);
            super.put((String) key, result);
        }
        return result;
    }

    @Override
    public Map<Permissible, Boolean> put(String key, Map<Permissible, Boolean> value) {
        if (!(value instanceof PEXSubscriptionValueMap)) {
            value = new PEXSubscriptionValueMap(key, value, this);
        }
        return super.put(key, value);
    }

    @Override
    public Object clone() {
        return super.clone();
    }

}
