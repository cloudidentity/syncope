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
package org.apache.syncope.console.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.syncope.common.to.AbstractAttributableTO;
import org.apache.syncope.common.to.AttributeTO;
import org.apache.syncope.common.to.ConnObjectTO;
import org.apache.syncope.common.to.PropagationRequestTO;
import org.apache.syncope.console.pages.panels.StatusPanel;
import org.apache.syncope.console.rest.AbstractAttributableRestClient;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUtils implements Serializable {

    private static final long serialVersionUID = 7238009174387184309L;

    public enum Status {

        NOT_YET_SUBMITTED,
        CREATED,
        ACTIVE,
        SUSPENDED,
        UNDEFINED,
        OBJECT_NOT_FOUND;

        public boolean isActive() {
            return this == ACTIVE;
        }
    }

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StatusUtils.class);

    private final AbstractAttributableRestClient restClient;

    public StatusUtils(final AbstractAttributableRestClient restClient) {
        this.restClient = restClient;
    }

    public List<StatusBean> getRemoteStatuses(final AbstractAttributableTO attributable) {
        final List<StatusBean> statuses = new ArrayList<StatusBean>();

        for (String resouceName : attributable.getResources()) {
            ConnObjectTO objectTO = null;
            try {
                objectTO = restClient.getConnectorObject(resouceName, attributable.getId());
            } catch (Exception e) {
                LOG.warn("ConnObject '{}' not found on resource '{}'", attributable.getId(), resouceName);
            }

            final StatusBean statusBean = getRemoteStatus(objectTO);
            statusBean.setResourceName(resouceName);
            statuses.add(statusBean);
        }

        return statuses;
    }

    public StatusBean getRemoteStatus(final ConnObjectTO objectTO) {
        final StatusBean statusBean = new StatusBean();

        if (objectTO != null) {
            final Boolean enabled = isEnabled(objectTO);

            final StatusUtils.Status status = enabled == null
                    ? StatusUtils.Status.UNDEFINED
                    : enabled
                    ? StatusUtils.Status.ACTIVE
                    : StatusUtils.Status.SUSPENDED;

            final String accountLink = getAccountLink(objectTO);

            statusBean.setStatus(status);
            statusBean.setAccountLink(accountLink);
        }

        return statusBean;
    }

    private Boolean isEnabled(final ConnObjectTO objectTO) {
        final String STATUSATTR = "__ENABLE__";

        final Map<String, AttributeTO> attributeTOs = objectTO.getAttributeMap();

        final AttributeTO status = attributeTOs.get(STATUSATTR);

        return status != null && status.getValues() != null && !status.getValues().isEmpty()
                ? Boolean.parseBoolean(status.getValues().get(0))
                : null;
    }

    private String getAccountLink(final ConnObjectTO objectTO) {
        final String NAME = "__NAME__";

        final Map<String, AttributeTO> attributeTOs = objectTO == null
                ? Collections.<String, AttributeTO>emptyMap()
                : objectTO.getAttributeMap();

        final AttributeTO name = attributeTOs.get(NAME);

        return name != null && name.getValues() != null && !name.getValues().isEmpty()
                ? name.getValues().get(0)
                : null;
    }

    public static PropagationRequestTO buildPropagationRequestTO(final Collection<StatusBean> statuses) {
        return buildPropagationRequestTO(statuses, null);
    }

    public static PropagationRequestTO buildPropagationRequestTO(final Collection<StatusBean> statuses,
            final Boolean enable) {

        PropagationRequestTO propagationRequestTO = new PropagationRequestTO();

        for (StatusBean status : statuses) {
            if (enable == null
                    || (enable && !status.getStatus().isActive()) || (!enable && status.getStatus().isActive())) {

                if ("Syncope".equals(status.getResourceName())) {
                    propagationRequestTO.setOnSyncope(true);
                } else {
                    propagationRequestTO.addResource(status.getResourceName());
                }

            }
        }

        return propagationRequestTO;
    }

    public static void update(final StatusPanel statusPanel, final AjaxRequestTarget target,
            final Collection<String> resourcesToAdd, final Collection<String> resourcesToRemove) {

        if (statusPanel != null) {
            Map<String, StatusBean> statusMap = new LinkedHashMap<String, StatusBean>();
            for (StatusBean statusBean : statusPanel.getStatusBeans()) {
                statusMap.put(statusBean.getResourceName(), statusBean);
            }

            for (String resource : resourcesToAdd) {
                if (!statusMap.keySet().contains(resource)) {
                    StatusBean statusBean;
                    if (statusPanel.getInitialStatusBeanMap().containsKey(resource)) {
                        statusBean = statusPanel.getInitialStatusBeanMap().get(resource);
                    } else {
                        statusBean = new StatusBean();
                        statusBean.setResourceName(resource);
                        statusBean.setStatus(StatusUtils.Status.NOT_YET_SUBMITTED);
                    }

                    statusMap.put(statusBean.getResourceName(), statusBean);
                }
            }

            for (String resource : resourcesToRemove) {
                statusMap.remove(resource);
            }

            statusPanel.updateStatusBeans(new ArrayList<StatusBean>(statusMap.values()));
            target.add(statusPanel);
        }
    }
}
