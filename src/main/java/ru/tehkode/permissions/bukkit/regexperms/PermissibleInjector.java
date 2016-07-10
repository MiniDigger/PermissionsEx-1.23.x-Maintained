package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class handles injection of {@link Permissible}s into {@link Player}s for
 * various server implementations.
 */
public abstract class PermissibleInjector {

    protected final String clazzName, fieldName;
    protected final boolean copyValues;

    public PermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
        this.clazzName = clazzName;
        this.fieldName = fieldName;
        this.copyValues = copyValues;
    }

    /**
     * Attempts to inject {@code permissible} into {@code player},
     *
     * @param player The player to have {@code permissible} injected into
     * @param permissible The permissible to inject into {@code player}
     * @return the old permissible if the injection was successful, otherwise
     * null
     * @throws NoSuchFieldException when the permissions field could not be
     * found in the Permissible
     * @throws IllegalAccessException when things go very wrong
     */
    public Permissible inject(Player player, Permissible permissible) throws NoSuchFieldException, IllegalAccessException {
        Field permField = getPermissibleField(player);
        if (permField == null) {
            return null;
        }
        Permissible oldPerm = (Permissible) permField.get(player);
        if (copyValues && permissible instanceof PermissibleBase) {
            PermissibleBase newBase = (PermissibleBase) permissible;
            PermissibleBase oldBase = (PermissibleBase) oldPerm;
            copyValues(oldBase, newBase);
        }

        // Inject permissible
        permField.set(player, permissible);
        return oldPerm;
    }

    public Permissible getPermissible(Player player) throws NoSuchFieldException, IllegalAccessException {
        return (Permissible) getPermissibleField(player).get(player);
    }

    private Field getPermissibleField(Player player) throws NoSuchFieldException {
        Class<?> humanEntity;
        try {
            humanEntity = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            Logger.getLogger("PermissionsEx").warning("[PermissionsEx] Unknown server implementation being used!");
            return null;
        }

        if (!humanEntity.isAssignableFrom(player.getClass())) {
            Logger.getLogger("PermissionsEx").warning("[PermissionsEx] Strange error while injecting permissible!");
            return null;
        }

        Field permField = humanEntity.getDeclaredField(fieldName);
        // Make it public for reflection
        permField.setAccessible(true);
        return permField;
    }

    private void copyValues(PermissibleBase old, PermissibleBase newPerm) throws NoSuchFieldException, IllegalAccessException {
        // Attachments
        Field attachmentField = PermissibleBase.class.getDeclaredField("attachments");
        attachmentField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> attachmentPerms = (List<Object>) attachmentField.get(newPerm);
        attachmentPerms.clear();
        @SuppressWarnings("unchecked")
        boolean addAll = attachmentPerms.addAll((List) attachmentField.get(old));
        newPerm.recalculatePermissions();
    }

    public abstract boolean isApplicable(Player player);

}

