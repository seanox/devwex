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

public class WorkerModule_A extends AbstractWorkerModule {
    
    public WorkerModule_A(final String options) {
    }
    
    public void filter(final Worker worker, final String options)
            throws Exception {
        this.perform(worker, options);
    }

    public void service(final Worker worker, final String options)
            throws Exception {
        this.perform(worker, options);
    }
        
    private void perform(final Worker worker, final String options)
            throws Exception {
          
        worker.status = 1;

        //the header is built and written out
        String string = ("HTTP/1.0 ").concat("001 Test ok").concat("\r\n");
        string = string.concat("Server: ").concat(worker.environmentMap.get("SERVER_SOFTWARE")).concat("\r\n");
        if (worker.environmentMap.get("MODULE_OPTS").length() > 0)
            string = string.concat("Opts: ").concat(worker.environmentMap.get("MODULE_OPTS")).concat("\r\n");
        String method = new Throwable().getStackTrace()[1].getMethodName();
        method = method.substring(0, 1).toUpperCase().concat(method.substring(1).toLowerCase());
        string = string.concat("Module: ").concat(this.getClass().getName() + "::" + method).concat("\r\n\r\n");

        //the connection is marked as closed
        worker.control = false;
        worker.output.write(string.getBytes());
    }
}