package ru.tehkode.permissions.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.tehkode.permissions.PermissionUser;

public class PlayerEventsListener implements Listener {

    private final PermissionsEx outer;

    public PlayerEventsListener(final PermissionsEx outer) {
        this.outer = outer;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED && !outer.requiresLateUserSetup()) {
            outer.getPermissionsManager().cacheUser(event.getUniqueId().toString(), event.getName());
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        try {
            PermissionUser user = outer.getPermissionsManager().getUser(event.getPlayer());
            if (!user.isVirtual()) {
                if (!event.getPlayer().getName().equals(user.getOption("name"))) {
                    // Update name only if user exists in config
                    user.setOption("name", event.getPlayer().getName());
                }
                if (!outer.config.shouldLogPlayers()) {
                    return;
                }
                user.setOption("last-login-time", Long.toString(System.currentTimeMillis() / 1000L));
                // user.setOption("last-login-ip", event.getPlayer().getAddress().getAddress().getHostAddress()); // somehow this won't work
            }
        } catch (Throwable t) {
            ErrorReport.handleError("While login cleanup event", t);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            PermissionUser user = outer.getPermissionsManager().getUser(event.getPlayer());
            if (!user.isVirtual()) {
                if (outer.config.shouldLogPlayers()) {
                    user.setOption("last-logout-time", Long.toString(System.currentTimeMillis() / 1000L));
                }
                user.getName(); // Set name if user was created during server run
            }
            outer.getPermissionsManager().resetUser(event.getPlayer());
        } catch (Throwable t) {
            ErrorReport.handleError("While logout cleanup event", t);
        }
    }
}

