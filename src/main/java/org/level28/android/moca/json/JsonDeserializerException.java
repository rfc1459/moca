// @formatter:off
/*
 * JsonDeserializerException.java - base exception for JSON deserialization
 * Copyright (C) 2012 Matteo Panella <morpheus@level28.org>
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
// @formatter:on
package org.level28.android.moca.json;

/**
 * Base exception for JSON deserialization.
 * 
 * @author Matteo Panella
 */
public final class JsonDeserializerException extends Exception {

    private static final long serialVersionUID = 2092214466976070680L;

    /**
     * {@inheritDoc}
     */
    public JsonDeserializerException() {
        super();
    }

    public JsonDeserializerException(String detailMessage) {
        super(detailMessage);
    }

    public JsonDeserializerException(Throwable throwable) {
        super(throwable);
    }

    public JsonDeserializerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
