// @formatter:off
/*
 * FaqDeserializer.java - JSON deserializer for FAQ entries
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

import org.level28.android.moca.model.FaqEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

/**
 * JSON deserializer for FAQ entries.
 * 
 * @author Matteo Panella
 */
public final class FaqDeserializer extends
        AbstractJsonDeserializer<List<FaqEntry>> {

    private int faqIdTally = 1;

    @Override
    public List<FaqEntry> fromInputStream(InputStream in)
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

        // I miss list comprehensions :-(
        ArrayList<FaqEntry> result = Lists.newArrayList();
        for (JsonNode node : root) {
            if (node == null || node.isNull()) {
                throw new JsonDeserializerException("null objectRoot");
            }
            if (!node.isObject()) {
                throw new JsonDeserializerException(
                        "objectRoot is not a JSON object");
            }

            final JsonNode categoryName = node.path("category");
            final JsonNode entries = node.path("faqs");

            if (categoryName.isMissingNode() || entries.isMissingNode()
                    || !categoryName.isTextual() || !entries.isArray()) {
                throw new JsonDeserializerException("Malformed FAQ category");
            }

            final String category = categoryName.textValue();

            for (JsonNode childNode : entries) {
                result.add(parseEntry(category, childNode));
            }
        }

        return result;
    }

    private FaqEntry parseEntry(final String currentCategory,
            JsonNode objectRoot) throws JsonDeserializerException {
        if (objectRoot == null || objectRoot.isNull()) {
            throw new JsonDeserializerException("null objectRoot");
        }
        if (!objectRoot.isObject()) {
            throw new JsonDeserializerException(
                    "objectRoot is not a JSON object");
        }

        final JsonNode q = objectRoot.path("q");
        final JsonNode a = objectRoot.path("a");
        if (q.isMissingNode() || a.isMissingNode() || !q.isTextual()
                || !a.isTextual()) {
            throw new JsonDeserializerException("Malformed FAQ entry");
        }

        return new FaqEntry(faqIdTally++, currentCategory, q.textValue(),
                a.textValue());
    }
}
