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

package org.eclipse.viatra.cep.core.api.helpers;

import java.util.List;

import org.eclipse.viatra.cep.core.api.engine.CEPEngine;
import org.eclipse.viatra.cep.core.api.rules.ICepRule;
import org.eclipse.viatra.cep.core.metamodels.automaton.EventContext;
import org.eclipse.viatra.cep.core.streams.EventStream;

/**
 * Helper class for CEP applications. By extending it, a {@link CEPEngine} and an {@link EventStream} is available for
 * use. The user must implement the {@link DefaultApplication#configureRules()} method by specifying the rules to be
 * instantiated for the engine.
 * 
 * @author Istvan David
 * 
 */
public abstract class DefaultApplication {
    private CEPEngine engine;
    private EventStream eventStream;

    public DefaultApplication(EventContext eventContext, List<Class<? extends ICepRule>> rules) {
        engine = CEPEngine.newEngine().eventContext(eventContext).rules(rules).prepare();
        eventStream = engine.getStreamManager().newEventStream();
    }
    
    public DefaultApplication(EventContext eventContext, Class<? extends ICepRule> rule) {
        engine = CEPEngine.newEngine().eventContext(eventContext).rule(rule).prepare();
        eventStream = engine.getStreamManager().newEventStream();
    }

    public CEPEngine getEngine() {
        return engine;
    }

    public EventStream getEventStream() {
        return eventStream;
    }
}
