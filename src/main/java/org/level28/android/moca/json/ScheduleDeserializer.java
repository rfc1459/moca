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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
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
        } catch (ParseException e) {
            throw new JsonDeserializerException("Invalid session entry", e);
        }
    }

    /**
     * Pure Java reimplementation of {@link Time#parse3339(String)}.
     * <p>
     * Due to <a
     * href="http://code.google.com/p/android/issues/detail?id=16002">Issue
     * 16002</a>, {@code Time.parse3339()} leaks memory on short input. However,
     * the same leak happens <em>also</em> if the function is fed a formally
     * invalid RFC3339 timestamp.
     * <p>
     * The safest option is a full rewrite in pure Java, since JNI on Android is
     * a mess (we'd have to ship the same library for three different
     * architectures &mdash; not pretty...).
     * 
     * @param timeString
     *            a date/time specification formatted as an RFC3339 string
     * @return the same date/time specification in milliseconds since the Epoch
     * @throws ParseException
     *             if the string is formally invalid per RFC3339
     * @throws NullPointerException
     *             if the string is {@code null}
     * @throws IllegalArgumentException
     *             if the string is less than 10 characters long
     */
    private static long parseTime(String timeString) throws ParseException {
        checkNotNull(timeString, "Time input should not be null");
        final int len = timeString.length();
        checkArgument(len >= 10,
                "Time input is too short; must be at least 10 characters");
        final char[] s = timeString.toCharArray();

        final Time t = new Time();
        int n;
        boolean inUtc = false;

        // Year
        n = getChar(s, 0, 1000);
        n += getChar(s, 1, 100);
        n += getChar(s, 2, 10);
        n += getChar(s, 3, 1);
        t.year = n;

        // '-'
        checkChar(s, 4, '-');

        // Month
        n = getChar(s, 5, 10);
        n += getChar(s, 6, 1);
        --n;
        t.month = n;

        // '-'
        checkChar(s, 7, '-');

        // Day
        n = getChar(s, 8, 10);
        n += getChar(s, 9, 1);
        t.monthDay = n;

        // Check if we have a time as well
        if (len >= 19) {
            // 'T'
            checkChar(s, 10, 'T');
            t.allDay = false;

            // Hour
            n = getChar(s, 11, 10);
            n += getChar(s, 12, 1);
            int hour = n;

            // ':'
            checkChar(s, 13, ':');

            // Minute
            n = getChar(s, 14, 10);
            n += getChar(s, 15, 1);
            int minute = n;

            // ':'
            checkChar(s, 16, ':');

            // Second
            n = getChar(s, 17, 10);
            n += getChar(s, 18, 1);
            t.second = n;

            // Skip subsecond component (if any)
            int tz_index = 19;
            if (tz_index < len && s[tz_index] == '.') {
                do {
                    tz_index++;
                } while (tz_index < len && s[tz_index] >= '0'
                        && s[tz_index] <= '9');
            }

            int offset = 0;
            if (len > tz_index) {
                final char c = s[tz_index];

                switch (c) {
                case 'Z':
                    // UTC
                    offset = 0;
                    break;
                case '-':
                    offset = 1;
                    break;
                case '+':
                    offset = -1;
                    break;
                default:
                    throw new ParseException("Unexpected character", tz_index);
                }
                inUtc = true;

                if (offset != 0) {
                    // Hour
                    n = getChar(s, tz_index + 1, 10);
                    n += getChar(s, tz_index + 2, 1);
                    n *= offset;
                    hour += n;

                    // ':'
                    checkChar(s, tz_index + 3, ':');

                    // Minute
                    n = getChar(s, tz_index + 4, 10);
                    n += getChar(s, tz_index + 5, 1);
                    n *= offset;
                    minute += n;
                }
            }
            t.hour = hour;
            t.minute = minute;

            if (offset != 0) {
                t.normalize(false);
            }
        } else {
            // We don't
            t.allDay = true;
            t.hour = 0;
            t.minute = 0;
            t.second = 0;
        }

        t.weekDay = 0;
        t.yearDay = 0;
        t.isDst = -1;
        t.gmtoff = 0;

        if (inUtc) {
            t.timezone = Time.TIMEZONE_UTC;
        }

        return t.toMillis(false);
    }

    private static int getChar(final char[] s, int index, int multiplier)
            throws ParseException {
        final char c = s[index];
        if (c >= '0' && c <= '9') {
            return Character.digit(c, 10) * multiplier;
        }
        throw new ParseException("Illegal character", index);
    }

    private static void checkChar(final char[] s, int index, char expected)
            throws ParseException {
        if (s[index] != expected) {
            throw new ParseException("Unexpected character", index);
        }
    }
}
