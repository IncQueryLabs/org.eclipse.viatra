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

package org.eclipse.viatra.query.runtime.rete.single;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.viatra.query.runtime.matchers.context.IPosetComparator;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.matchers.tuple.TupleMask;
import org.eclipse.viatra.query.runtime.rete.network.Production;
import org.eclipse.viatra.query.runtime.rete.network.ReteContainer;
import org.eclipse.viatra.query.runtime.rete.traceability.CompiledQuery;
import org.eclipse.viatra.query.runtime.rete.traceability.TraceInfo;

/**
 * Default implementation of the Production node, based on UniquenessEnforcerNode
 *
 * @author Gabor Bergmann
 */
public class DefaultProductionNode extends UniquenessEnforcerNode implements Production {

    protected Map<String, Integer> posMapping;

    public DefaultProductionNode(ReteContainer reteContainer, Map<String, Integer> posMapping,
            boolean deleteRederiveEvaluation) {
        this(reteContainer, posMapping, deleteRederiveEvaluation, null, null, null);
    }

    public DefaultProductionNode(ReteContainer reteContainer, Map<String, Integer> posMapping,
            boolean deleteRederiveEvaluation, TupleMask coreMask, TupleMask posetMask,
            IPosetComparator posetComparator) {
        super(reteContainer, posMapping.size(), deleteRederiveEvaluation, coreMask, posetMask, posetComparator);
        this.posMapping = posMapping;
    }

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

    @Override
    public String toString() {
        for (TraceInfo traceInfo : this.traceInfos) {
            if (traceInfo instanceof CompiledQuery) {
                String patternName = ((CompiledQuery) traceInfo).getPatternName();
                return String.format("ProductionNode<%s>=%s", patternName, super.toString());
            }
        }
        return super.toString();
    }

}
