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
package org.syncope.core.rest;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.web.client.HttpStatusCodeException;
import org.syncope.client.mod.AttributeMod;
import org.syncope.client.mod.RoleMod;
import org.syncope.client.to.AttributeTO;
import org.syncope.client.to.RoleTO;
import org.syncope.client.validation.SyncopeClientCompositeErrorException;

public class RoleTestITCase extends AbstractTest {

    @Test
    @ExpectedException(value = SyncopeClientCompositeErrorException.class)
    public void createWithException() {
        AttributeTO attributeTO = new AttributeTO();
        attributeTO.setSchema("attr1");
        attributeTO.addValue("value1");

        RoleTO newRoleTO = new RoleTO();
        newRoleTO.addAttribute(attributeTO);

        restTemplate.postForObject(BASE_URL + "role/create",
                newRoleTO, RoleTO.class);
    }

    @Test
    public void create() {
        RoleTO roleTO = new RoleTO();
        roleTO.setName("lastRole");
        roleTO.setParent(8L);

        AttributeTO icon = new AttributeTO();
        icon.setSchema("icon");
        icon.addValue("anIcon");

        RoleTO newRoleTO = restTemplate.postForObject(BASE_URL + "role/create",
                roleTO, RoleTO.class);

        roleTO.setId(newRoleTO.getId());
        assertEquals(roleTO, newRoleTO);
    }

    @Test
    public void delete() {
        try {
            restTemplate.delete(BASE_URL + "role/delete/{roleId}", 0);
        } catch (HttpStatusCodeException e) {
            assertEquals(e.getStatusCode(), HttpStatus.NOT_FOUND);
        }

        restTemplate.delete(BASE_URL + "role/delete/{roleId}", 5);
        try {
            restTemplate.getForObject(BASE_URL + "role/read/{roleId}.json",
                    RoleTO.class, 2);
        } catch (HttpStatusCodeException e) {
            assertEquals(e.getStatusCode(), HttpStatus.NOT_FOUND);
        }
    }

    @Test
    public void list() {
        List<RoleTO> roleTOs = Arrays.asList(
                restTemplate.getForObject(BASE_URL
                + "role/list.json", RoleTO[].class));
        assertNotNull(roleTOs);
        assertEquals(8, roleTOs.size());
        for (RoleTO roleTO : roleTOs) {
            assertNotNull(roleTO);
        }
    }

    @Test
    public void parent() {
        RoleTO roleTO = restTemplate.getForObject(BASE_URL
                + "role/parent/{roleId}.json", RoleTO.class, 7);

        assertNotNull(roleTO);
        assertEquals(roleTO.getId(), 6L);
    }

    @Test
    public void read() {
        RoleTO roleTO = restTemplate.getForObject(BASE_URL
                + "role/read/{roleId}.json", RoleTO.class, 1);

        assertNotNull(roleTO);
        assertNotNull(roleTO.getAttributes());
        assertFalse(roleTO.getAttributes().isEmpty());
    }

    @Test
    public void update() {
        RoleTO roleTO = new RoleTO();
        roleTO.setName("latestRole");
        roleTO.setParent(8L);

        AttributeTO icon = new AttributeTO();
        icon.setSchema("icon");
        icon.addValue("anIcon");
        roleTO.addAttribute(icon);

        roleTO = restTemplate.postForObject(BASE_URL + "role/create",
                roleTO, RoleTO.class);

        assertEquals(1, roleTO.getAttributes().size());

        AttributeMod attributeMod = new AttributeMod();
        attributeMod.setSchema("show");
        attributeMod.addValueToBeAdded("FALSE");

        RoleMod roleMod = new RoleMod();
        roleMod.setId(roleTO.getId());
        roleMod.setName("finalRole");
        roleMod.addAttributeToBeUpdated(attributeMod);

        roleTO = restTemplate.postForObject(BASE_URL + "role/update",
                roleMod, RoleTO.class);

        assertEquals("finalRole", roleTO.getName());
        assertEquals(2, roleTO.getAttributes().size());
    }
}
