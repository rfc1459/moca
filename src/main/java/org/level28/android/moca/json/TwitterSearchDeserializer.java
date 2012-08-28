// @formatter:off
/*
 * TwitterSearchDeserializer.java - JSON deserializer for Twitter search results
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.level28.android.moca.model.Tweet;
import org.level28.android.moca.model.TwitterSearchReply;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

/**
 * JSON deserializer for Twitter search results.
 * 
 * @author Matteo Panella
 */
public final class TwitterSearchDeserializer extends
        AbstractJsonDeserializer<TwitterSearchReply> {
    /** Date format used by Twitter */
    private static final String DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss Z";

    /** Date format used in Twitter Search API replies */
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            DATE_FORMAT, Locale.US);

    /**
     * Deserialize a JSON search result from the given {@link InputStream}.
     * 
     * @param in
     *            the input stream from which the JSON document should be parsed
     * @return a list of results with relevant search metadata
     * @throws JsonDeserializerException
     *             if the JSON document is invalid
     */
    @Override
    public TwitterSearchReply fromInputStream(InputStream in)
            throws JsonDeserializerException {
        JsonNode root;
        try {
            root = sJsonMapper.readTree(in);
        } catch (IOException e) {
            throw new JsonDeserializerException("Internal Jackson error: "
                    + e.getMessage(), e);
        }

        // Root should be an object
        if (!root.isObject()) {
            throw new JsonDeserializerException(
                    "Root JsonNode is not an object!");
        }

        JsonNode node, resultsArray;
        TwitterSearchReply reply = new TwitterSearchReply();

        // Get all required attributes starting with the all-important results
        // array
        resultsArray = root.path("results");
        if (resultsArray.isMissingNode() || !resultsArray.isArray()) {
            throw new JsonDeserializerException(
                    "Invalid search reply (results missing or not an array)");
        }
        // We'll get to results deserialization later, continue to hunt down
        // required search reply fields

        // Original query (not really needed, just checked for sanity reasons)
        node = root.path("query");
        if (node.isMissingNode() || !node.isTextual()) {
            throw new JsonDeserializerException("'query' missing or invalid");
        }
        reply.setQuery(node.textValue());

        // Refresh url for this query
        node = root.path("refresh_url");
        if (node.isMissingNode() || !node.isTextual()) {
            throw new JsonDeserializerException(
                    "'refresh_url' missing or invalid");
        }
        reply.setRefreshUrl(node.textValue());

        // Maximum tweet id
        node = root.path("max_id");
        if (node.isMissingNode() || !node.canConvertToLong()) {
            throw new JsonDeserializerException("'max_id' missing or invalid");
        }
        reply.setMaxId(node.asLong());

        // Starting tweet id
        node = root.path("since_id");
        if (node.isMissingNode() || !node.canConvertToLong()) {
            throw new JsonDeserializerException("'since_id' missing or invalid");
        }
        reply.setSinceId(node.asLong());

        // Results per page
        node = root.path("results_per_page");
        if (node.isMissingNode() || !node.isInt()) {
            throw new JsonDeserializerException(
                    "'results_per_page' missing or invalid");
        }
        reply.setResultsPerPage(node.intValue());

        // Current page
        node = root.path("page");
        if (node.isMissingNode() || !node.isInt()) {
            throw new JsonDeserializerException("'page' missing or invalid");
        }
        reply.setPage(node.intValue());

        // Parse all results
        ArrayList<Tweet> tweets = Lists.newArrayList();
        for (JsonNode child : resultsArray) {
            tweets.add(parseSingleTweet(child));
        }
        reply.setResults(tweets);

        // Go for optional and low-priority attributes
        reply.setCompletedIn(root.path("completed_in").asDouble(666.0));
        reply.setNextPage(root.path("next_page").textValue());

        // Aaaaand we're done :-)
        return reply;
    }

    /**
     * Parse a single tweet.
     */
    private static Tweet parseSingleTweet(JsonNode objectRoot)
            throws JsonDeserializerException {
        // Basic sanity check
        if (!objectRoot.isObject()) {
            throw new JsonDeserializerException(
                    "Tweet JsonNode is not an object");
        }
        JsonNode node;
        Tweet tweet = new Tweet();

        // When was this tweet created?
        node = objectRoot.path("created_at");
        if (node.isMissingNode() || !node.isTextual()) {
            throw new JsonDeserializerException(
                    "'created_at' missing or invalid");
        }
        try {
            tweet.setCreatedAt(mDateFormat.parse(node.textValue()));
        } catch (ParseException e) {
            throw new JsonDeserializerException(
                    "Invalid date specified in 'created_at'", e);
        }

        // Who sent it? (user handle)
        node = objectRoot.path("from_user");
        if (node.isMissingNode() || !node.isTextual()) {
            throw new JsonDeserializerException(
                    "'from_user' missing or invalid");
        }
        tweet.setFromUser(node.textValue());

        // Who sent it? (user id)
        node = objectRoot.path("from_user_id");
        if (node.isMissingNode() || !node.canConvertToLong()) {
            throw new JsonDeserializerException(
                    "'from_user_id' missing or invalid");
        }
        tweet.setFromUserId(node.asLong());

        // Tweet id
        node = objectRoot.path("id");
        if (node.isMissingNode() || !node.canConvertToLong()) {
            throw new JsonDeserializerException("'id' missing or invalid");
        }
        tweet.setId(node.asLong());

        // Profile image url - prefer https over http
        node = objectRoot.path("profile_image_url_https");
        if (node.isMissingNode() || !node.isTextual()) {
            // Fall back to http
            node = objectRoot.path("profile_image_url");
            if (node.isMissingNode() || !node.isTextual()) {
                throw new JsonDeserializerException(
                        "'profile_image_url' missing or invalid");
            }
        }
        tweet.setProfileImageUrl(node.textValue());

        // Finally: tweet contents!
        node = objectRoot.path("text");
        if (node.isMissingNode() || !node.isTextual()) {
            throw new JsonDeserializerException("'text' missing or invalid");
        }
        tweet.setText(node.textValue());

        // FIXME: entities

        // Optional fields
        // Who sent it? (display name - yes, apparently this is OPTIONAL!)
        tweet.setFromUserName(objectRoot.path("from_user_name").textValue());

        // Free-form location
        tweet.setLocation(objectRoot.path("location").textValue());

        // FIXME: geo

        // End of tweet :-)
        return tweet;
    }
}
