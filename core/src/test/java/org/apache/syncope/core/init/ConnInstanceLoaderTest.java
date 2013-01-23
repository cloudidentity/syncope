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
package org.apache.syncope.core.init;

import static org.junit.Assert.assertEquals;

import org.apache.syncope.core.AbstractNonDAOTest;
import org.apache.syncope.core.persistence.dao.ResourceDAO;
import org.apache.syncope.core.propagation.SyncopeConnector;
import org.apache.syncope.core.rest.data.ResourceDataBinder;
import org.apache.syncope.core.util.ApplicationContextProvider;
import org.apache.syncope.core.util.ConnBundleManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ConnInstanceLoaderTest extends AbstractNonDAOTest {

    private ConnInstanceLoader cil;

    @Autowired
    private ResourceDAO resourceDAO;

    @Autowired
    private ConnBundleManager connBundleManager;

    @Autowired
    private ResourceDataBinder resourceDataBinder;

    @Before
    public void before() {
        cil = new ConnInstanceLoader();
        ReflectionTestUtils.setField(cil, "resourceDAO", resourceDAO);
        ReflectionTestUtils.setField(cil, "connBundleManager", connBundleManager);
        ReflectionTestUtils.setField(cil, "resourceDataBinder", resourceDataBinder);

        // Remove any other connector instance bean set up by
        // standard ConnInstanceLoader.load()
        for (String bean : ApplicationContextProvider.getApplicationContext().
                getBeanNamesForType(SyncopeConnector.class)) {

            cil.unregisterConnector(bean);
        }
    }

    @Test
    public void load() {
        cil.load();

        assertEquals(resourceDAO.findAll().size(),
                ApplicationContextProvider.getApplicationContext().
                getBeanNamesForType(SyncopeConnector.class, false, true).length);
    }
}
