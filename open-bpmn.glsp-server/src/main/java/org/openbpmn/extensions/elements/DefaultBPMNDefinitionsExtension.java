/********************************************************************************
 * Copyright (c) 2022 Imixs Software Solutions GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package org.openbpmn.extensions.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.BPMNProcess;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.Signal;
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.extensions.AbstractBPMNElementExtension;
import org.openbpmn.glsp.bpmn.BPMNGNode;
import org.openbpmn.glsp.jsonforms.DataBuilder;
import org.openbpmn.glsp.jsonforms.SchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder.Layout;
import org.openbpmn.glsp.model.BPMNGModelFactory;
import org.openbpmn.glsp.model.BPMNGModelState;
import org.w3c.dom.Element;

import com.google.inject.Inject;

/**
 * This is the Default Root extension providing the JSONForms schemata on the
 * BPMN definitions level. This includes general diagram properties and sets
 * like signal or message elements.
 *
 * @author rsoika
 *
 */
public class DefaultBPMNDefinitionsExtension extends AbstractBPMNElementExtension {

    private static Logger logger = LogManager.getLogger(DefaultBPMNDefinitionsExtension.class);

    @Inject
    protected BPMNGModelState modelState;

    @Inject
    protected BPMNGModelFactory bpmnGModelFactory;

    public DefaultBPMNDefinitionsExtension() {
        super();
    }

    @Override
    public boolean handlesElementTypeId(final String elementTypeId) {

        return BPMNTypes.PROCESS_TYPE_PUBLIC.equals(elementTypeId);
    }

    /**
     * This Extension is for the default public process only
     */
    @Override
    public boolean handlesBPMNElement(final BPMNElement bpmnElement) {
        if (bpmnElement instanceof BPMNProcess) {
            return ((BPMNProcess) bpmnElement).isPublicProcess();
        }
        return false;
    }

    /**
     * This Helper Method generates a JSON Object with the BPMNElement properties.
     * <p>
     * This json object is used on the GLSP Client to generate the EMF JsonForms
     */
    @Override
    public void buildPropertiesForm(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

        String[] autoAlignOptions = { "Yes|true", "No|false" };
        Element definitions = modelState.getBpmnModel().getDefinitions();
        dataBuilder //
                .addData("name", bpmnElement.getName()) //
                .addData("documentation", bpmnElement.getDocumentation()) //
                .addData("targetNamespace", definitions.getAttribute("targetNamespace")) //
                .addData("exporter", definitions.getAttribute("exporter")) //
                .addData("exporterVersion", definitions.getAttribute("exporterVersion")) //
                .addData("auto-align", "" + modelState.getAutoAlign());

        schemaBuilder. //
                addProperty("name", "string", null). //
                addProperty("targetNamespace", "string", null). //
                addProperty("exporter", "string", null). //
                addProperty("exporterVersion", "string", null). //
                addProperty("documentation", "string", "Model description."). //
                addProperty("auto-align", "string", "Automatically aligns all BPMN elements to the grid.",
                        autoAlignOptions);

        Map<String, String> selectItemOption = new HashMap<>();
        selectItemOption.put("format", "selectitem");
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        uiSchemaBuilder. //
                addCategory("General"). //
                addLayout(Layout.HORIZONTAL). //
                addElements("name"). //
                addLayout(Layout.HORIZONTAL). //
                addElement("documentation", "Documentation", multilineOption). //
                // Category Definitions...
                addCategory("Definitions"). //
                addLayout(Layout.VERTICAL). //
                addElements("targetNamespace", "exporter", "exporterVersion"). //
                // Category Layout...
                addCategory("Layout"). //
                addLayout(Layout.VERTICAL). //
                addElement("auto-align", "Auto Align Elements", selectItemOption);

        // Signal List
        buildSignalProperties(modelState.getBpmnModel(), dataBuilder, schemaBuilder, uiSchemaBuilder);

    }

    /**
     * Adds the Signal set schema properties
     *
     * @param eventDefinitions
     * @param dataBuilder
     * @param schemaBuilder
     * @param uiSchemaBuilder
     */
    private void buildSignalProperties(final BPMNModel model, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder,
            final UISchemaBuilder uiSchemaBuilder) {

        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");

        if (model.getSignals().size() > 0) {
            Map<String, String> arrayDetailOption = new HashMap<>();
            // GENERATED | DEFAULT | HorizontalLayout
            arrayDetailOption.put("detail", "VerticalLayout");

            uiSchemaBuilder. //
                    addCategory("Signals"). //
                    addLayout(Layout.VERTICAL);

            JsonObjectBuilder layoutBuilder = Json.createObjectBuilder().add("type", "VerticalLayout");
            JsonArrayBuilder controlsArrayBuilder = Json.createArrayBuilder();
            controlsArrayBuilder //
                    .add(Json.createObjectBuilder() //
                            .add("type", "Control") //
                            .add("scope", "#/properties/name")//
                    );
            layoutBuilder.add("elements", controlsArrayBuilder);
            JsonObjectBuilder detailBuilder = Json.createObjectBuilder(). //
                    add("detail", layoutBuilder.build());

            uiSchemaBuilder.addDetailLayout("signals", "Signals", detailBuilder.build());

            // Schema builder
            schemaBuilder.addArray("signals");
            schemaBuilder.addProperty("name", "string", null, null);

            /*
             * create the data structure for the signals - each signal is represented as a
             * separate object
             */
            dataBuilder.addArray("signals");
            for (Signal bpmnSignal : model.getSignals()) {
                dataBuilder.addObject();
                dataBuilder.addData("name", bpmnSignal.getName());
                // the id is used to find the signal in the bpmn model later
                dataBuilder.addData("id", bpmnSignal.getId());
            }
            dataBuilder.closeArray();
        }
    }

    /**
     * Updates the BPMN Diagram definition properties
     */
    @Override
    public boolean updatePropertiesData(JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        boolean _update = false;

        Element definitions = modelState.getBpmnModel().getDefinitions();

        if ("General".equals(category)) {
            bpmnElement.setName(json.getString("name", ""));
            bpmnElement.setDocumentation(json.getString("documentation", ""));
        }
        if ("Definitions".equals(category)) {
            definitions.setAttribute("targetNamespace", json.getString("targetNamespace", ""));
            definitions.setAttribute("exporter", json.getString("exporter", ""));
            definitions.setAttribute("exporterVersion", json.getString("exporterVersion", ""));
        }

        if ("Layout".equals(category)) {
            String autoAlignOption = json.getString("auto-align", "true");
            modelState.setAutoAlign(Boolean.parseBoolean(autoAlignOption));
        }

        if ("Signals".equals(category)) {
            // update signal properties...
            logger.debug("      updating signals.. ");
            JsonArray signalSetValues = json.getJsonArray("signals");
            List<String> signalIdList = new ArrayList<String>();
            if (signalSetValues != null) {
                for (JsonValue signalValue : signalSetValues) {
                    JsonObject signalData = (JsonObject) signalValue;
                    String id = signalData.getString("id", null);
                    Signal signal = (Signal) modelState.getBpmnModel().findElementById(id);
                    if (signal != null) {
                        signalIdList.add(id);
                        // did the name change?
                        String newName = signalData.getString("name", "");
                        if (!newName.equals(signal.getName())) {
                            signal.setName(newName);
                            // Update the properties for all signal events!
                            updateSignalEvents();
                        }

                    } else {
                        // signal did not yet exist in definition list - so we create a new one
                        String newSignalID = generateUniqueSignalIdByMaxNumber(); // find an unique id...
                        String newSignalName = "Signal " + newSignalID.substring(7);
                        signalIdList.add(newSignalID);
                        try {
                            modelState.getBpmnModel().addSignal(newSignalID, newSignalName);
                        } catch (BPMNModelException e) {
                            logger.warn("Unable to add new signal: " + e.getMessage());
                        }
                        _update = true; // reset signal state
                    }
                }

                // remove signals from definitions which are not longer in the list
                // collect all affected ids...
                if (removeDeprecatedSignals(signalIdList)) {
                    _update = true;
                }

            } else {
                _update = true; // reset signal state
            }
            if (_update) {
                bpmnGModelFactory.applyBPMNElementExtensions(gNodeElement, bpmnElement);
            }
        }
        return _update;
    }

    /**
     * Remove signals from definitions which are not longer in a given list of
     * signal Ids
     * <p>
     * The method returns true if the list was updated
     * 
     */
    private boolean removeDeprecatedSignals(List<String> signalIdList) {
        boolean update = false;
        // remove signals from definitions which are not longer in the list
        // collect all affected ids...
        Iterator<Signal> _signalsIter = modelState.getBpmnModel().getSignals().iterator();
        List<String> _toRemove = new ArrayList<String>();
        while (_signalsIter.hasNext()) {
            Signal _signal = _signalsIter.next();
            if (!signalIdList.contains(_signal.getId())) {
                _toRemove.add(_signal.getId());
            }
        }
        // now remove all signals no longer listed in the properties
        for (String idToRemove : _toRemove) {
            modelState.getBpmnModel().deleteSignal(idToRemove);
            update = true;
        }
        // return final update state
        return update;
    }

    /**
     * This helper method updates the property data for all signal events. This
     * method is needed in case the name of an existing event changed.
     */
    private void updateSignalEvents() {
        Set<Event> events = modelState.getBpmnModel().findAllEvents();
        for (Event event : events) {
            // test if this event is an signal event
            Set<Element> signalEventDefinitions = event.getEventDefinitionsByType("signalEventDefinition");
            if (signalEventDefinitions.size() > 0) {
                // find the gmodel
                BPMNGNode _baseElement = (BPMNGNode) modelState.getIndex().get(event.getId()).orElse(null);
                if (_baseElement != null) {
                    bpmnGModelFactory.applyBPMNElementExtensions(_baseElement, event);
                }
            }
        }
    }

    /**
     * Helper Method to find the next free signal_n number to generate a unique id
     * 
     * @return
     */
    private String generateUniqueSignalIdByMaxNumber() {
        int maxNumber = modelState.getBpmnModel().getSignals().stream()
                .map(signal -> signal.getId())
                .filter(id -> id.startsWith("signal_"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(7));
                    } catch (NumberFormatException e) {
                        return 0; // ignore IDs that do not match the pattern
                    }
                })
                .max()
                .orElse(0);

        return "signal_" + (maxNumber + 1);
    }

}
