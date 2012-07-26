// @formatter:off
/*
 * AbstractJsonDeserializer.java - base class for JSON deserializers
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

import java.io.InputStream;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for JSON deserializers.
 * 
 * @author Matteo Panella
 * @param <D>
 *            JSON resource produced by this deserializer
 */
public abstract class AbstractJsonDeserializer<D> {

    /** Static (thread-safe) factory for JSON objects */
    protected static final ObjectMapper sJsonMapper;

    /**
     * Deserialize a JSON resource from the given {@link InputStream}.
     * 
     * @param in
     *            the input stream from which the JSON document should be parsed
     * @return a parsed JSON resource
     * @throws JsonDeserializerException
     *             if the JSON document is invalid
     */
    public abstract D fromInputStream(InputStream in)
            throws JsonDeserializerException;

    // Static initializer for the ObjectMapper
    static {
        sJsonMapper = new ObjectMapper();
        // Annotation processing on pre-ICS devices is horribly slow
        sJsonMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
    }
}
