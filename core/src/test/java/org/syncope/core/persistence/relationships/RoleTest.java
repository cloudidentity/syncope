/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.core.persistence.relationships;

import static org.junit.Assert.*;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.syncope.core.persistence.beans.role.RAttr;
import org.syncope.core.persistence.beans.role.RAttrValue;
import org.syncope.core.persistence.beans.role.RSchema;
import org.syncope.core.persistence.beans.role.SyncopeRole;
import org.syncope.core.persistence.dao.AttrDAO;
import org.syncope.core.persistence.dao.AttrValueDAO;
import org.syncope.core.persistence.dao.EntitlementDAO;
import org.syncope.core.persistence.dao.SchemaDAO;
import org.syncope.core.persistence.dao.RoleDAO;
import org.syncope.core.persistence.dao.UserDAO;
import org.syncope.core.persistence.AbstractTest;

@Transactional
public class RoleTest extends AbstractTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private SchemaDAO schemaDAO;

    @Autowired
    private AttrDAO attrDAO;

    @Autowired
    private AttrValueDAO attrValueDAO;

    @Autowired
    private EntitlementDAO entitlementDAO;

    @Test
    public final void delete() {
        roleDAO.delete(2L);

        roleDAO.flush();

        assertNull(roleDAO.find(2L));
        assertEquals(1, entitlementDAO.find("base").getRoles().size());
        assertTrue(userDAO.find(2L).getRoles().size() == 1);
        assertNull(attrDAO.find(700L, RAttr.class));
        assertNull(attrValueDAO.find(41L, RAttrValue.class));
        assertNotNull(schemaDAO.find("icon", RSchema.class));
    }

    @Test
    public final void inheritedAttributes() {
        SyncopeRole director = roleDAO.find(7L);

        assertEquals(2, director.findInheritedAttributes().size());
    }

    @Test
    public final void inheritedDerivedAttributes() {
        SyncopeRole director = roleDAO.find(7L);

        assertEquals(1, director.findInheritedDerivedAttributes().size());
    }
}
