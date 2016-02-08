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
package org.eclipse.incquery.runtime.evm.api.event;


/**
 * Basic interface that represents a source that is able to send notifications on events
 * 
 * @author Abel Hegedus
 *
 */
public interface EventSource<EventAtom> {
    
    EventSourceSpecification<EventAtom> getSourceSpecification();

    EventRealm getRealm();
    
    void dispose();
}
