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

/**
 * ConnectionListener allows code to listen for connection start/stop events.
 * <p>
 *     All event handlers should be called from ConnectionManager.
 * </p>
 */
public interface ConnectionListener {
    /**
     * Event for connection start.
     * @param connection Connection that started
     */
    default void onConnectionStart(Connection connection) {}

    /**
     * Event for connection stop.
     * @param connection Connection that stopped
     */
    default void onConnectionStop(Connection connection) {}
}
