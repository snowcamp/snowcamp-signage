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
package io.snowcamp.signage.sched;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

import com.google.api.services.sheets.v4.Sheets;

import io.snowcamp.signage.gsuite.Row;
import io.snowcamp.signage.session.Session;
import io.snowcamp.signage.session.SessionRowParser;

public final class SchedParser implements SessionRowParser {
    private static final String RANGE = "Sessions!B9:O";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("M/dd/yyyy h:mm a");
    private static final int TITLE_INDEX = 0;
    private static final int ROOM_INDEX = 13;
    private static final int START_TIME_INDEX = 2;
    private static final int END_TIME_INDEX = 3;
    private static final int SPEAKERS_INDEX = 8;
    private static final String SPEAKERS_SEPARATOR = "; ";
    private final Sheets googleSheets;

    public SchedParser(final Sheets googleSheets) {
        this.googleSheets = googleSheets;
    }

    @Override
    public Stream<Session> parseSessions(final String spreadSheetId) {
        requireNonNull(spreadSheetId);
        try {
            return googleSheets.spreadsheets()
                               .values()
                               .get(spreadSheetId, RANGE)
                               .execute()
                               .getValues()
                               .stream()
                               .map(this::mapToRow)
                               .filter(this::isSessionRow)
                               .map(this::mapToSession)
                               ;
        } catch (IOException e) { // FIXME
            e.printStackTrace();
        }

        return List.<Session>of().stream();
    }

    private Row mapToRow(final List<Object> row) {
        return new Row(row);
    }

    private boolean isSessionRow(final Row row) {
        requireNonNull(row);
        return row.size() == 14;
    }

    private Session mapToSession(final Row row) {
        requireNonNull(row);
        return new Session.Builder()
                          .title(row.getAsString(TITLE_INDEX))
                          .speakers(mapToSpeakers(row))
                          .room(row.getAsString(ROOM_INDEX))
                          .start(row.getAsLocalDateTime(START_TIME_INDEX, TIME_FORMATTER))
                          .end(row.getAsLocalDateTime(END_TIME_INDEX, TIME_FORMATTER))
                          .build();
    }

    private List<String> mapToSpeakers(final Row row) {
        return Optional.ofNullable(row.getAsString(SPEAKERS_INDEX))
                       .map(speakers -> speakers.split(SPEAKERS_SEPARATOR))
                       .map(Arrays::stream)
                       .map(speakers -> speakers.map(String::toLowerCase).map(WordUtils::capitalize).collect(toList()))
                       .orElse(List.of());
    }
}
