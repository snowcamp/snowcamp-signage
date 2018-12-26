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

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.services.slides.v1.SlidesScopes.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.slides.v1.Slides;
import com.google.common.collect.ImmutableList;

public final class GSuiteFactories {
    private static final String APPLICATION_NAME = "SnowCamp Signage Generator";
    private static final String GSLIDES_CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String GSLIDES_TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = ImmutableList.of(PRESENTATIONS, DRIVE, SPREADSHEETS_READONLY);
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private GSuiteFactories() {
    }

    public static Slides googleSlides() throws IOException, GeneralSecurityException {
        return new Slides.Builder(newTrustedTransport(),
                                  JSON_FACTORY,
                                  credential(GSLIDES_CREDENTIALS_FILE_PATH, GSLIDES_TOKENS_DIRECTORY_PATH))
                         .setApplicationName(APPLICATION_NAME)
                         .build();
    }

    public static Drive googleDrive() throws IOException, GeneralSecurityException {
        return new Drive.Builder(newTrustedTransport(),
                                 JSON_FACTORY,
                                 credential(GSLIDES_CREDENTIALS_FILE_PATH, GSLIDES_TOKENS_DIRECTORY_PATH))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }

    public static Sheets googleSheets() throws IOException, GeneralSecurityException {
        return new Sheets.Builder(newTrustedTransport(),
                                  JSON_FACTORY,
                                  credential(GSLIDES_CREDENTIALS_FILE_PATH, GSLIDES_TOKENS_DIRECTORY_PATH))
                         .setApplicationName(APPLICATION_NAME)
                         .build();
    }

    private static Credential credential(final String credentialsPath, final String tokensPath)
            throws IOException, GeneralSecurityException {
        // Load client secrets.
        InputStream in = GSuiteFactories.class.getResourceAsStream(credentialsPath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensPath)))
                        .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
