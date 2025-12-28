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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.seanox.test.Executor;
import com.seanox.test.Executor.Worker;
import com.seanox.test.HttpUtils;
import com.seanox.test.Pattern;
import com.seanox.test.SystemInfo;
import com.seanox.test.Timing;

/** Test cases for {@link com.seanox.devwex.Worker}. */
public class PerformanceTest extends AbstractStageRequestTest {
    
    private static String createFailedTestWorkerInfo(final Executor executor) {
        String result = "";
        for (final Worker worker : executor.getWorkers(Worker.Filter.FAILED, Worker.Filter.INTERRUPTED))
            result += worker.toString();
        return result;
    }
    
    /** 
     * Test case for acceptance.
     * Measures the execution time of 1000 (40 x 25) request.
     * The first load test is slower because the server first increases the
     * number of threads.
     * @throws Exception
     */
    @Test
    public void testAcceptance_1()
            throws Exception {
        
        Service.restart();
        Thread.sleep(3000);
        
        final Executor executor = Executor.create(40, TestWorker.class);
        
        final Timing timing = Timing.create(true);
        executor.execute();
        final boolean success = executor.await(5000);
        timing.assertTimeIn(500, 1500);
        final String failedTestWorkerInfo = PerformanceTest.createFailedTestWorkerInfo(executor);
        Assert.assertTrue(failedTestWorkerInfo, success);
        Assert.assertFalse(failedTestWorkerInfo, executor.isFailed());
        Assert.assertFalse(failedTestWorkerInfo, executor.isInterrupted());
        
        for (final Worker worker : executor.getWorkers()) {
            Assert.assertTrue(worker.isExecuted());
            Assert.assertTrue(worker.isTerminated());
            Assert.assertFalse(worker.isFailed());
            Assert.assertFalse(worker.isInterrupted());
            final TestWorker testWorker = (TestWorker)worker;
            Assert.assertTrue(testWorker.success);
            for (final TestWorker.Response response : testWorker.responseList) {
                Assert.assertNull(response.exception);
                Assert.assertNotNull(response.data);
                final String responseString = new String(response.data);
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_STATUS_200));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_IMAGE_JPEG));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED));                    
            }
        }
    }
    
    /** 
     * Test case for acceptance.
     * Measures the execution time of 1000 (40 x 25) request.
     * The second load test is faster because the server has a large number of
     * threads.
     * @throws Exception
     */
    @Test
    public void testAcceptance_2()
            throws Exception {
        
        Service.restart();
        Thread.sleep(3000);
        
        final Timing timing = Timing.create(true);

        final Executor executor1 = Executor.create(40, TestWorker.class);
        executor1.execute();
        final boolean success1 = executor1.await(5000);
        timing.assertTimeIn(500, 1500);
        final String failedTestWorkerInfo1 = PerformanceTest.createFailedTestWorkerInfo(executor1);
        Assert.assertTrue(failedTestWorkerInfo1, success1);
        Assert.assertFalse(failedTestWorkerInfo1, executor1.isFailed());
        Assert.assertFalse(failedTestWorkerInfo1, executor1.isInterrupted());
        
        Thread.sleep(3000);
        
        final Executor executor2 = Executor.create(40, TestWorker.class);
        timing.restart();
        executor2.execute();
        final boolean success2 = executor2.await(5000); 
        timing.assertTimeIn(500, 1500);
        final String failedTestWorkerInfo2 = PerformanceTest.createFailedTestWorkerInfo(executor2);
        Assert.assertTrue(failedTestWorkerInfo2, success2);
        Assert.assertFalse(failedTestWorkerInfo2, executor2.isFailed());
        Assert.assertFalse(failedTestWorkerInfo2, executor2.isInterrupted());
        
        for (final Worker worker : executor1.getWorkers()) {
            Assert.assertTrue(worker.isExecuted());
            Assert.assertTrue(worker.isTerminated());
            Assert.assertFalse(worker.isFailed());
            Assert.assertFalse(worker.isInterrupted());
            final TestWorker testWorker = (TestWorker)worker;
            Assert.assertTrue(testWorker.success);
            for (final TestWorker.Response response : testWorker.responseList) {
                Assert.assertNull(response.exception);
                Assert.assertNotNull(response.data);
                final String responseString = new String(response.data);
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_STATUS_200));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_IMAGE_JPEG));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED));                    
            }
        }
        
        for (final Worker worker : executor2.getWorkers()) {
            Assert.assertTrue(worker.isExecuted());
            Assert.assertTrue(worker.isTerminated());
            Assert.assertFalse(worker.isFailed());
            Assert.assertFalse(worker.isInterrupted());
            final TestWorker testWorker = (TestWorker)worker;
            Assert.assertTrue(testWorker.success);
            for (final TestWorker.Response response : testWorker.responseList) {
                Assert.assertNull(response.exception);
                Assert.assertNotNull(response.data);
                final String responseString = new String(response.data);
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_STATUS_200));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_IMAGE_JPEG));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED));                    
            }
        }
    } 
    
    private static void waitRuntimeReady()
            throws Exception {
        String compare = null;
        final Timing timing = Timing.create(true);
        while (timing.timeMillis() < 5000) {
            System.gc();
            Thread.sleep(1000);
            final String threads = String.format("%08d", Thread.activeCount() /10);
            final String memory = String.format("%08d", (int)(SystemInfo.getSystemMemoryLoad() /1024 /1024));
            if (compare == null
                    || (threads + "\0" + memory).compareTo(compare) < 0) {
                timing.restart();
                compare = (threads + "\0" + memory);
            }
        }
    }
    
    /** 
     * Test case for acceptance.
     * The internal resource management must fast deallocate threads and memory.
     * @throws Exception
     */    
    @Test
    public void testAcceptance_3()
            throws Exception {
        
        Service.restart();
        Thread.sleep(3000);
        
        PerformanceTest.waitRuntimeReady();

        final long threadCount1 = Thread.activeCount() /10;
        final long memoryUsage1 = SystemInfo.getSystemMemoryLoad() /1024 /1024;
        
        final Executor executor = Executor.create(40, TestWorker.class);
        final long threadCount2 = Thread.activeCount() /10;
        final long memoryUsage2 = SystemInfo.getSystemMemoryLoad() /1024 /1024;     
        
        final Timing timing = Timing.create(true);
        executor.execute();
        final boolean success = executor.await(5000);
        timing.assertTimeIn(500, 1500);
        final String failedTestWorkerInfo = PerformanceTest.createFailedTestWorkerInfo(executor);
        Assert.assertTrue(failedTestWorkerInfo, success);
        Assert.assertFalse(failedTestWorkerInfo, executor.isFailed());
        Assert.assertFalse(failedTestWorkerInfo, executor.isInterrupted());
        final long threadCount3 = Thread.activeCount() /10;
        final long memoryUsage3 = SystemInfo.getSystemMemoryLoad() /1024 /1024;

        for (final Worker worker : executor.getWorkers()) {
            Assert.assertTrue(worker.isExecuted());
            Assert.assertTrue(worker.isTerminated());
            Assert.assertFalse(worker.isFailed());
            Assert.assertFalse(worker.isInterrupted());
            final TestWorker testWorker = (TestWorker)worker;
            Assert.assertTrue(testWorker.success);
            for (final TestWorker.Response response : testWorker.responseList) {
                Assert.assertNull(response.exception);
                Assert.assertNotNull(response.data);
                final String responseString = new String(response.data);
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_STATUS_200));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_TYPE_IMAGE_JPEG));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_CONTENT_LENGTH));
                Assert.assertTrue(responseString.matches(Pattern.HTTP_RESPONSE_LAST_MODIFIED));                    
            }
        }

        // server needs some time to clean up unused workers
        PerformanceTest.waitRuntimeReady();
        Thread.sleep(60000);
        final long threadCount4 = Thread.activeCount() /10;
        final long memoryUsage4 = SystemInfo.getSystemMemoryLoad() /1024 /1024;
        
        Assert.assertTrue(String.format("%d < %d", threadCount1, threadCount2),
                threadCount1 < threadCount2);
        Assert.assertTrue(String.format("%d < %d", threadCount2, threadCount3),
                threadCount2 < threadCount3);
        Assert.assertTrue(String.format("%d < %d", threadCount4, threadCount3),
                threadCount4 < threadCount3);
        Assert.assertTrue(String.format("%d <= %d", threadCount4, threadCount2),
                threadCount4 <= threadCount2);

        Assert.assertTrue(String.format("%d <= %d", memoryUsage1, memoryUsage2),
                memoryUsage1 <= memoryUsage2);
        Assert.assertTrue(String.format("%d < %d", memoryUsage2, memoryUsage3),
                memoryUsage2 < memoryUsage3);
        Assert.assertTrue(String.format("%d < %d", memoryUsage4, memoryUsage3),
                memoryUsage4 < memoryUsage3);
        Assert.assertTrue(String.format("%d < %d", memoryUsage4, memoryUsage3),
                memoryUsage4 < memoryUsage3);
    }
    
    /**
     * Internal class for a worker.
     * A worker creates a set of requests, performs them and collects the
     * responses.
     */
    public static class TestWorker extends Worker {
        
        /** set of requests */
        private List<String> requestList;
        
        /** collected set of responses */
        private List<Response> responseList;
        
        private boolean success;
        
        @Override
        protected void prepare()
                throws IOException {
            this.requestList = new ArrayList<>();
            this.responseList = new ArrayList<>();
            final File resources = new File(AbstractStage.getRootStage(), "documents/performance");
            for (final File file : resources.listFiles())
                this.requestList.add("GET /performance/" + file.getName() + " HTTP/1.0\r\n\r\n");
        }
        
        @Override
        protected void execute() {
            for (final String request : this.requestList)
                this.responseList.add(Response.create("127.0.0.1:18080", request));
            this.success = true;
        }
        
        @Override
        public String toString() {
            String result = this.getClass().getSimpleName() + " " + this.hashCode();
            for (int loop = 0; loop < this.requestList.size(); loop++) {
                final Response response = this.responseList.get(loop);
                if (response.exception != null)
                    result += "\t" + this.requestList.get(loop)
                            + "\t\t" + response.exception;
            }
            return result;
        }
        
        /**
         * Internal class for a response.
         * Collection of response data (data, duration, possibly occurred
         * exception).
         */
        static class Response {
            
            /** response data */
            byte[] data;
            
            /** execution time */
            long duration;
            
            /** possibly occurred exception */
            Exception exception;
            
            /**
             * Performs a request and creates a new response object.
             * @param  host
             * @param  request
             * @return the created response
             */
            static Response create(final String host, final String request) {
                final Response response = new Response();
                response.duration = System.currentTimeMillis();
                try {response.data = HttpUtils.sendRequest(host, request);
                } catch (IOException | GeneralSecurityException exception) {
                    return Response.create(exception);
                }
                response.duration = System.currentTimeMillis() -response.duration;
                return response;
            }
            
            /**
             * Creates a new response object for an occurred exception.
             * @return the created response
             */
            static Response create(final Exception exception) {
                final Response response = new Response();
                response.exception = exception;
                return response;
            }
        }
    }
}