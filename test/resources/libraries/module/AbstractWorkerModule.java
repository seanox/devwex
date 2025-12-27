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

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Map;

class AbstractWorkerModule extends AbstractModule {
    
    public void filter(final Object facade, final String options)
            throws Exception {
        final Worker worker = Worker.create(facade);
        try {this.filter(worker, options);
        } finally {
            worker.synchronize();
        }
    }
    
    public void filter(final Worker worker, final String options)
            throws Exception {
        return;
    }

    public void service(final Object facade, final String options)
            throws Exception {
        final Worker worker = Worker.create(facade);
        try {this.service(worker, options);
        } finally {
            worker.synchronize();
        }
    }    

    public void service(final Worker worker, final String options)
            throws Exception {
        return;
    }
    
    static class Worker {
        
        /** referenced worker */
        private Object facade;
        
        /** socket of worker */
        protected Socket accept;   
        
        /** worker output stream */
        protected OutputStream output;
        
        /** worker environment */
        private Object environment;
        
        /** internal map of worker environment */
        protected Map<String, String> environmentMap;    

        /** worker connection control */
        protected boolean control;

        /** worker response status */
        protected int status;        

        /**
         * Synchronizes the fields of two objects.
         * In the target object is searched for the fields from the source object
         * and synchronized when if they exist.
         * @param source
         * @param target
         */
        private static void synchronizeFields(final Object source, final Object target)
                throws Exception {

            for (final Field inport : source.getClass().getDeclaredFields()) {

                final Field export;
                try {export = target.getClass().getDeclaredField(inport.getName());
                } catch (NoSuchFieldException exception) {
                    continue;
                }

                export.setAccessible(true);
                inport.setAccessible(true);

                if (inport.getType().equals(Boolean.TYPE))
                    export.setBoolean(target, inport.getBoolean(source));
                else if (inport.getType().equals(Integer.TYPE))
                    export.setInt(target, inport.getInt(source));
                else if (inport.getType().equals(Long.TYPE))
                    export.setLong(target, inport.getLong(source));
                else if (!inport.getType().isPrimitive())
                    export.set(target, inport.get(source));
            }
        } 
        
        private static Object getField(final Object source, final String field)
                throws Exception {
            final Field export = source.getClass().getDeclaredField(field);
            export.setAccessible(true);
            return export.get(source);
        }        
        
        static Worker create(final Object facade)
                throws Exception {
            final Worker worker = new Worker();
            worker.facade = facade;
            Worker.synchronizeFields(facade, worker);
            if (worker.environment != null)
                worker.environmentMap = (Map<String, String>)Worker.getField(worker.environment, "entries");
            if (worker.output == null)
                worker.output = worker.accept.getOutputStream(); 
            
            return worker; 
        }
        
        void synchronize()
                throws Exception {
            Worker.synchronizeFields(this, this.facade);
        }
    }    
}