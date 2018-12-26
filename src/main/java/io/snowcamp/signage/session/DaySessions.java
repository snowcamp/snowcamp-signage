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
import java.util.Collection;
import java.util.List;

public final class DaySessions {
    private final DayOfWeek day;
    private final List<Session> sessions;

    public DaySessions(final DayOfWeek day, final Collection<Session> sessions) {
        this.day = requireNonNull(day);
        this.sessions = requireNonNull(List.copyOf(sessions));
    }

    public DayOfWeek day() {
        return day;
    }

    public List<Session> sessions() {
        return sessions;
    }
}
