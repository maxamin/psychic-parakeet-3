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
package com.warxim.vucsa.common.message.xml;

import com.warxim.vucsa.common.message.Message;
import com.warxim.vucsa.common.message.MessageType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Message for XML challenge containing list of storage items.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class StorageMessage extends Message {
    List<StorageItem> items;

    @Builder
    public StorageMessage(int target, List<StorageItem> items) {
        super(target);
        this.items = Collections.unmodifiableList(items);
    }

    @Override
    public MessageType getType() {
        return MessageType.XML_STORAGE_MESSAGE;
    }
}
