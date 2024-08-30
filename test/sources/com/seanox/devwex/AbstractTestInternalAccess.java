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
package com.seanox.devwex;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** AbstractTestInternalAccess provides low-level access via reflections. */
class AbstractTestInternalAccess {

    /**
     * Determines the source class for an object.
     * @param  object object
     * @return the source class for the object
     */
    private static Class<?> getSourceClass(final Object object) {
        if (object == null)
            return null;
        if (object instanceof Class)
            return (Class<?>)object;
        return object.getClass();
    }

    /**
     * Determines the data types for an object(array).
     * If {@code null} is passed, {@code null} is returned.
     * @param  objects object(s)
     * @return the determined the data types for an object(array)
     */
    private static Class<?>[] getTypes(final Object... objects) {
        if (objects == null)
            return null;
        final List<Class<?>> types = new ArrayList<>();
        for (Object object : objects) {
            if (object != null)
                object = object.getClass();
            types.add((Class<?>)object);
        }
        return types.toArray(new Class<?>[0]);
    }

    /**
     * Gets a field, even those that are not public or in a superclass, or
     * throws {@link NoSuchFieldException} if the field does not exist.
     * @param  object class or object to be analyzed
     * @param  name   name of the field
     * @return the determined field
     * @throws NoSuchFieldException
     *     If this field cannot be determined.
     */
    private static Field getField(final Object object, final String name)
            throws NoSuchFieldException {
        Objects.requireNonNull(object, "Invalid object [null]");
        Objects.requireNonNull(name, "Invalid field [null]");
        for (Class<?> source = AbstractTestInternalAccess.getSourceClass(object);
               source != null; source = source.getSuperclass()) {
            final Field field;
            try {field = source.getDeclaredField(name);
            } catch (NoSuchFieldException exception) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        throw new NoSuchFieldException();
    }

    /**
     * Gets the value of a field from an object, even those that are not
     * public or in a superclass. Primitive data types are returned as a
     * corresponding wrapper object.
     * @param  object object
     * @param  field   name of the field
     * @return the value of the field, primitive data types are returned as a
     *         corresponding wrapper object
     * @throws IllegalAccessException
     *     In the case of an access violation.
     * @throws NoSuchFieldException
     *     If the field does not exist.
     */
    static Object getFieldValue(final Object object, final String field)
            throws IllegalAccessException, NoSuchFieldException {
        Objects.requireNonNull(object, "Invalid object [null]");
        Objects.requireNonNull(field, "Invalid field [null]");
        return AbstractTestInternalAccess.getField(object, field).get(object);
    }

    /**
     * Gets a method, even those that are not public or in a superclass, or
     * throws {@link NoSuchMethodException} if the method does not exist.
     * @param  object class or object to be analyzed
     * @param  name   name of the method
     * @param  types  data types as an array
     * @return the determined method
     * @throws NoSuchMethodException
     *     If this method cannot be determined.
     */
    private static Method getMethod(final Object object, final String name, final Class<?>... types)
            throws NoSuchMethodException {
        Objects.requireNonNull(object, "Invalid object [null]");
        Objects.requireNonNull(name, "Invalid field [null]");
        for (Class<?> source = AbstractTestInternalAccess.getSourceClass(object);
                source != null; source = source.getSuperclass()) {
            final Method method;
            try {method = source.getDeclaredMethod(name, types);
            } catch (NoSuchMethodException exception) {
                continue;
            }
            method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException();
    }

    /**
     * Executes a method from an object or class without further arguments,
     * even if the method is not public or in a superclass. Returns the return
     * value of the method as an object. Primitive data types are returned as
     * a corresponding wrapper object.
     * @param  object object or class
     * @param  method name of the method
     * @return the return value of the method as an object
     * @throws NoSuchMethodException
     *     If the method does not exist.
     * @throws IllegalAccessException
     *     If access to the field fails.
     * @throws InvocationTargetException
     *     If call and/or execution of the method fails.
     */
    static Object invoke(final Object object, final String method)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return AbstractTestInternalAccess.invoke(object, method, null, null);
    }

    /**
     * Executes a method from an object or class with the arguments as an
     * array of objects, even those that are not public or in a superclass.
     * For arguments with a primitive data type, the appropriate wrapper must
     * be used. Returns the return value of the method as an object. Primitive
     * data types are returned as a corresponding wrapper object.
     * @param  object    object or class
     * @param  method    name of the method
     * @param  arguments arguments as an array of objects
     * @return the return value of the method as an object
     * @throws NoSuchMethodException
     *     If the method does not exist.
     * @throws IllegalAccessException
     *     In case of an access violation to the method.
     * @throws InvocationTargetException
     *     If call and/or execution of the method fails.
     */
    static Object invoke(final Object object, final String method, final Object... arguments)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return AbstractTestInternalAccess.invoke(object, method, AbstractTestInternalAccess.getTypes(arguments), arguments);
    }

    /**
     * Executes a method from an object or class with the arguments as an
     * array of data types and objects, even those that are not public or in a
     * superclass. For arguments with a primitive data type, the appropriate
     * wrapper must be used. Returns the return value of the method as an
     * object. Primitive data types are returned as a corresponding wrapper
     * object.
     * @param  object    object or class
     * @param  method    name of the method
     * @param  types     data types as an array
     * @param  arguments arguments as an array of objects
     * @return the return value of the method as an object
     * @throws NoSuchMethodException
     *     If the method does not exist.
     * @throws IllegalAccessException
     *     In case of an access violation to the method.
     * @throws InvocationTargetException
     *     If call and/or execution of the method fails.
     */
    private static Object invoke(final Object object, final String method, final Class<?>[] types, final Object[] arguments)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Objects.requireNonNull(object, "Invalid object [null]");
        Objects.requireNonNull(method, "Invalid method [null]");
        return AbstractTestInternalAccess.getMethod(object, method, types).invoke(object, arguments);
    }
}