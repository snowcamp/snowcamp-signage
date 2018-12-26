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
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class SessionsParser {
    private final SessionRowParser sessionRowParser;

    public SessionsParser(final SessionRowParser sessionRowParser) {
        this.sessionRowParser = requireNonNull(sessionRowParser);
    }

    public Stream<DaySessions> parse(final String spreadSheetId) {
        requireNonNull(spreadSheetId);
        return sessionRowParser.parseSessions(spreadSheetId)
                               .collect(groupSessionsByDay())
                               .entrySet()
                               .stream()
                               .map(this::mapToDaySessions);
    }

    private Collector<Session, ?, EnumMap<DayOfWeek, SortedSet<Session>>> groupSessionsByDay() {
        return groupingBy(Session::dayInTheWeek,
                          () -> new EnumMap<>(DayOfWeek.class),
                          toCollection(() -> new TreeSet<>(sessionComparator())));
    }

    private Comparator<Session> sessionComparator() {
        return Comparator.comparing(Session::start).thenComparing(Session::room).reversed();
    }

    private DaySessions mapToDaySessions(final Entry<DayOfWeek, SortedSet<Session>> e) {
        return new DaySessions(e.getKey(), e.getValue());
    }

}
