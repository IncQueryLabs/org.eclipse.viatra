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

package org.eclipse.incquery.runtime.rete.index;

import java.util.Collection;

import org.eclipse.incquery.runtime.matchers.tuple.Tuple;
import org.eclipse.incquery.runtime.rete.network.Direction;
import org.eclipse.incquery.runtime.rete.network.ReteContainer;

/**
 * Propagates all substitutions arriving at the PRIMARY slot if and only if (a matching substitution on the SECONDARY is
 * present) xor (NEGATIVE).
 * 
 * The negative parameter specifies whether this node checks for existence or non-existence.
 * 
 * @author Gabor Bergmann
 */
public class ExistenceNode extends DualInputNode {

    protected boolean negative;

    /**
     * @param reteContainer
     * @param negative
     *            if false, act as axistence checker, otherwise a nonexistence-checker
     */
    public ExistenceNode(ReteContainer reteContainer,
            boolean negative) {
        super(reteContainer, null);
        this.negative = negative;
    }

    @Override
    public Tuple calibrate(Tuple primary, Tuple secondary) {
        return primary;
    }

    @Override
    public void notifyUpdate(Side side, Direction direction, Tuple updateElement, Tuple signature, boolean change) {
        switch (side) {
        case PRIMARY:
            if ((retrieveOpposites(side, signature) != null) ^ negative)
                propagateUpdate(direction, updateElement);
            break;
        case SECONDARY:
            if (change) {
                Collection<Tuple> opposites = retrieveOpposites(side, signature);
                if (opposites != null)
                    for (Tuple opposite : opposites) {
                        propagateUpdate((negative ? direction.opposite() : direction), opposite);
                    }
            }
            break;
        case BOTH:
            // in case the slots coincide,
            // negative --> always empty
            // !positive --> identity
            if (!negative) {
                propagateUpdate(direction, updateElement);
            }
            break;
        }
    }

    @Override
    public void pullInto(Collection<Tuple> collector) {
    	if (primarySlot == null || secondarySlot == null) return;
        reteContainer.flushUpdates();

        for (Tuple signature : primarySlot.getSignatures()) {
            Collection<Tuple> primaries = primarySlot.get(signature); // not null due to the contract of
                                                                      // IterableIndex.getSignatures()
            Collection<Tuple> opposites = secondarySlot.get(signature);
            if ((opposites != null) ^ negative)
                collector.addAll(primaries);
        }
    }

}
