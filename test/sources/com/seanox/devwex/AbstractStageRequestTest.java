/**
 * Devwex, Experimental Server Engine
 * Copyright (C) 2025 Seanox Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.seanox.devwex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.UUID;

import com.seanox.test.HttpUtils;
import com.seanox.test.HttpUtils.Authentication;

/** Abstract class to implement a stage request based test. */
abstract class AbstractStageRequestTest extends AbstractStageTest {

    /** duration of general breaks and interruption of tests */
    static final long SLEEP = 50;

    /** general timeout for I/O */
    private static final long TIMEOUT = 60 *1000;
    
    /** pattern for UUID as HTTP header */
    private static final String HTTP_HEADER_UUID = "(?s)^.*\r\nUUID:\\s*(\\S+).*$";
    
    /**
     * Dynamic pattern for access-log entries with UUID
     * @param  uuid
     * @return pattern for access-log entries with UUID 
     */
    static String ACCESS_LOG_UUID(final String uuid) {
        if (uuid == null
                || uuid.trim().isEmpty())
            throw new IllegalArgumentException("Invalid uuid");
        return "(?s)^.*(?<=(?:^|\\R)\\Q" + uuid + "\\E\\R)\\s*(\\S+.*?)\\s*(?:(?:\\R.*$)|$)";
    }   
    
    /**
     * Dynamic pattern for access-log entries with response UUID
     * @param  response
     * @return pattern for access-log entries with UUID 
     */
    static String ACCESS_LOG_RESPONSE_UUID(final String response) {
        if (response == null
                || !response.matches(HTTP_HEADER_UUID))
            return null;
        final String uuid = response.replaceAll(HTTP_HEADER_UUID, "$1");
        return ACCESS_LOG_UUID(uuid);
    }
    
    /**
     * Extends the request header is extended by a UUID, to localize it in the
     * access log.
     * @param  request
     * @return request with UUID
     * @throws Exception
     */
    private static UniqueRequest createUniqueRequest(final String request)
            throws Exception {

        String uuid = UUID.randomUUID().toString();
        if (request.matches(HTTP_HEADER_UUID))
            uuid = request.replaceAll(HTTP_HEADER_UUID, "$1");
        
        final File accessLog = AbstractStage.getRootStageAccessLog();
        Files.write(accessLog.toPath(), (uuid + "\r\n").getBytes(), StandardOpenOption.APPEND);
        
        return new UniqueRequest(request, uuid);
    }
    
    /**
     * Sends an HTTP request to a server.
     * The request header is extended by a UUID to localize it in the access log.
     * @param  address address
     * @param  request request
     * @throws IOException
     * @throws GeneralSecurityException
     */     
    static String sendRequest(final String address, final String request)
            throws Exception {
        final UniqueRequest uniqueRequest = AbstractStageRequestTest.createUniqueRequest(request);
        final String response = new String(HttpUtils.sendRequest(address, uniqueRequest.request))
                .replaceAll("(^[^\\r\\n]+)", "$1\r\nUUID: " + uniqueRequest.UUID);
        AbstractStage.getAccessStreamCapture().await(ACCESS_LOG_UUID(uniqueRequest.UUID), TIMEOUT);
        return response;
    }
    
    /**
     * Sends an HTTP request to a server.
     * The request header is extended by a UUID to localize it in the access log.
     * @param  address        address
     * @param  request        request
     * @param  authentication authentication
     * @throws IOException
     * @throws GeneralSecurityException
     */     
    static String sendRequest(final String address, final String request, final Authentication authentication)
            throws Exception {
        final UniqueRequest uniqueRequest = AbstractStageRequestTest.createUniqueRequest(request);
        final String response = new String(HttpUtils.sendRequest(address, uniqueRequest.request, authentication))
                .replaceAll("(^[^\\r\\n]+)", "$1\r\nUUID: " + uniqueRequest.UUID);
        AbstractStage.getAccessStreamCapture().await(ACCESS_LOG_UUID(uniqueRequest.UUID), TIMEOUT);
        return response;
    }
    
    /**
     * Sends an HTTP request to a server.
     * The request header is extended by a UUID to localize it in the access log.
     * @param  address address
     * @param  request request
     * @param  input   input
     * @return the received response
     * @throws IOException
     * @throws GeneralSecurityException
     */
    static String sendRequest(final String address, final String request, final InputStream input)
            throws Exception {
        final UniqueRequest uniqueRequest = AbstractStageRequestTest.createUniqueRequest(request);
        final String response = new String(HttpUtils.sendRequest(address, uniqueRequest.request, input))
                .replaceAll("(^[^\\r\\n]+)", "$1\r\nUUID: " + uniqueRequest.UUID);
        AbstractStage.getAccessStreamCapture().await(ACCESS_LOG_UUID(uniqueRequest.UUID), TIMEOUT);
        return response;
    }
    
    private static class UniqueRequest {
        
        final String request;
        final String UUID;
        
        private UniqueRequest(String request, String uuid) {
            if (!request.matches(HTTP_HEADER_UUID))
                request = request.replaceAll("(^[^\\r\\n]+)", "$1\r\nUUID: " + uuid);
            this.request = request;
            this.UUID = uuid;
        }
    }
}