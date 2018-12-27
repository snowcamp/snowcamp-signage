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

import static io.vavr.Function1.identity;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.util.Comparator;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;

public final class SessionsParser {
    private final SessionRowParser sessionRowParser;

    public SessionsParser(final SessionRowParser sessionRowParser) {
        this.sessionRowParser = requireNonNull(sessionRowParser);
    }

    public Stream<DaySessions> parse(final String spreadSheetId) {
        requireNonNull(spreadSheetId);
        return sessionRowParser.parseSessions(spreadSheetId)
                               .toStream()
                               .flatMap(identity())
                               .groupBy(Session::dayInTheWeek)
                               .map(this::mapToSession)
                               .toStream();
    }

    private DaySessions mapToSession(final Tuple2<DayOfWeek, Stream<Session>> daySessions) {
        return new DaySessions(daySessions._1, daySessions._2.toList().sorted(sessionComparator()));
    }
    private Comparator<Session> sessionComparator() {
        return comparing(Session::start).thenComparing(Session::room).reversed();
    }
}
