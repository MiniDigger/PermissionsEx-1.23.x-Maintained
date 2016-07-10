package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;

public class ClassPresencePermissibleInjector extends PermissibleInjector {

    public ClassPresencePermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
        super(clazzName, fieldName, copyValues);
    }

    @Override
    public boolean isApplicable(Player player) {
        try {
            return Class.forName(clazzName).isInstance(player);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
