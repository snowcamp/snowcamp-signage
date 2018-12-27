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
package io.snowcamp.signage;

import static java.time.format.TextStyle.FULL;
import static java.util.Locale.FRENCH;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.text.WordUtils.capitalize;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationResponse;
import com.google.api.services.slides.v1.model.DeleteObjectRequest;
import com.google.api.services.slides.v1.model.DuplicateObjectRequest;
import com.google.api.services.slides.v1.model.DuplicateObjectResponse;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.Response;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import io.snowcamp.signage.session.DaySessions;
import io.snowcamp.signage.session.Session;
import io.vavr.Value;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Try;

public final class SlidesGenerator {
    private static final DateTimeFormatter HOURS_FORMATTER = DateTimeFormatter.ofPattern("HH'h'mm");
    private static final String TITLE_VARIABLE = "{{ title }}";
    private static final String ROOM_VARIABLE = "{{ room }}";
    private static final String SPEAKERS_VARIABLE = "{{ speakers }}";
    private static final String HOURS_VARIABLE = "{{ hours }}";
    private static final String SPEAKERS_SEPARATOR = " & ";
    private final Slides googleSlides;
    private final Drive googleDrive;

    public SlidesGenerator(final Slides googleSlides, final Drive googleDrive) {
        this.googleSlides = requireNonNull(googleSlides);
        this.googleDrive = requireNonNull(googleDrive);
    }

    public Try<String> generateSlides(final String templatePresentationId, final DaySessions daySessions) {
        requireNonNull(templatePresentationId);
        requireNonNull(daySessions);

        return Try.ofCallable(() -> generate(templatePresentationId, daySessions));
    }

    private String generate(final String templatePresentationId, final DaySessions daySessions) throws IOException {
        final String presentationId = copyPresentation(templatePresentationId, presentationName(daySessions));

        final String templateSlideId = templateSlideId(presentationId);
        final List<Session> sessions = daySessions.sessions();
        final Stream<String> slideIds = duplicateSlides(presentationId, templateSlideId, sessions.size());

        return replaceTemplateVariables(presentationId, slideIds, sessions);
    }

    private String replaceTemplateVariables(final String presentationId,
                                            final Stream<String> slideIds,
                                            final List<Session> sessions) throws IOException {
        final List<Request> replaceRequests =
                slideIds.zipWith(sessions, SlidesGenerator::replaceTextRequests)
                        .flatMap(Value::toStream)
                        .toList();

        final BatchUpdatePresentationRequest batchReplaceRequests =
                new BatchUpdatePresentationRequest().setRequests(replaceRequests.asJava());
        googleSlides.presentations().batchUpdate(presentationId, batchReplaceRequests).execute();

        return presentationId;
    }

    private static List<Request> replaceTextRequests(final String slideId, final Session session) {
        return List.of(replaceTextRequest(slideId, TITLE_VARIABLE, session.title()),
                       replaceTextRequest(slideId, ROOM_VARIABLE, session.room()),
                       replaceTextRequest(slideId, SPEAKERS_VARIABLE, formatSpeakers(session.speakers())),
                       replaceTextRequest(slideId, HOURS_VARIABLE, formatTime(session)));
    }

    private Stream<String> duplicateSlides(final String presentationId,
                                           final String slideId,
                                           final int slidesNumber) throws IOException {
        List<Request> requests = Stream.range(0, slidesNumber)
                                             .map(i -> duplicateSlideRequest(slideId))
                                             .toList();
        requests = requests.append(deleteSlideRequest(slideId));

        final BatchUpdatePresentationRequest batchRequests =
                new BatchUpdatePresentationRequest().setRequests(requests.asJava());
        final BatchUpdatePresentationResponse responses =
                googleSlides.presentations().batchUpdate(presentationId, batchRequests).execute();

        return Stream.ofAll(responses.getReplies())
                        .map(Response::getDuplicateObject)
                        .filter(Objects::nonNull)
                        .map(DuplicateObjectResponse::getObjectId);
    }

    private String presentationName(final DaySessions daySessions) {
        return capitalize(daySessions.day().getDisplayName(FULL, FRENCH));
    }

    private String copyPresentation(String sourcePresentationId, String copyName) {
        try {
            File copyMetadata = new File().setName(copyName);
            File presentationCopyFile = googleDrive.files().copy(sourcePresentationId, copyMetadata).execute();
            return presentationCopyFile.getId();
        } catch (Exception e) {
            e.printStackTrace(); // FIXME
        }

        return null;
    }

    private String templateSlideId(final String presentationId) throws IOException {
        final Presentation presentation = googleSlides.presentations().get(presentationId).execute();
        final java.util.List<Page> slides = presentation.getSlides();

        Preconditions.checkState(!slides.isEmpty(), "no slide for presentationId [" + presentationId + "]");
        return slides.get(0).getObjectId();
    }

    private static Request duplicateSlideRequest(final String templateSlideId) {
        return new Request().setDuplicateObject(new DuplicateObjectRequest().setObjectId(templateSlideId));
    }

    private static Request deleteSlideRequest(final String templateSlideId) {
        return new Request().setDeleteObject(new DeleteObjectRequest().setObjectId(templateSlideId));
    }

    private static Request replaceTextRequest(final String slideId, final String textToReplace, final String text) {
        return new Request().setReplaceAllText(
                new ReplaceAllTextRequest()
                        .setPageObjectIds(java.util.List.of(slideId)).setReplaceText(text)
                        .setContainsText(new SubstringMatchCriteria().setMatchCase(true).setText(textToReplace)));
    }

    private static String formatSpeakers(final List<String> speakers) {
        return Joiner.on(SPEAKERS_SEPARATOR).join(speakers);
    }

    private static String formatTime(final Session session) {
        return session.start().format(HOURS_FORMATTER) + " - " + session.end().format(HOURS_FORMATTER);
    }

}
