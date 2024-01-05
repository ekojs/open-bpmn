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
import {
    CircularNodeView,
    ConsoleLogger,
    ContainerConfiguration,
    DefaultTypes,
    DeleteElementContextMenuItemProvider,
    DiamondNodeView,
    GLSPGraph,
    LogLevel,
    RectangularNodeView,
    RoundedCornerNodeView,
    SCompartment,
    SCompartmentView,
    SLabel,
    SLabelView,
    TYPES,
    configureCommand,
    configureDefaultModelElements,
    configureModelElement,
    configureView,
    editLabelFeature,
    initializeDiagramContainer,
    moveFeature,
    overrideViewerOptions,
    selectFeature
} from '@eclipse-glsp/client';
import {
    BPMNEdge,
    DataObjectNode,
    EventNode,
    GatewayNode,
    Icon, LabelNode,
    LaneDivider,
    LaneNode,
    MessageNode,
    MultiLineTextNode,
    PoolNode, TaskNode,
    TextAnnotationNode
} from '@open-bpmn/open-bpmn-model';
import 'balloon-css/balloon.min.css';
import { Container, ContainerModule } from 'inversify';
import 'sprotty/css/edit-label.css';
import '../css/diagram.css';
import {
    BPMNGraphView,
    DataObjectNodeView,
    IconView,
    LaneDividerView,
    LaneHeaderView,
    MessageNodeView,
    MultiLineTextNodeView,
    PoolHeaderView,
    TaskNodeView,
    TextAnnotationNodeView
} from './bpmn-element-views';
import {
    BPMNElementSnapper,
    DrawHelperLinesCommand,
    HelperLineListener,
    HelperLineView,
    RemoveHelperLinesCommand
} from './bpmn-helperlines';
import { BPMNEdgeView } from './bpmn-routing-views';
import {
    BPMNLabelNodeSelectionListener,
    BPMNMultiNodeSelectionListener
} from './bpmn-select-listeners';

import {
    BPMNPropertiesMouseListener,
    BPMNPropertyModule
} from '@open-bpmn/open-bpmn-properties';


const bpmnDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
    const context = { bind, unbind, isBound, rebind };

    rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
    rebind(TYPES.LogLevel).toConstantValue(LogLevel.warn);
    bind(TYPES.ISnapper).to(BPMNElementSnapper);

    // We do not whant a reveal action in BPMN
    // bind(TYPES.ICommandPaletteActionProvider).to(RevealNamedElementActionProvider);

    bind(TYPES.IContextMenuItemProvider).to(DeleteElementContextMenuItemProvider);

    // bind new SelectionListener for BPMNLabels and BoundaryEvents
    bind(TYPES.SelectionListener).to(BPMNLabelNodeSelectionListener);
    bind(TYPES.SelectionListener).to(BPMNMultiNodeSelectionListener);
    bind(TYPES.MouseListener).to(BPMNPropertiesMouseListener);

    // bpmn helper lines
    bind(TYPES.MouseListener).to(HelperLineListener);
    configureCommand({ bind, isBound }, DrawHelperLinesCommand);
    configureCommand({ bind, isBound }, RemoveHelperLinesCommand);
    configureView({ bind, isBound }, 'helpline', HelperLineView);
    configureDefaultModelElements(context);

    configureModelElement(context, DefaultTypes.GRAPH, GLSPGraph, BPMNGraphView);

    configureModelElement(context, 'task', TaskNode, TaskNodeView);
    configureModelElement(context, 'manualTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'userTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'scriptTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'businessRuleTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'serviceTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'sendTask', TaskNode, TaskNodeView);
    configureModelElement(context, 'receiveTask', TaskNode, TaskNodeView);

    configureModelElement(context, 'startEvent', EventNode, CircularNodeView);
    configureModelElement(context, 'endEvent', EventNode, CircularNodeView);
    configureModelElement(context, 'intermediateCatchEvent', EventNode, CircularNodeView);
    configureModelElement(context, 'intermediateThrowEvent', EventNode, CircularNodeView);
    configureModelElement(context, 'boundaryEvent', EventNode, CircularNodeView);

    configureModelElement(context, 'exclusiveGateway', GatewayNode, DiamondNodeView);
    configureModelElement(context, 'inclusiveGateway', GatewayNode, DiamondNodeView);
    configureModelElement(context, 'parallelGateway', GatewayNode, DiamondNodeView);
    configureModelElement(context, 'eventBasedGateway', GatewayNode, DiamondNodeView);
    configureModelElement(context, 'complexGateway', GatewayNode, DiamondNodeView);

    configureModelElement(context, 'label:heading', SLabel, SLabelView, { enable: [editLabelFeature] });

    configureModelElement(context, 'comp:comp', SCompartment, SCompartmentView);
    configureModelElement(context, 'icon', Icon, IconView);

    // configureModelElement(context, 'comp:header', SCompartment, ContainerHeaderView);
    configureModelElement(context, 'pool_header', SCompartment, PoolHeaderView);
    configureModelElement(context, 'lane_header', SCompartment, LaneHeaderView);

    configureModelElement(context, 'pool', PoolNode, RoundedCornerNodeView, { disable: [moveFeature] });
    configureModelElement(context, 'lane', LaneNode, RoundedCornerNodeView, {
        disable: [moveFeature, selectFeature]
    }
    );

    configureModelElement(context, 'dataObject', DataObjectNode, DataObjectNodeView);
    configureModelElement(context, 'message', MessageNode, MessageNodeView);
    configureModelElement(context, 'textAnnotation', TextAnnotationNode, TextAnnotationNodeView);

    configureModelElement(context, 'BPMNLabel', LabelNode, RectangularNodeView);
    configureModelElement(context, 'bpmn-text-node', MultiLineTextNode, MultiLineTextNodeView);
    configureModelElement(context, 'lane-divider', LaneDivider, LaneDividerView);
    configureModelElement(context, 'container', SCompartment, SCompartmentView);

    // Sequence flows
    configureModelElement(context, 'sequenceFlow', BPMNEdge, BPMNEdgeView);
    configureModelElement(context, 'messageFlow', BPMNEdge, BPMNEdgeView);
    configureModelElement(context, 'association', BPMNEdge, BPMNEdgeView);

});

/*
 * Create the createClientContainer with the diagramModule and the BPMN bpmnPropertyModule...
 */
// export default function createBPMNDiagramContainer(widgetId: string): Container {
// 	// Note: the widgetId is generated by the GLSP core and is something like 'bpmn-diagram_0'
//     const container = createDiagramContainer(bpmnDiagramModule, BPMNPropertyModule);
//     overrideViewerOptions(container, {
//         baseDiv: widgetId,
//         hiddenDiv: widgetId + '_hidden'
//     });
//     return container;
// }

export function createBPMNDiagramContainer(widgetId: string, ...containerConfiguration: ContainerConfiguration): Container {
    return initializeBPMNDiagramContainer(new Container(), widgetId, ...containerConfiguration);
}

export function initializeBPMNDiagramContainer(
    container: Container,
    widgetId: string,
    ...containerConfiguration: ContainerConfiguration
): Container {
    initializeDiagramContainer(container, bpmnDiagramModule, BPMNPropertyModule, ...containerConfiguration);
    overrideViewerOptions(container, {
        baseDiv: widgetId,
        hiddenDiv: widgetId + '_hidden'
    });
    return container;
}
