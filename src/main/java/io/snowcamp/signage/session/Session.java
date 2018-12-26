/*
 * Copyright (C) 2018 SnowCamp.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.snowcamp.signage.session;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

public final class Session {
    public static final class Builder {
        private String title;
        private String room;
        private List<String> speakers;
        private LocalDateTime start;
        private LocalDateTime end;

        public Session build() {
            return new Session(title, speakers, start, end, room);
        }

        public Builder end(final LocalDateTime end) {
            this.end = end;
            return this;
        }

        public Builder speakers(final List<String> speakers) {
            this.speakers = speakers;
            return this;
        }

        public Builder start(final LocalDateTime start) {
            this.start = start;
            return this;
        }

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder room(final String room) {
            this.room = room;
            return this;
        }
    }

    private final String title;
    private final List<String> speakers;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String room;

    public Session(final String title,
                   final List<String> speakers,
                   final LocalDateTime start,
                   final LocalDateTime end,
                   final String room) {
        this.title = requireNonNull(title);
        this.speakers = requireNonNull(speakers);
        this.start = requireNonNull(start);
        this.end = requireNonNull(end);
        this.room = requireNonNull(room);
    }

    public String title() {
        return title;
    }

    public List<String> speakers() {
        return speakers;
    }

    public LocalDateTime start() {
        return start;
    }

    public LocalDateTime end() {
        return end;
    }

    public DayOfWeek dayInTheWeek() {
        return start.getDayOfWeek();
    }

    public String room() {
        return room;
    }

    @Override
    public String toString() {
        return "Session{"
                + "title='"
                + title
                + '\''
                + ", speakers="
                + speakers
                + ", start="
                + start
                + ", end="
                + end
                + ", room='"
                + room
                + '\''
                + '}';
    }
}
