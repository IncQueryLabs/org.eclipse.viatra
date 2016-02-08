/*******************************************************************************
 * Copyright (c) 2004-2009 Gabor Bergmann and Daniel Varro
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
import org.eclipse.incquery.runtime.matchers.tuple.TupleMask;
import org.eclipse.incquery.runtime.rete.network.Direction;
import org.eclipse.incquery.runtime.rete.network.ReteContainer;

/**
 * Performs a left outer join.
 * 
 * @author Gabor Bergmann
 * 
 */
public class OuterJoinNode extends DualInputNode {
    final Tuple defaults;

    /**
     * @param reteContainer
     * @param complementerSecondaryMask
     * @param defaults
     *            the default line to use instead of missing elements if a left tuple has no match
     * 
     */
    public OuterJoinNode(ReteContainer reteContainer,
            TupleMask complementerSecondaryMask, Tuple defaults) {
        super(reteContainer, complementerSecondaryMask);
        this.defaults = defaults;
    }

    @Override
    public Tuple calibrate(Tuple primary, Tuple secondary) {
        return unify(primary, secondary);
    }

    @Override
    public void notifyUpdate(Side side, Direction direction, Tuple updateElement, Tuple signature, boolean change) {
        Collection<Tuple> opposites = retrieveOpposites(side, signature);
        switch (side) {
        case PRIMARY:
            if (opposites != null)
                for (Tuple opposite : opposites) {
                    propagateUpdate(direction, unify(updateElement, opposite));
                }
            else
                propagateUpdate(direction, unifyWithDefaults(updateElement));
            break;
        case SECONDARY:
            if (opposites != null)
                for (Tuple opposite : opposites) {
                    propagateUpdate(direction, unify(opposite, updateElement));
                    if (change)
                        propagateUpdate(direction.opposite(), unifyWithDefaults(opposite));
                }
            break;
        case BOTH:
            for (Tuple opposite : opposites) {
                propagateUpdate(direction, unify(updateElement, opposite));
                if (updateElement.equals(opposite))
                    continue;
                propagateUpdate(direction, unify(opposite, updateElement));
            }
            if (direction == Direction.REVOKE) // missed joining with itself
                propagateUpdate(direction, unify(updateElement, updateElement));
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
            if (opposites != null)
                for (Tuple ps : primaries)
                    for (Tuple opposite : opposites) {
                        collector.add(unify(ps, opposite));
                    }
            else
                for (Tuple ps : primaries) {
                    collector.add(unifyWithDefaults(ps));
                }
        }
    }

    private Tuple unifyWithDefaults(Tuple ps) {
        return unify(ps, defaults);
    }

}
