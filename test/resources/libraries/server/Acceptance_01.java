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
package server;

import com.seanox.devwex.Service;

public class Acceptance_01 implements Runnable {

    public Acceptance_01(final String context, final Object data)
            throws Throwable {
        throw new Throwable(this.getClass().getName());
    }
    
    public String expose() {
        final RuntimeException exception = new RuntimeException("Not expected!!!");
        Service.print(exception);
        throw exception;
    }

    public void destroy() {
        final RuntimeException exception = new RuntimeException("Not expected!!!");
        Service.print(exception);
        throw exception;
    }

    public void run() {
        final RuntimeException exception = new RuntimeException("Not expected!!!");
        Service.print(exception);
        throw exception;
    }
}