// @formatter:off
/*
 * FaqEntry.java - data model for FAQ entries
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

/**
 * A FAQ entry.
 * 
 * @see Category
 * @author Matteo Panella
 */
public final class FaqEntry {
    public static final String QUESTION_COLUMN = "q";
    public static final String ANSWER_COLUMN = "a";

    public final int id;
    public final String category;
    public final String question;
    public final String answer;

    public FaqEntry(int id, final String category, final String question,
            final String answer) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FaqEntry)) {
            return false;
        }
        FaqEntry other = (FaqEntry) o;
        return this.id == other.id && this.category.equals(other.category)
                && this.question.equals(other.question)
                && this.answer.equals(other.answer);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Integer.valueOf(id).hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + question.hashCode();
        result = 31 * result + answer.hashCode();
        return result;
    }
}
