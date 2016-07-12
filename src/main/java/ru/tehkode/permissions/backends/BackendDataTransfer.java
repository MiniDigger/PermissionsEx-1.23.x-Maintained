package ru.tehkode.permissions.backends;

import ru.tehkode.permissions.PermissionsData;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

import java.util.List;

/**
 * Helper class to hold static methods relating to import/export between
 * backends. Should be refactored to be interface methods in jdk8
 */
public class BackendDataTransfer {

    private static void transferBase(PermissionsData from, PermissionsData to) {
        from.getPermissionsMap().entrySet().stream().forEach((entry) -> {
            to.setPermissions(entry.getValue(), entry.getKey());
        });

        from.getOptionsMap().entrySet().stream().forEach((entry) -> {
            entry.getValue().entrySet().stream().forEach((option) -> {
                to.setOption(option.getKey(), option.getValue(), entry.getKey());
            });
        });

        to.setParents(from.getParents(null), null);
        from.getWorlds().stream().forEach((world) -> {
            List<String> groups = from.getParents(world);
            if (!(groups == null || groups.isEmpty())) {
                to.setParents(groups, world);
            }
        });
    }

    public static void transferGroup(PermissionsGroupData from, PermissionsGroupData to) {
        transferBase(from, to);
    }

    public static void transferUser(PermissionsUserData from, PermissionsUserData to) {
        transferBase(from, to);
    }

    private BackendDataTransfer() {
        // NO NO NO
    }
}
