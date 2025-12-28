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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** TestSuite for {@link com.seanox.devwex.Worker}. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    WorkerAccessLogTest.class,
    WorkerAuthenticationTestSuite.class,
    WorkerConfigurationTest.class,
    WorkerDeleteTest.class,
    WorkerDirectoryIndexTest.class,
    WorkerFileTest.class,
    WorkerFilterTest.class,
    WorkerGatewayTest.class,
    WorkerGetTest.class,
    WorkerHeadTest.class,
    WorkerLocateTest.class,
    WorkerMimeTypeTest.class,
    WorkerOptionsTest.class,
    WorkerPutTest.class,
    WorkerRequestTest.class,
    WorkerStatusTest.class,
    WorkerTextTest.class,
    WorkerVirtualHostTest.class
})
public class WorkerTestSuite extends AbstractStageTestSuite {
}