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
package com.warxim.vucsa.client.challenge.bufferoverread;

import com.warxim.vucsa.common.connection.Connection;
import com.warxim.vucsa.common.message.Message;
import com.warxim.vucsa.common.message.MessageHandler;
import com.warxim.vucsa.common.message.bufferoverread.StringListMessage;
import lombok.RequiredArgsConstructor;

/**
 * Handler for handling messages for buffer over-read challenge.
 */
@RequiredArgsConstructor
public class BufferOverreadHandler implements MessageHandler {
    private final BufferOverreadController controller;

    @Override
    public boolean supports(Message message) {
        return message instanceof StringListMessage;
    }

    @Override
    public boolean handleMessage(Connection connection, Message message) {
        controller.setItems(((StringListMessage) message).getItems());
        return true;
    }
}
