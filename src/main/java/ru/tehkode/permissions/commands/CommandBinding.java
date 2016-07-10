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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class CommandBinding {

    protected Object object;
    protected Method method;
    protected Map<String, String> params = new HashMap<>();
    private final CommandsManager outer;

    public CommandBinding(Object object, Method method, final CommandsManager outer) {
        this.outer = outer;
        this.object = object;
        this.method = method;
    }

    public Command getMethodAnnotation() {
        return this.method.getAnnotation(Command.class);
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public boolean checkPermissions(Player player) {
        PermissionManager manager = PermissionsEx.getPermissionManager();
        String permission = this.getMethodAnnotation().permission();
        if (permission.contains("<")) {
            for (Map.Entry<String, String> entry : this.getParams().entrySet()) {
                if (entry.getValue() != null) {
                    permission = permission.replace("<" + entry.getKey() + ">", entry.getValue().toLowerCase());
                }
            }
        }
        return manager.has(player, permission);
    }

    public void call(Object... args) throws Exception {
        this.method.invoke(object, args);
    }
}

