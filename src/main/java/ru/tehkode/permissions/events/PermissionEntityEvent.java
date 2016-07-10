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
package ru.tehkode.permissions.events;

import org.bukkit.event.HandlerList;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import java.util.UUID;
import ru.tehkode.permissions.PermissionsEntityType;

/**
 * @author t3hk0d3
 */
public class PermissionEntityEvent extends PermissionEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
    protected transient PermissionEntity entity;
    protected PermissionsEntityAction action;
    protected PermissionsEntityType type;
    protected String entityIdentifier;

    public PermissionEntityEvent(UUID sourceUUID, PermissionEntity entity, PermissionsEntityAction action) {
        super(sourceUUID);
        this.entity = entity;
        this.entityIdentifier = entity.getIdentifier();
        this.type = entity.getType();
        this.action = action;
    }

    public PermissionsEntityAction getAction() {
        return this.action;
    }

    public PermissionEntity getEntity() {
        if (entity == null) {
            switch (type) {
                case GROUP:
                    entity = PermissionsEx.getPermissionManager().getGroup(entityIdentifier);
                    break;
                case USER:
                    entity = PermissionsEx.getPermissionManager().getUser(entityIdentifier);
                    break;
            }
        }
        return entity;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public PermissionsEntityType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
