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
package io.snowcamp.signage.gsuite;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public final class Row {
    private final List<Object> row;

    public Row(final List<Object> row) {
        this.row = requireNonNull(row);
    }

    public int size() {
        return row.size();
    }

    public String getAsString(final int index) {
        return getAsOptionalString(index)
                   .orElse(null);
    }

    public LocalDateTime getAsLocalDateTime(final int index, final DateTimeFormatter formatter) {
        requireNonNull(formatter);
        return getAsOptionalString(index)
                   .map(s -> LocalDateTime.parse(s, formatter))
                   .orElse(null);
    }

    private Optional<String> getAsOptionalString(final int index) {
        return Optional.ofNullable(row.get(index))
                       .map(Object::toString);
    }

}
