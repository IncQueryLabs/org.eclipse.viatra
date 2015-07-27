/*******************************************************************************
 * Copyright (c) 2004-2014, Istvan David, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Istvan David - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.cep.core.api.patterns;

import java.util.List;

import org.eclipse.viatra.cep.core.metamodels.automaton.Automaton;
import org.eclipse.viatra.cep.core.metamodels.automaton.EventToken;
import org.eclipse.viatra.cep.core.metamodels.automaton.ParameterTable;
import org.eclipse.viatra.cep.core.metamodels.automaton.TrapState;
import org.eclipse.viatra.cep.core.metamodels.events.Event;
import org.eclipse.viatra.cep.core.metamodels.events.EventPattern;

/**
 * An {@link EventPattern} instance that cannot be matched because of an error that pushed the related
 * {@link EventToken} into the {@link TrapState} of the appropriate {@link Automaton}.
 * 
 * <p>
 * An object of this type should be instantiated at runtime when an {@link EventPattern} is placed into the
 * {@link TrapState}.
 * 
 * @author Istvan David
 * 
 */
public class InTrapComplexEventPattern implements IObservableComplexEventPattern {

    private Automaton automaton;
    private String observableEventPatternId;
    private List<Event> observedAtomicEventInstances;
    private ParameterTable parameterTable;

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }

    @Override
    public String getObservedEventPatternId() {
        return observableEventPatternId;
    }

    @Override
    public List<Event> getObservedAtomicEventInstances() {
        return this.observedAtomicEventInstances;
    }

    @Override
    public ParameterTable getParameterTable() {
        return parameterTable;
    }

    public InTrapComplexEventPattern(Automaton automaton, EventToken eventToken) {
        this.observedAtomicEventInstances = eventToken.getRecordedEvents();
        this.parameterTable = eventToken.getParameterTable();
        this.automaton = automaton;
        this.observableEventPatternId = automaton.getEventPatternId();
    }

}
