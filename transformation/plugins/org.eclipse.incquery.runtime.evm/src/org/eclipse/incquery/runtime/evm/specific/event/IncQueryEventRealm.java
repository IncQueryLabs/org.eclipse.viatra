/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.evm.specific.event;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.evm.api.event.EventRealm;
import org.eclipse.incquery.runtime.evm.api.event.EventSourceSpecification;
import org.eclipse.incquery.runtime.exception.IncQueryException;

public class IncQueryEventRealm implements EventRealm {

    private IncQueryEngine engine;
    
    protected IncQueryEventRealm(IncQueryEngine engine) {
        checkArgument(engine != null, "Cannot create event realm for null engine!");
        this.engine = engine;
    }
    
    public IncQueryEngine getEngine() {
        return engine;
    }

    protected <Match extends IPatternMatch> IncQueryEventSource<Match> createSource(
            EventSourceSpecification<Match> sourceSpecification) throws IncQueryException {
        checkArgument(sourceSpecification instanceof IncQueryEventSourceSpecification,
                "Source definition must be IncQueryEventSourceSpecification!");
        IncQueryEventSource<Match> eventSource = new IncQueryEventSource<Match>(this,
                (IncQueryEventSourceSpecification<Match>) sourceSpecification);
        eventSource.prepareSource();
        return eventSource;
    };
    
    
    public static IncQueryEventRealm create(IncQueryEngine engine) {
        return new IncQueryEventRealm(engine);
    }

    public static <Match extends IPatternMatch> IncQueryEventSourceSpecification<Match> createSourceSpecification(IQuerySpecification<? extends IncQueryMatcher<Match>> factory){
        return new IncQueryEventSourceSpecification<Match>(factory);
    }
    
}
