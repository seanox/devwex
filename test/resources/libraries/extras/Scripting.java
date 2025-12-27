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
package extras;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/** Very simple command line interface for scripting, based on the CGI. */
public class Scripting {
    
    /**
     * Main method of application.
     * Reads and execute the script file of the environment variable
     * {@code PATH_TRANSLATED}.
     * @param options
     */
    public static void main(final String... options) {
        try {
            final Path pathTranslated = Paths.get(System.getenv("PATH_TRANSLATED"));
            System.setProperty("user.dir", pathTranslated.getParent().toRealPath().toString());
            final ScriptEngineManager factory = new ScriptEngineManager();
            final ScriptEngine engine = factory.getEngineByName("JavaScript");
            engine.eval(Files.newBufferedReader(pathTranslated));            
        } catch (Exception exception) {
            System.out.print("\r\n\r\n");
            exception.printStackTrace(System.out);
        }
    }
}