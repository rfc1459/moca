// @formatter:off
/*
 * HomeDeserializer.java - JSON deserializer for Home screen contents
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.level28.android.moca.model.HomeSection;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

/**
 * JSON deserializer for Home screen contents.
 * 
 * @author Matteo Panella
 */
public class HomeDeserializer extends
        AbstractJsonDeserializer<List<HomeSection>> {

    @Override
    public List<HomeSection> fromInputStream(InputStream in)
            throws JsonDeserializerException {
        JsonNode root;
        try {
            root = sJsonMapper.readTree(in);
        } catch (IOException e) {
            throw new JsonDeserializerException("Internal Jackson error", e);
        }

        if (!root.isArray()) {
            throw new JsonDeserializerException("Root node is not an array");
        }

        ArrayList<HomeSection> result = Lists.newArrayList();
        for (JsonNode node : root) {
            if (!node.isObject()) {
                throw new JsonDeserializerException(
                        "Array element is not an object");
            }

            final JsonNode header = node.path("header");
            final JsonNode contents = node.path("contents");

            if (header.isMissingNode() || contents.isMissingNode()
                    || !header.isTextual() || !contents.isTextual()) {
                throw new JsonDeserializerException("Malformed entry");
            }

            result.add(new HomeSection(header.textValue(), contents.textValue()));
        }

        return result;
    }
}
