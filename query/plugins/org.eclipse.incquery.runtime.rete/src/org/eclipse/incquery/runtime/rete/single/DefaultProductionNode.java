/*******************************************************************************
 * Copyright (c) 2004-2008 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.rete.single;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.rete.network.Production;
import org.eclipse.incquery.runtime.rete.network.ReteContainer;
import org.eclipse.incquery.runtime.rete.traceability.TraceInfo;

/**
 * Default implementation of the Production node, based on UniquenessEnforcerNode
 *
 * @author Gabor Bergmann
 */
public class DefaultProductionNode extends UniquenessEnforcerNode implements Production {

    protected Map<String, Integer> posMapping;

    // protected HashMap<TupleMask, Indexer> projections;

    /**
     * @param reteContainer
     * @param posMapping
     */
    public DefaultProductionNode(ReteContainer reteContainer, Map<String, Integer> posMapping) {
        super(reteContainer, posMapping.size());
        this.posMapping = posMapping;
        // this.projections= new HashMap<TupleMask, Indexer>();
    }

    /**
     * @return the posMapping
     */
    public Map<String, Integer> getPosMapping() {
        return posMapping;
    }

    public Iterator<Tuple> iterator() {
        return memory.iterator();
    }

    @Override
    public void acceptPropagatedTraceInfo(TraceInfo traceInfo) {
    	if (traceInfo.propagateToProductionNodeParentAlso())
    		super.acceptPropagatedTraceInfo(traceInfo);
    }
}
