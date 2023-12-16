/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * Devwex, Experimental Server Engine
 * Copyright (C) 2022 Seanox Software Solutions
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
package module;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorkerModule_E extends AbstractWorkerModule {
    
    public WorkerModule_E(final String options) {
    }    
    
    public void filter(final Worker worker, final String options)
            throws Exception {

        final String docRoot = worker.environmentMap.get("DOCUMENT_ROOT");
        
        int value = 1;
        Path testFile = Paths.get(docRoot, "test.txt");
        if (Files.exists(testFile))
            value = Integer.valueOf(new String(Files.readAllBytes(testFile))).intValue() +1;
        Files.write(testFile, String.valueOf(value).getBytes());
    }
}