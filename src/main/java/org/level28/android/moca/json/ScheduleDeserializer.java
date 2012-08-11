// @formatter:off
/*
 * ScheduleDeserializer.java - deserializer for TMA-1 schedule API
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
import java.util.HashMap;
import java.util.Map;

import org.level28.android.moca.model.Session;

import android.text.TextUtils;
import android.text.format.Time;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

/**
 * Deserializer for TMA-1 schedule API.
 * 
 * @author Matteo Panella
 */
public class ScheduleDeserializer extends
        AbstractJsonDeserializer<Map<String, Session>> {

    private static final Time sTime = new Time();

    @Override
    public Map<String, Session> fromInputStream(InputStream in)
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

        HashMap<String, Session> result = Maps.newHashMap();

        for (JsonNode node : root) {
            Session session = parseSession(node);
            result.put(session.getId(), session);
        }

        return result;
    }

    private static Session parseSession(final JsonNode objectRoot)
            throws JsonDeserializerException {
        // Basic sanity checks
        if (objectRoot == null || objectRoot.isNull()) {
            throw new JsonDeserializerException("null objectRoot");
        }
        if (!objectRoot.isObject()) {
            throw new JsonDeserializerException(
                    "objectRoot is not a JSON object");
        }

        JsonNode node;
        Session result = new Session();

        try {
            // Session id (required)
            node = objectRoot.path("id");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'id' is missing or invalid");
            }
            result.setId(node.textValue());

            // Session title (required)
            node = objectRoot.path("title");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'title' is missing or invalid");
            }
            result.setTitle(node.textValue());

            // Session day (required)
            node = objectRoot.path("day");
            if (node.isMissingNode() || !node.isInt()) {
                throw new JsonDeserializerException(
                        "'day' is missing or invalid");
            }
            result.setDay(node.asInt());

            // Session start time (required)
            node = objectRoot.path("start");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'start' is missing or invalid");
            }
            result.setStartTime(parseTime(node.textValue()));

            // Session end time (required)
            node = objectRoot.path("end");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'end' is missing or invalid");
            }
            result.setEndTime(parseTime(node.textValue()));

            // Session hosts (required)
            node = objectRoot.path("hosts");
            if (node.isMissingNode() || !node.isArray()) {
                throw new JsonDeserializerException(
                        "'hosts' is missing or invalid");
            }
            final ArrayList<String> hosts = new ArrayList<String>(node.size());
            for (JsonNode hostsSubNode : node) {
                if (!hostsSubNode.isTextual()
                        || "".equals(hostsSubNode.textValue())) {
                    throw new JsonDeserializerException(
                            "'hosts' children is not valid");
                }
                hosts.add(hostsSubNode.textValue());
            }
            result.setHosts(TextUtils.join(", ", hosts));

            // Session language (required)
            node = objectRoot.path("lang");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'lang' is missing or invalid");
            }
            result.setLang(node.textValue());

            // Session abstract (optional)
            node = objectRoot.path("abstract");
            if (!node.isMissingNode()) {
                result.setSessionAbstract(node.textValue());
            }

            return result;
        } catch (IllegalArgumentException e) {
            throw new JsonDeserializerException("Invalid session entry", e);
        }
    }

    private static long parseTime(String s) {
        // XXX: parse3339 on all versions of Android up to (and including) Jelly
        // Bean has some *SERIOUS* input validation issues. It HAS to be
        // replaced by a saner (albeit slower) Java implementation of RFC3339.
        // See also http://code.google.com/p/android/issues/detail?id=16002
        sTime.parse3339(s);
        return sTime.toMillis(false);
    }
}
