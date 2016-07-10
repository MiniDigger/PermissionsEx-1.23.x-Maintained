package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;

public class RegexPermissibleInjector extends PermissibleInjector {

    private final String regex;

    public RegexPermissibleInjector(String clazz, String field, boolean copyValues, String regex) {
        super(clazz, field, copyValues);
        this.regex = regex;
    }

    @Override
    public boolean isApplicable(Player player) {
        return player.getClass().getName().matches(regex);
    }
}

