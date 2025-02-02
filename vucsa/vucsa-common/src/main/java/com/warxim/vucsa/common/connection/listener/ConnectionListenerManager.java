/*
 * Vulnerable Client-Server Application (VuCSA)
 *
 * Copyright (C) 2021 Michal Válka
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.warxim.vucsa.common.connection.listener;

import com.warxim.vucsa.common.connection.Connection;
import com.warxim.vucsa.common.listener.ListenerManager;

/**
 * Listener manager that allows modules to register their own listener (aggregates listeners).
 * <p>Based on {@link ListenerManager}</p>
 */
public final class ConnectionListenerManager extends ListenerManager<ConnectionListener> implements ConnectionListener {
    @Override
    public void onConnectionStart(Connection connection) {
        parallelCall(listener -> listener.onConnectionStart(connection));
    }

    @Override
    public void onConnectionStop(Connection connection) {
        parallelCall(listener -> listener.onConnectionStop(connection));
    }
}