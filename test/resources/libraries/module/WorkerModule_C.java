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
package module;

import java.util.Arrays;

public class WorkerModule_C extends AbstractWorkerModule {
    
    public WorkerModule_C(final String options) {
    }

    public void filter(final Worker worker, final String options)
            throws Exception {
        this.perform(worker, options);
    }

    public void service(final Worker worker, final String options)
            throws Exception {
        this.perform(worker, options);
    }
        
    private void perform(Worker worker, String options)
            throws Exception {
        
        worker.status = 3;
        
        //the header is built and written out
        String string = ("HTTP/1.0 ").concat("003 Test ok").concat("\r\n");
        string = string.concat("Server: ").concat(worker.environmentMap.get("SERVER_SOFTWARE")).concat("\r\n");
        String method = new Throwable().getStackTrace()[1].getMethodName();
        method = method.substring(0, 1).toUpperCase().concat(method.substring(1).toLowerCase());
        string = string.concat("Module: ").concat(this.getClass().getName() + "::" + method).concat("\r\n\r\n");
        
        String value = null;
        String param = null;
        String queryString = worker.environmentMap.get("QUERY_STRING");
        for (String queryEntry : queryString.split("&")) {
            int index = queryEntry.indexOf("=");
            if (index >= 0)
                param = queryEntry.substring(0, index).trim();
            if (index >= 0)
                value = queryEntry.substring(index +1).trim();
            if (!Arrays.asList(new String[] {"exist", "value"}).contains(param))
                param = null;
            if (param != null)
                break;
        }
        
        if (("exist").equals(param))
            string = string.concat(worker.environmentMap.containsKey(value) ? "true" : "false");
        if (("value").equals(param)
                && worker.environmentMap.containsKey(value))
            string = string.concat(worker.environmentMap.get(value));            

        //the connection is marked as closed
        worker.control = false;
        worker.output.write(string.getBytes());
    }
}