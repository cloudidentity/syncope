/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.propagation;

/**
 * Bear stacktrace received during propagation towards a certain resource.
 */
public class PropagationException extends Exception {

    private static final long serialVersionUID = 1125884657361004958L;

    /**
     * The resource involved in this exception.
     */
    private final String resourceName;

    /**
     * Create a new instance based on resource name and original stacktrace
     * received during propagation.
     *
     * @param resourceName name of resource involved in this exception
     * @param stackTrace original stacktrace
     */
    public PropagationException(final String resourceName, final String stackTrace) {

        super("Exception during provision on resource " + resourceName + "\n" + stackTrace);

        this.resourceName = resourceName;
    }

    /**
     * @return name of resource involved in this exception
     */
    public String getResourceName() {
        return resourceName;
    }
}