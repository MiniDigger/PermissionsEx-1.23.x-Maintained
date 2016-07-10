package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;

public class ServerNamePermissibleInjector extends PermissibleInjector {

    protected final String serverName;

    public ServerNamePermissibleInjector(String clazz, String field, boolean copyValues, String serverName) {
        super(clazz, field, copyValues);
        this.serverName = serverName;
    }

    @Override
    public boolean isApplicable(Player player) {
        return player.getServer().getName().equalsIgnoreCase(serverName);
    }
}
