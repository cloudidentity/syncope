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
package org.apache.syncope.console.pages.panels;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.syncope.common.search.NodeCond;
import org.apache.syncope.common.to.AbstractAttributableTO;
import org.apache.syncope.common.to.UserTO;
import org.apache.syncope.common.validation.SyncopeClientCompositeErrorException;
import org.apache.syncope.console.commons.Constants;
import org.apache.syncope.console.pages.DisplayAttributesModalPage;
import org.apache.syncope.console.pages.EditUserModalPage;
import org.apache.syncope.console.pages.ResultStatusModalPage;
import org.apache.syncope.console.pages.StatusModalPage;
import org.apache.syncope.console.rest.AbstractAttributableRestClient;
import org.apache.syncope.console.wicket.extensions.markup.html.repeater.data.table.AttrColumn;
import org.apache.syncope.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.console.wicket.extensions.markup.html.repeater.data.table.TokenColumn;
import org.apache.syncope.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.springframework.util.ReflectionUtils;

public class UserSearchResultPanel extends AbstractSearchResultPanel {

    private static final long serialVersionUID = -905187144506842332L;

    public <T extends AbstractAttributableTO> UserSearchResultPanel(final String id, final boolean filtered,
            final NodeCond searchCond, final PageReference callerRef, final AbstractAttributableRestClient restClient) {

        super(id, filtered, searchCond, callerRef, restClient);
    }

    @Override
    protected List<IColumn<AbstractAttributableTO, String>> getColumns() {
        final List<IColumn<AbstractAttributableTO, String>> columns =
                new ArrayList<IColumn<AbstractAttributableTO, String>>();

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_DETAILS_VIEW)) {
            final Field field = ReflectionUtils.findField(UserTO.class, name);

            if ("token".equalsIgnoreCase(name)) {
                columns.add(new TokenColumn("token"));
            } else if (field != null && field.getType().equals(Date.class)) {
                columns.add(new DatePropertyColumn<AbstractAttributableTO>(new ResourceModel(name, name), name, name));
            } else {
                columns.add(
                        new PropertyColumn<AbstractAttributableTO, String>(new ResourceModel(name, name), name, name));
            }
        }

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_ATTRIBUTES_VIEW)) {
            columns.add(new AttrColumn(name, AttrColumn.SchemaType.schema));
        }

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_DERIVED_ATTRIBUTES_VIEW)) {
            columns.add(new AttrColumn(name, AttrColumn.SchemaType.derivedSchema));
        }

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_VIRTUAL_ATTRIBUTES_VIEW)) {
            columns.add(new AttrColumn(name, AttrColumn.SchemaType.virtualSchema));
        }

        // Add defaults in case of no selection
        if (columns.isEmpty()) {
            for (String name : DisplayAttributesModalPage.DEFAULT_SELECTION) {
                columns.add(
                        new PropertyColumn<AbstractAttributableTO, String>(new ResourceModel(name, name), name, name));
            }

            prefMan.setList(getRequest(), getResponse(), Constants.PREF_USERS_DETAILS_VIEW,
                    Arrays.asList(DisplayAttributesModalPage.DEFAULT_SELECTION));
        }

        columns.add(new AbstractColumn<AbstractAttributableTO, String>(new ResourceModel("actions", "")) {

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(final Item<ICellPopulator<AbstractAttributableTO>> cellItem,
                    final String componentId, final IModel<AbstractAttributableTO> model) {

                final ActionLinksPanel panel = new ActionLinksPanel(componentId, model);

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -7978723352517770644L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        statusmodal.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new StatusModalPage(page.getPageReference(), statusmodal, model.getObject());
                            }
                        });

                        statusmodal.show(target);
                    }
                }, ActionLink.ActionType.ENABLE, "Users", "update");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -7978723352517770644L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        editmodal.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new EditUserModalPage(
                                        page.getPageReference(), editmodal, (UserTO) model.getObject());
                            }
                        });

                        editmodal.show(target);
                    }
                }, ActionLink.ActionType.EDIT, "Users", "update");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -7978723352517770644L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            final UserTO userTO = (UserTO) restClient.delete(model.getObject().getId());

                            page.setModalResult(true);

                            editmodal.setPageCreator(new ModalWindow.PageCreator() {

                                private static final long serialVersionUID = -7834632442532690940L;

                                @Override
                                public Page createPage() {
                                    return new ResultStatusModalPage(editmodal, userTO);
                                }
                            });

                            editmodal.show(target);
                        } catch (SyncopeClientCompositeErrorException scce) {
                            error(getString("operation_error") + ": " + scce.getMessage());
                            target.add(feedbackPanel);
                        }
                    }
                }, ActionLink.ActionType.DELETE, "Users", "delete");

                cellItem.add(panel);
            }
        });

        return columns;
    }
}
