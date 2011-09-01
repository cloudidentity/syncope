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
package org.syncope.console.pages.panels;

import org.syncope.console.wicket.markup.html.form.MultiValueSelectorPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syncope.client.to.AbstractAttributableTO;
import org.syncope.client.to.AttributeTO;
import org.syncope.client.to.RoleTO;
import org.syncope.client.to.SchemaTO;
import org.syncope.client.to.UserTO;
import org.syncope.console.pages.Schema;
import org.syncope.console.rest.SchemaRestClient;
import org.syncope.console.wicket.markup.html.form.AjaxCheckBoxPanel;
import org.syncope.console.wicket.markup.html.form.AjaxDropDownChoicePanel;
import org.syncope.console.wicket.markup.html.form.AjaxTextFieldPanel;
import org.syncope.console.wicket.markup.html.form.DateTextFieldPanel;
import org.syncope.console.wicket.markup.html.form.DateTimeFieldPanel;
import org.syncope.console.wicket.markup.html.form.FieldPanel;

public class AttributesPanel extends Panel {

    /**
     * Logger.
     */
    protected static final Logger LOG =
            LoggerFactory.getLogger(AttributesPanel.class);

    private static final long serialVersionUID = 552437609667518888L;

    @SpringBean
    private SchemaRestClient schemaRestClient;

    final WebMarkupContainer attributesContainer;

    public <T extends AbstractAttributableTO> AttributesPanel(
            final String id, final T entityTO, final Form form) {
        super(id);

        final IModel<Map<String, SchemaTO>> schemas =
                new LoadableDetachableModel<Map<String, SchemaTO>>() {

                    private static final long serialVersionUID =
                            -2012833443695917883L;

                    @Override
                    protected Map<String, SchemaTO> load() {
                        final List<SchemaTO> schemaTOs;
                        if (entityTO instanceof RoleTO) {
                            schemaTOs = schemaRestClient.getSchemas(
                                    "role");
                        } else if (entityTO instanceof UserTO) {
                            schemaTOs = schemaRestClient.getSchemas(
                                    "user");
                        } else {
                            schemaTOs = schemaRestClient.getSchemas(
                                    "membership");
                        }

                        final Map<String, SchemaTO> schemas =
                                new HashMap<String, SchemaTO>();

                        for (SchemaTO schemaTO : schemaTOs) {
                            schemas.put(schemaTO.getName(), schemaTO);
                        }

                        return schemas;
                    }
                };

        attributesContainer = new WebMarkupContainer("container");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        initEntityData(entityTO, schemas.getObject().values());

        final ListView<AttributeTO> attributeView = new ListView<AttributeTO>(
                "schemas", new PropertyModel<List<? extends AttributeTO>>(
                entityTO, "attributes")) {

            private static final long serialVersionUID = 9101744072914090143L;

            @Override
            protected void populateItem(ListItem item) {
                final AttributeTO attributeTO =
                        (AttributeTO) item.getDefaultModelObject();

                item.add(new Label("name", attributeTO.getSchema()));

                final Panel panel = getFieldPanel(
                        schemas.getObject().get(attributeTO.getSchema()),
                        form,
                        attributeTO);

                if (schemas.getObject().get(attributeTO.getSchema()).
                        isMultivalue()) {

                    final MultiValueSelectorPanel multiFieldPanel =
                            new MultiValueSelectorPanel<String>(
                            "panel",
                            new PropertyModel(attributeTO, "values"),
                            String.class,
                            (FieldPanel) panel);

                    item.add(multiFieldPanel);
                } else {
                    item.add(panel);
                }

            }
        };

        attributesContainer.add(attributeView);
    }

    private List<AttributeTO> initEntityData(
            final AbstractAttributableTO entityTO,
            final Collection<SchemaTO> schemas) {

        final List<AttributeTO> entityData = new ArrayList<AttributeTO>();

        final Map<String, List<String>> attributeMap =
                entityTO.getAttributeMap();

        AttributeTO attributeTO;
        List<String> values;

        for (SchemaTO schema : schemas) {
            attributeTO = new AttributeTO();
            attributeTO.setSchema(schema.getName());

            if (attributeMap.get(schema.getName()) == null
                    || attributeMap.get(schema.getName()).isEmpty()) {

                values = new ArrayList<String>();
                values.add("");
                attributeTO.setValues(values);

                // is important to set readonly only after valus setting
                attributeTO.setReadonly(schema.isReadonly());

            } else {
                attributeTO.setValues(attributeMap.get(schema.getName()));
            }
            entityData.add(attributeTO);
        }

        entityTO.setAttributes(entityData);

        return entityData;
    }

    private Panel getFieldPanel(
            final SchemaTO schemaTO,
            final Form form,
            final AttributeTO attributeTO) {

        final FieldPanel panel;

        final boolean required =
                schemaTO.getMandatoryCondition().equalsIgnoreCase("true");

        final boolean readOnly = schemaTO.isReadonly();

        switch (schemaTO.getType()) {
            case Boolean:
                panel = new AjaxCheckBoxPanel(
                        "panel", schemaTO.getName(), new Model(), true);

                panel.setRequired(required);

                break;

            case Date:
                if (!schemaTO.getConversionPattern().contains("H")) {
                    panel = new DateTextFieldPanel(
                            "panel", schemaTO.getName(), new Model(), true,
                            schemaTO.getConversionPattern());

                    if (required) {
                        panel.addRequiredLabel();
                    }
                } else {
                    panel = new DateTimeFieldPanel(
                            "panel", schemaTO.getName(), new Model(), true,
                            schemaTO.getConversionPattern());

                    if (required) {
                        panel.addRequiredLabel();
                        ((DateTimeFieldPanel) panel).setFormValidator(form);
                    }

                    panel.setStyleShet("ui-widget-content ui-corner-all");
                }

                break;

            case Enum:
                panel = new AjaxDropDownChoicePanel(
                        "panel", schemaTO.getName(), new Model(), true);

                ((AjaxDropDownChoicePanel) panel).setChoices(
                        Arrays.asList(schemaTO.getEnumerationValues().
                        split(Schema.enumValuesSeparator)));

                if (required) {
                    panel.addRequiredLabel();
                }

                break;

            default:
                panel = new AjaxTextFieldPanel(
                        "panel", schemaTO.getName(), new Model(), true);

                if (required) {
                    panel.addRequiredLabel();
                }

        }

        panel.setReadOnly(readOnly);
        panel.setNewModel(attributeTO.getValues());

        return panel;
    }
}