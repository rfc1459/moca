// @formatter:off
/*
 * Session.java - data model for sessions
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

package org.level28.android.moca.model;

import java.util.Locale;

import android.text.TextUtils;

/**
 * Data model for sessions.
 * 
 * @author Matteo Panella
 */
public final class Session {

    /**
     * Enumeration for session languages.
     */
    public enum Language {
        IT, EN
    }

    /**
     * Session UUID.
     */
    private String id;

    /**
     * Session title.
     */
    private String title;

    /**
     * Session day.
     */
    private int day;

    /**
     * Session start time in milliseconds since the Epoch.
     */
    private long startTime;

    /**
     * Session end time in milliseconds since the Epoch.
     */
    private long endTime;

    /**
     * Host(s) for this session.
     */
    private String hosts;

    /**
     * Language of this session.
     */
    private Language lang;

    /**
     * Session abstract (may be null).
     */
    private String sessionAbstract;

    /**
     * Create an empty session.
     */
    public Session() {
        // No-op
    }

    /**
     * Create a new session.
     * <p>
     * I know, there are too many parameters...
     */
    public Session(final String id, final String title, final int day,
            final long startTime, final long endTime, final String hosts,
            final String lang, final String sessionAbstract) {
        this.setId(id);
        this.setTitle(title);
        this.setDay(day);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
        this.setHosts(hosts);
        this.setLang(lang);
        this.setSessionAbstract(sessionAbstract);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException(
                    "Session id may not be null or empty");
        }
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException(
                    "Session title may not be null or empty");
        }
        this.title = title;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        if (day < 1 || day > 3) {
            throw new IllegalArgumentException("Session day is invalid: " + day);
        }
        this.day = day;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(final String hosts) {
        if (TextUtils.isEmpty(hosts)) {
            throw new IllegalArgumentException(
                    "Session hosts may not be null or empty");
        }
        this.hosts = hosts;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(final String lang) {
        this.lang = Language.valueOf(lang.toUpperCase(Locale.US));
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public String getSessionAbstract() {
        return sessionAbstract;
    }

    public void setSessionAbstract(final String sessionAbstract) {
        this.sessionAbstract = sessionAbstract;
    }
}
