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
import org.eclipse.incquery.runtime.matchers.tuple.TupleMask;
import org.eclipse.incquery.runtime.rete.network.Direction;
import org.eclipse.incquery.runtime.rete.network.Receiver;
import org.eclipse.incquery.runtime.rete.network.ReteContainer;
import org.eclipse.incquery.runtime.rete.network.StandardNode;
import org.eclipse.incquery.runtime.rete.traceability.TraceInfo;
import org.eclipse.incquery.runtime.rete.util.Options;

/**
 * Abstract superclass for nodes with two inputs that are matched against each other.
 * 
 * @author Gabor Bergmann
 */
public abstract class DualInputNode extends StandardNode /* implements Pullable */{

    public IterableIndexer getPrimarySlot() {
        return primarySlot;
    }

    public Indexer getSecondarySlot() {
        return secondarySlot;
    }

    /**
     * @author Gabor Bergmann
     * 
     */
    public enum Side {
        PRIMARY, SECONDARY, BOTH;

        public Side opposite() {
            switch (this) {
            case PRIMARY:
                return SECONDARY;
            case SECONDARY:
                return PRIMARY;
            case BOTH:
                return BOTH;
            default:
                return BOTH;
            }
        }
    }

    /**
     * Holds the primary input slot of this node.
     */
    protected IterableIndexer primarySlot;

    /**
     * Holds the secondary input slot of this node.
     */
    protected Indexer secondarySlot;

    /**
     * Optional complementer mask
     */
    protected TupleMask complementerSecondaryMask;

    /**
     * true if the primary and secondary slots coincide
     */
    protected boolean coincidence;

    /**
     * @param reteContainer
     */
    public DualInputNode(ReteContainer reteContainer, TupleMask complementerSecondaryMask) {
        super(reteContainer);
        this.complementerSecondaryMask = complementerSecondaryMask;
        //connectToIndexers(primarySlot, secondarySlot);
    }

    /**
     * Should be called only once, when node is initialized
     */
	public void connectToIndexers(IterableIndexer primarySlot, Indexer secondarySlot) {
		this.primarySlot = primarySlot;
        this.secondarySlot = secondarySlot;
  
        // beta nodes are stateless, no need to synch contents to children...
        // ...unless in a weird corner case of recursive queries, when there are already receivers attached
        // so we prepare contents to be synced NOW
        Collection<Tuple> contentsToSync = 
        		getReceivers().isEmpty()? null 
        				: reteContainer.pullContents(this);       
        
        // attach listeners
        // if there is syncing, do this after the flush done for pulling, but before syncing updates
        coincidence = primarySlot.equals(secondarySlot); 
        final DualInputNode me = this;
        if (!coincidence) { // regular case
            primarySlot.attachListener(new DefaultIndexerListener(this) {
                public void notifyIndexerUpdate(Direction direction, Tuple updateElement, Tuple signature,
                        boolean change) {
                    notifyUpdate(Side.PRIMARY, direction, updateElement, signature, change);
                }

                @Override
                public String toString() {
                    return "primary@" + me;
                }
            });
            secondarySlot.attachListener(new DefaultIndexerListener(this) {
                public void notifyIndexerUpdate(Direction direction, Tuple updateElement, Tuple signature,
                        boolean change) {
                    notifyUpdate(Side.SECONDARY, direction, updateElement, signature, change);
                }

                @Override
                public String toString() {
                    return "secondary@" + me;
                }
            });
        } else { // if the two slots are the same, updates have to be handéed carefully
            primarySlot.attachListener(new DefaultIndexerListener(this) {
                public void notifyIndexerUpdate(Direction direction, Tuple updateElement, Tuple signature,
                        boolean change) {
                    notifyUpdate(Side.BOTH, direction, updateElement, signature, change);
                }

                @Override
                public String toString() {
                    return "both@" + me;
                }
            });
        }
        
        // synch contents to receivers now... if any
        if (contentsToSync != null) {
    		for (Receiver receiver : getReceivers()) {			
    			reteContainer.sendConstructionUpdates(receiver, Direction.INSERT, contentsToSync);
    		}
    		reteContainer.flushUpdates();
        }

   	}

	// public Indexer createPrimarySlot(TupleMask mask)
    // {
    // return accessSlot(mask, Side.PRIMARY);
    // }
    //
    // public Indexer createSecondarySlot(TupleMask mask)
    // {
    // return accessSlot(mask, Side.SECONDARY);
    // }
    //
    // public Indexer accessSlot(TupleMask mask, Side side)
    // {
    // Indexer slot = slots.get(side);
    // if (slot == null)
    // {
    // slot = new Indexer(network, mask);
    // slot.attachListener(this, side);
    // slots.put(side, slot);
    // }
    // return slot;
    // }

    /**
     * Helper: retrieves all stored substitutions from the opposite side memory. The results are copied into a new
     * collection; this step has a performance penalty, but avoids ConcurrentModificaton Exceptions when loops are
     * present.
     * 
     * @return the collection of opposite substitutions if any, or null if none
     */
    protected Collection<Tuple> retrieveOpposites(Side side, Tuple signature) {
        Collection<Tuple> opposites = getSlot(side.opposite()).get(signature);
        return opposites;
        // if (opposites!=null) return new LinkedList<Tuple>(opposites);
        // else return null;
    }

    /**
     * Helper: unifies a left and right partial matching.
     */
    protected Tuple unify(Tuple left, Tuple right) {
        // if (complementerSecondaryMask == null)
        // return secondarySlot.getMask().combine(left, right,
        // Options.enableInheritance, false);
        // else
        return complementerSecondaryMask.combine(left, right, Options.enableInheritance, true);
    }

    /**
     * Helper: unifies the a substitution from the specifies side with another substitution from the other side.
     */
    protected Tuple unify(Side side, Tuple ps, Tuple opposite) {
        switch (side) {
        case PRIMARY:
            return unify(ps, opposite);
        case SECONDARY:
            return unify(opposite, ps);
        case BOTH:
            return unify(ps, opposite);
        default:
            return null;
        }
    }

    /**
     * Abstract handler for update event.
     * 
     * @param side
     *            The side on which the event occured.
     * @param direction
     *            The direction of the update.
     * @param updateElement
     *            The partial matching that is inserted.
     * @param signature
     *            Masked signature of updateElement.
     * @param change
     *            Indicates whether this is/was the first/last instance of this signature in this slot.
     */
    public abstract void notifyUpdate(Side side, Direction direction, Tuple updateElement, Tuple signature,
            boolean change);

    /**
     * Simulates the behaviour of the node for calibration purposes only.
     */
    public abstract Tuple calibrate(Tuple primary, Tuple secondary);

    /**
     * @param complementerSecondaryMask
     *            the complementerSecondaryMask to set
     */
    public void setComplementerSecondaryMask(TupleMask complementerSecondaryMask) {
        this.complementerSecondaryMask = complementerSecondaryMask;
    }

    /**
     * Retrieves the slot corresponding to the specified side.
     */
    protected Indexer getSlot(Side side) {
        if (side == Side.SECONDARY)
            return secondarySlot;
        else
            return primarySlot;
    }
    
    @Override
    public void assignTraceInfo(TraceInfo traceInfo) {
    	super.assignTraceInfo(traceInfo);
    	if (traceInfo.propagateToIndexerParent()) {
    		if (primarySlot != null)
    			primarySlot.acceptPropagatedTraceInfo(traceInfo);
    		if (secondarySlot != null)
    			secondarySlot.acceptPropagatedTraceInfo(traceInfo);
    	}
    }

}
