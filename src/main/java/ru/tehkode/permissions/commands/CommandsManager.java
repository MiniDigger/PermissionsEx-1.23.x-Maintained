/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.exceptions.AutoCompleteChoicesException;
import ru.tehkode.utils.StringUtils;

/**
 * @author code
 */
public class CommandsManager {

    protected Map<String, Map<CommandSyntax, CommandBinding>> listeners = new LinkedHashMap<>();
    protected PermissionsEx plugin;

    public CommandsManager(PermissionsEx plugin) {
        this.plugin = plugin;
    }

    public void register(CommandListener listener) {
        for (Method method : listener.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            Command cmdAnnotation = method.getAnnotation(Command.class);

            Map<CommandSyntax, CommandBinding> commandListeners = listeners.get(cmdAnnotation.name());
            if (commandListeners == null) {
                commandListeners = new LinkedHashMap<>();
                listeners.put(cmdAnnotation.name(), commandListeners);
            }

            commandListeners.put(new CommandSyntax(cmdAnnotation.syntax(), this), new CommandBinding(listener, method, this));
        }

        listener.onRegistered(this);
    }

    public boolean execute(CommandSender sender, org.bukkit.command.Command command, String[] args) {
        Map<CommandSyntax, CommandBinding> callMap = this.listeners.get(command.getName());

        if (callMap == null) { // No commands registered
            return false;
        }

        CommandBinding selectedBinding = null;
        int argumentsLength = 0;
        String arguments = StringUtils.implode(args, " ");

        for (Entry<CommandSyntax, CommandBinding> entry : callMap.entrySet()) {
            CommandSyntax syntax = entry.getKey();
            if (!syntax.isMatch(arguments)) {
                continue;
            }
            if (selectedBinding != null && syntax.getRegexp().length() < argumentsLength) { // match, but there already more fitted variant
                continue;
            }

            CommandBinding binding = entry.getValue();
            binding.setParams(syntax.getMatchedArguments(arguments));
            selectedBinding = binding;
        }

        if (selectedBinding == null) { // there is fitting handler
            sender.sendMessage(ChatColor.RED + "Error in command syntax. Check command help.");
            return true;
        }

        // Check permission
        if (sender instanceof Player) { // this method are not public and required permission
            if (!selectedBinding.checkPermissions((Player) sender)) {
                plugin.getLogger().warning("User " + sender.getName() + " tried to access chat command \""
                        + command.getName() + " " + arguments
                        + "\", but doesn't have permission to do this.");
                sender.sendMessage(ChatColor.RED + "Sorry, you don't have enough permissions.");
                return true;
            }
        }

        try {
            selectedBinding.call(this.plugin, sender, selectedBinding.getParams());
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof AutoCompleteChoicesException) {
                AutoCompleteChoicesException autocomplete = (AutoCompleteChoicesException) e.getTargetException();
                sender.sendMessage("Autocomplete for <" + autocomplete.getArgName() + ">:");
                sender.sendMessage("    " + StringUtils.implode(autocomplete.getChoices(), "   "));
            } else {
                throw new RuntimeException(e.getCause());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("There is bogus command handler for " + command.getName() + " command. (Is appropriate plugin is update?)");
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            } else {
                e.printStackTrace();
            }
        }

        return true;
    }

    public List<CommandBinding> getCommands() {
        List<CommandBinding> commands = new LinkedList<>();

        for (Map<CommandSyntax, CommandBinding> map : this.listeners.values()) {
            commands.addAll(map.values());
        }

        return commands;
    }
}
