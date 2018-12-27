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

import static io.snowcamp.signage.gsuite.GSuiteFactories.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

import io.snowcamp.signage.sched.SchedParser;
import io.snowcamp.signage.session.SessionRowParser;
import io.snowcamp.signage.session.SessionsParser;

public final class App {
    private static final String GSLIDES_URL = "https://docs.google.com/presentation/d/";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // https://docs.google.com/presentation/d/<gslides_id>
        String templatePresentationId = "<your_template_gslide_id>";
        // https://docs.google.com/spreadsheets/d/<gsheet_id>
        String spreadsheetId = "<your_sched_gsheet_id>";

        final SessionRowParser scheds = new SchedParser(googleSheets());
        final SessionsParser sessionsParser = new SessionsParser(scheds);
        final SlidesGenerator slidesGenerator = new SlidesGenerator(googleSlides(), googleDrive());

        sessionsParser.parse(spreadsheetId)
                      .map(daySessions -> slidesGenerator.generateSlides(templatePresentationId, daySessions))
                      .forEach(t -> t.map(id -> GSLIDES_URL + id)
                                     .onSuccess(url -> System.out.printf("presentation successfully generated at %s\n",
                                      url))
                                     .onFailure(err -> System.err.printf("failed to generate the presentation %s: \n",
                                                                         err)));

        System.out.println("That's it!");
    }
}
