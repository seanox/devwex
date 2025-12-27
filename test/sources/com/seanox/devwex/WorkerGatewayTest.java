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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.HttpUtils;
import com.seanox.test.HttpUtils.RequestEvent;
import com.seanox.test.Pattern;
import com.seanox.test.Timing;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class WorkerGatewayTest extends AbstractStageRequestTest {

    /**
     * Test case for acceptance.
     * Method {@code HEAD} was not defined for the CGI and the request is
     * responded with status 405. For a method {@code HEAD} the server status is
     * without content.
     * @throws Exception
     */
    @Test
    public void testAcceptance_01()
            throws Exception {
        
        final String request = "HEAD /cgi_module.con HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_405));
    }
    
    /** 
     * Test case for acceptance.
     * The file extension {@code *.con }" was defined as CGI with the module
     * {@code module.WorkerModule_A}. Thus, the module responded the request
     * with status 001.
     * @throws Exception
     */
    @Test
    public void testAcceptance_02()
            throws Exception {
        
        final String request = "GET /cgi_module.con HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("001 Test ok")));
        Assert.assertTrue(response.matches("(?s)^.*\r\nModule: module.WorkerModule_A::Service\r\n.*$"));
        Assert.assertTrue(response.matches("(?s)^.*\r\nOpts: module.WorkerModule_A \\[pa=1\\] \\[M\\]\r\n.*$"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("1")));
    } 
    
    /** 
     * Test case for acceptance.
     * The file extension {@code *.con }" was defined as CGI with the module
     * {@code module.WorkerModule_A}. Thus, the module responded the request
     * with status 001.
     * @throws Exception
     */
    @Test
    public void testAcceptance_03()
            throws Exception {
        
        final String request = "GET /cgi_module.con/1 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_404));      
    } 
    
    /** 
     * Test case for acceptance.
     * Content-Length is 25 but be sent 28 bytes but only 25 bytes must be sent
     * to the CGI. The request is responded with status 200 and an echo of the
     * request.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_04()
            throws Exception {
        
        final String request = "POST /cgi_echo.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Length: 25\r\n"
                + "\r\n"
                + "parameter=xxx&xxx=1234567890";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);   
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertEquals(25, body.length());
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request, 25)));
    }
    
    /** 
     * Test case for acceptance.
     * The CGI script responds the request with {@code HTTP/1.1 123 Test ...}.
     * So must also the response header contain {@code HTTP/1.0 123 Test ...}
     * and be logged with status 123.
     * @throws Exception
     */
    @Test
    public void testAcceptance_05()
            throws Exception {
        
        final String request = "GET /cgi_header_status_1.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("123 Test")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_SERVER));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("123")));      
    } 
    
    /** 
     * Test case for acceptance.
     * For VHD a CGI application was defined which does not exist.
     * The request is responded with status 502.
     * The error must be logged in the std_out/output.log.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_06()
            throws Exception {

        final String request = "GET /cgi_header_status_1.jsx HTTP/1.0\r\n"
                + "Host: vHd\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_502));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_SERVER));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_502));   
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        final String outputLog = AbstractStage.getOutputStreamCapture().toString().trim();
        Assert.assertTrue(outputLog.matches("(?si)^.*\\Q\"xxx.xxx\": CreateProcess error=2,\\E.*$"));
    } 
    
    /** 
     * Test case for acceptance.
     * The environment variables {@code SERVER_PORT}, {@code SERVER_PROTOCOL},
     * {@code GATEWAY_INTERFACE}, {@code CONTENT_LENGTH},
     * {@code CONTENT_TYPE}, {@code QUERY_STRING}, {@code REQUEST_METHOD} and
     * {@code REMOTE_ADDR}  must be set.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_07()
            throws Exception {
        
        String request;
        String response;
        String header;
        String body;
        
        request = "GET \\cgi_environment.jsx?parameter=SERVER_PORT,SERVER_PROTOCOL,"
               + "GATEWAY_INTERFACE,CONTENT_LENGTH,CONTENT_TYPE,QUERY_STRING,REQUEST_METHOD,"
               + "REMOTE_ADDR&a=123+456\u00A9\u00FF%00ff\\/#123 456 HTTP/1.0\r\n"
               + "Host: vHa\r\n"
               + "Content-Length: 10\r\n"
               + "Content-Type: xxx/test\r\n"
               + "\r\n"
               + "1234567890";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QSERVER_PORT=18080\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QSERVER_PROTOCOL=HTTP/1.0\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QGATEWAY_INTERFACE=CGI/1.1\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QCONTENT_LENGTH=10\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QCONTENT_TYPE=xxx/test\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QQUERY_STRING=parameter=SERVER_PORT,SERVER_PROTOCOL,GATEWAY_INTERFACE,CONTENT_LENGTH,CONTENT_TYPE,QUERY_STRING,REQUEST_METHOD,REMOTE_ADDR&a=123+456\u00A9\u00FF%00ff\\/#123\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QREQUEST_METHOD=GET\\E\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\n\\QREMOTE_ADDR=127.0.0.1\\E\r\n.*$"));
        
        request = "GET \\cgi_environment.jsx?parameter=DOCUMENT_ROOT,SERVER_SOFTWARE,REMOTE_PORT,UNIQUE_ID HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);

        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        
        Assert.assertTrue(body.matches("(?si)^.*\r\nDOCUMENT_ROOT=[^\r\n]+/stage/documents_vh_A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nSERVER_SOFTWARE=Seanox-Devwex/[^\r\n]+\r\n.*$")); 
        Assert.assertTrue(body.matches("(?si)^.*\r\nREMOTE_PORT=\\d+\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nUNIQUE_ID=[A-Z0-9]+\r\n.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * The environment variable {@code DOCUMENT_ROOT} must contain the value of
     * {@code DOCROOT} and refer to the current work directory.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_08()
            throws Exception {
        
        final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*\r\nDOCUMENT_ROOT=[^\r\n]+/stage/documents_vh_A\r\n.*$"));
    } 
    
    /** 
     * Test case for acceptance.
     * Only for modules will set the environment variable {@code MODULE_OPTS}.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_09()
            throws Exception {
        
        final String request = "GET \\cgi_environment.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertFalse(body.matches("(?si)^.*\\sMODULE_OPTS=.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * For the CGI typical environment variables {@code SCRIPT_FILENAME},
     * {@code PATH_TRANSLATED}, {@code DOCUMENT_ROOT}, {@code REQUEST_URI},
     * {@code SCRIPT_URL}, {@code SCRIPT_URI}, {@code QUERY_STRING} and
     * {@code PATH_URL} must be set correctly. The environment variables
     * {@code PATH_CONTEXT} and {@code PATH_INFO} must not be set.
     * @throws Exception
     */      
    @Test
    public void testAcceptance_10()
            throws Exception {
        
        final String request = "GET \\cgi_environment.jsx?parameter=SCRIPT_FILENAME,PATH_TRANSLATED,REQUEST_URI&%20%20A HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");

        if (File.separator.equals("\\")) {
            Assert.assertTrue(body.matches("(?si)^.*\r\nSCRIPT_FILENAME=[^\r\n]+\\\\stage\\\\documents_vh_A\\\\cgi_environment\\.jsx\r\n.*$"));
            Assert.assertTrue(body.matches("(?si)^.*\r\nPATH_TRANSLATED=[^\r\n]+\\\\stage\\\\documents_vh_A\\\\cgi_environment\\.jsx\r\n.*$"));
        } else {
            Assert.assertTrue(body.matches("(?si)^.*\r\nSCRIPT_FILENAME=[^\r\n]+/stage/documents_vh_A/cgi_environment\\.jsx\r\n.*$"));
            Assert.assertTrue(body.matches("(?si)^.*\r\nPATH_TRANSLATED=[^\r\n]+/stage/documents_vh_A/cgi_environment\\.jsx\r\n.*$"));
        }
        Assert.assertTrue(body.matches("(?si)^.*\r\nDOCUMENT_ROOT=[^\r\n]+/stage/documents_vh_A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nREQUEST_URI=\\\\cgi_environment\\.jsx\\?parameter=SCRIPT_FILENAME,PATH_TRANSLATED,REQUEST_URI&%20%20A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nSCRIPT_URL=\\\\cgi_environment\\.jsx\r\n.*$"));        
        Assert.assertTrue(body.matches("(?si)^.*\r\nSCRIPT_URI=http://vHa:18180/cgi_environment\\.jsx\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nQUERY_STRING=parameter=SCRIPT_FILENAME,PATH_TRANSLATED,REQUEST_URI&%20%20A\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nPATH_URL=/cgi_environment\\.jsx\r\n.*$"));        
        Assert.assertFalse(body.matches("(?si)^.*\r\nPATH_CONTEXT=.*$"));     
        Assert.assertFalse(body.matches("(?si)^.*\r\nPATH_INFO=.*$"));
    } 
    
    /** 
     * Test case for acceptance.
     * The CGI script responds the request with {@code HTTP/1.1 401 Test ...}.
     * The first line with the HTTP status must be built by the server.
     * The custom HTTP status must not be included in the response.
     * There are no duplicates of the HTTP status allowed.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_11()
            throws Exception {
        
        final String request = "GET /cgi_header_status_C.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("401 Unauthorized")));
        Assert.assertFalse(response.matches("(?s)^.*\r\nHTTP/1\\.0 401.*$"));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_SERVER));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));      
    }  
    
    /** 
     * Test case for acceptance.
     * The CGI script responds the request with {@code HTTP/1.1 401 Test ...}.
     * The status text is individual but is responded with the server standard
     * {@code HTTP/1.0 401 Unauthorized}.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_12()
            throws Exception {

        final String request = "GET /cgi_header_status_C.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("401 Unauthorized")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_SERVER));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_401));      
    } 
    
    /** 
     * Test case for acceptance.
     * For the CGI all request-header-parameters will be passed with the prefix
     * 'HTTP_...'. Duplicates are overwritten.
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_13()
            throws Exception {
        
        String request;
        String response;
        String header;
        String body;        
        
        request = "GET \\cgi_environment.jsx?parameter=HTTP_TEST_123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Test-123: erfolgReich\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=vHa\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_TEST_123=erfolgReich\r\n.*$"));

        request = "GET \\cgi_environment.jsx?parameter=HTTP_TEST_123 HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Test-123: erfolgReich_1\r\n"
                + "Test-123: erfolgReich_2\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=vHa\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_TEST_123=erfolgReich_2\r\n.*$"));  
        Assert.assertFalse(body.matches("(?si)^.*\r\nHTTP_TEST_123=erfolgReich_1\r\n.*$"));  
    }
    
    /** 
     * Test case for acceptance.
     * The environment variables {@code HTTP_HOST} is always set.
     * For a virtual host with the name and for a server with the IP.
     * @throws Exception
     */ 
    @Test
    public void testAcceptance_14()
            throws Exception {
        
        String request;
        String response;
        String header;
        String body;
        
        request = "GET \\cgi_environment.jsx?parameter=HTTP_HOST HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));        

        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=vHa\r\n.*$"));
        Assert.assertFalse(body.matches("(?si)^.*\r\nHTTP_HOST=127\\.0\\.0\\.1\r\n.*$"));

        request = "GET \\cgi_environment.jsx?parameter=HTTP_HOST HTTP/1.0\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);

        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        Assert.assertFalse(body.matches("(?si)^.*\r\nHTTP_HOST=vHa\r\n.*$"));
        Assert.assertTrue(body.matches("(?si)^.*\r\nHTTP_HOST=127\\.0\\.0\\.1\r\n.*$"));        
    }
    
    /** 
     * Test case for acceptance.
     * Check of the command line arguments {@code[C]} {@code[D]} {@code[N]}
     * @throws Exception
     */     
    @Test
    public void testAcceptance_15()
            throws Exception {
        
        String request = "GET /parameter.jex HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));

        response = response.replaceAll("\\\\+", "/");
        response = response.replaceAll("(?i)(>[CDN]:)[^<]+?(/stage/documents_vh_A)", "$1/...$2");
        Assert.assertTrue(response.contains(">C:/.../stage/documents_vh_A/parameter.jex<"));
        Assert.assertTrue(response.contains(">D:/.../stage/documents_vh_A/<"));
        Assert.assertTrue(response.contains(">N:parameter<"));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("200", request)));
    }
    
    /** 
     * Test case for acceptance.
     * The CGI reads the data very slowly.
     * The request is canceled with status 502.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_16()
            throws Exception {
        
        String content = "x";
        while (content.length() < 1024 *1024)
            content += content;
        
        final String request = "POST /cgi_read_slow.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "Content-Length: " + content.length() + "\r\n"
                + "\r\n"
                + content;
        
        String response = "HTTP/1.0 502 xxx\r\n\r\n";
        try {response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        } catch (IOException exception) {
        }
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_502));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_502));      
    }
    
    /** 
     * Test case for acceptance.
     * An invalid {@code DOCROOT} has been configured for VHC.
     * The server uses an alternative working directory as {@code DOCROOT}.
     * @throws Exception
     */     
    @Test
    public void testAcceptance_17()
            throws Exception {
        
        final String request = "GET \\stage\\documents\\cgi_environment.jsx HTTP/1.0\r\n"
                + "Host: vHc\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");
        
        final String stage = AbstractStage.getRootStage().getParentFile().toString().replace('\\', '/');
        Assert.assertTrue(body.matches("(?si)^.*\r\nDOCUMENT_ROOT=\\Q" + stage + "\\E\r\n.*$"));
    } 
    
    /** 
     * Test case for acceptance.
     * If the CGI response starts with {@code HTTP/STATUS}, then the server
     * responds to the request. The CGI out-stream is read completely, but not
     * sent to the client.
     * @throws Exception
     */
    @Test
    public void testAcceptance_18()
            throws Exception {

        String request;
        String response;
        String accessLog;
        
        request = "GET /cgi_header_status_2.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("123 UND NUN")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("123")));
        
        request = "GET /cgi_header_status_3.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200)); 
        
        request = "GET /cgi_header_status_4.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
        
        request = "GET /cgi_header_status_5.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
        
        request = "GET /cgi_header_status_6.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));   
        
        request = "GET /cgi_header_status_7.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));   
        
        request = "GET /cgi_header_status_8.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertFalse(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));  
        
        request = "GET /cgi_header_status_9.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("200 Success")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));   
        
        request = "GET /cgi_header_status_A.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("200 Success")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_200));
        
        request = "GET /cgi_header_status_B.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS("444 AAA BBB")));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH_DIFFUSE));
        Assert.assertFalse(response.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED_DIFFUSE));
        Assert.assertTrue(response.matches("(?si)^.*\\sBerlin.*$"));
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS("444")));
    } 
    
    /** 
     * Test case for acceptance.
     * For the CGI a timeout of 30 seconds was defined.
     * The request is responded with status 200 and is logged with status 504.
     * Reason, the header has already begun.
     * @throws Exception
     */     
    @Test(timeout=60000)
    public void testAcceptance_19()
            throws Exception {
        
        final Timing timing = Timing.create(true);
        final String request = "GET /cgi_timeout_status_200.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        timing.assertTimeIn(31000);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_504));   
    }  
    
    /** 
     * Test case for acceptance.
     * For the CGI a timeout of 30 seconds was defined.
     * The request is responded with status 504 and is logged with status 504.
     * Reason, the header has not yet started.
     * @throws Exception
     */      
    @Test(timeout=60000)
    public void testAcceptance_20()
            throws Exception {
        
        final Timing timing = Timing.create(true);
        final String request = "GET /cgi_timeout_status_504.jsx HTTP/1.0\r\n"
                + "Host: vHa\r\n"
                + "\r\n";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        timing.assertTimeIn(31000);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_504));
        
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_504));   
    }   
    
    /** 
     * Test case for acceptance.
     * The CGI response header is limited to 65535 bytes.
     * In the case of an overlength, the request is responded with status 502.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_21()
            throws Exception {

        String request;
        String response;
        String accessLog;
        
        request = "GET /cgi_header_flood_1.jsx HTTP/1.0\r\n\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_502));
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_502));   
        
        request = "GET /cgi_header_flood_2.jsx HTTP/1.0\r\n\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18080", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_502));
        
        accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_RESPONSE_UUID(response));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_502));
    } 
    
    /**
     * Test case for acceptance.
     * If the server is stopped or restarted, running CGI processes must be
     * terminated. In this example, a CGI counter is started. The CGI process is
     * running and the server is restarted. The counter must stop!
     * @throws Exception
     */
    @Test
    public void testAcceptance_22()
            throws Exception {
        
        final String uuid = UUID.randomUUID().toString();
        Files.write(AbstractStage.getRootStageAccessLog().toPath(), (uuid + "\r\n").getBytes(), StandardOpenOption.APPEND);
        
        final String request = "GET /cgi_count.jsx HTTP/1.0\r\nUUID: " + uuid + "\r\n\r\n";
        HttpUtils.sendRequest("127.0.0.1:18080", request, (RequestEvent)null);
        Thread.sleep(2500);
        final Path counterPath = Paths.get(AbstractStage.getRootStage().toString(), "documents/cgi_count.txt");
        
        final int counterContent1 = Integer.parseInt(new String(Files.readAllBytes(counterPath)));
        HttpUtils.sendRequest("127.0.0.1:18001", "RESTaRT\r\n");
        Thread.sleep(2500);
        final int counterContent2 = Integer.parseInt(new String(Files.readAllBytes(counterPath)));
        Thread.sleep(2500);
        final int counterContent3 = Integer.parseInt(new String(Files.readAllBytes(counterPath)));
        
        Assert.assertTrue(counterContent1 <= counterContent2 && counterContent1 <= counterContent3);
        Assert.assertEquals(counterContent2, counterContent3);
        
        Thread.sleep(AbstractStageRequestTest.SLEEP);
        AbstractStage.getAccessStreamCapture().await(ACCESS_LOG_UUID(uuid));
        final String accessLog = AbstractStage.getAccessStreamCapture().fetch(ACCESS_LOG_UUID(uuid));
        Assert.assertTrue(accessLog, accessLog.matches(Pattern.ACCESS_LOG_STATUS_503));
        
        Thread.sleep(2500);
        final int counterContent4 = Integer.parseInt(new String(Files.readAllBytes(counterPath)));
        Assert.assertEquals(counterContent2, counterContent4);
    }

    /** 
     * Test case for acceptance.
     * For {@code SERVER:X}, JSX was configured as XCGI.
     * The environment variables must be transferred via Std_IO.
     * For the CGI all request-header-parameters will be passed with the prefix
     * 'HTTP_...'. Duplicates are ignored, only the first parameter will be used.
     * @throws Exception
     */       
    @Test
    public void testAcceptance_23()
            throws Exception {

        final String request = "POST /cgi_echo.jsx HTTP/1.0\r\n"
                + "Content-Length: 10\r\n"
                + "AAA: A1\r\n"
                + "AAA: A1\r\n"
                + "AAA: A2\r\n"
                + "AAC: A2\r\n"
                + "\r\n"
                + "1234567890";
        final String response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_200));
        
        final String header = response.replaceAll(Pattern.HTTP_RESPONSE, "$1");
        Assert.assertTrue(header.trim().length() > 0);
        final String body = "\r\n" + response.replaceAll(Pattern.HTTP_RESPONSE, "$2");

        Assert.assertTrue(body.matches("(?s)^.*\r\nHTTP_AAA=A1\r\nHTTP_AAC=A2\r\n.*$"));
    }
    
    /** 
     * Test case for acceptance.
     * Method {@code ALL} was defined for the CGI but {@code METHODS} does this
     * not allow and the request is responded with status 403.
     * @throws Exception
     */
    @Test
    public void testAcceptance_24()
            throws Exception {
        
        String request;
        String response;
        
        request = "Get /method.php HTTP/1.0\r\n"
                + "Host: vHb\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_404));
        
        request = "Zet /method.php HTTP/1.0\r\n"
                + "Host: vHb\r\n"
                + "\r\n";
        response = AbstractStageRequestTest.sendRequest("127.0.0.1:18180", request);
        Assert.assertTrue(response.matches(Pattern.HTTP_RESPONSE_STATUS_405));
    }    
}