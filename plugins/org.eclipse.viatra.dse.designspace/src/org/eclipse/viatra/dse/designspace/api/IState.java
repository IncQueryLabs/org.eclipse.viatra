/*******************************************************************************
 * Copyright (c) 2010-2014, Miklos Foldenyi, Andras Szabolcs Nagy, Abel Hegedus, Akos Horvath, Zoltan Ujhelyi and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *   Miklos Foldenyi - initial API and implementation
 *   Andras Szabolcs Nagy - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.dse.designspace.api;

import java.util.Collection;

import org.eclipse.viatra.dse.statecode.IStateSerializer;

/**
 * Abstract representation of a model state in the design space exploration process. See {@link IDesignSpace} for
 * explanation of the general concept.
 * 
 * 
 */
public interface IState {

    /**
     * <p>
     * A {@link IState state} can be categorized with the following types:
     * </p>
     * 
     * <p>
     * A <b>traversed</b> state is a state that has been found and outgoing arcs have been set up, but does not satisfy
     * the conditions of either a goal or a cut-off state, thus further exploration is possible.
     * </p>
     * 
     * <p>
     * A <b>goal</b> state is a state which satisfies the specified goal conditions, but does not satisfy the conditions
     * of a cut-off state, making this state a possibly desirable outcome of the exploration process. A goal state shall
     * not be further explored.
     * </p>
     * 
     * <p>
     * A <b>cut</b> state is a state which satisfies the conditions of a cut-off state, making this state an undesirable
     * or invalid state. A cut state shall not be further explored, nor shall be reported as a goal regardless if it
     * satisfies the conditions for a goal.
     * </p>
     */
    enum TraversalStateType {
        TRAVERSED, GOAL, CUT
    }

    /**
     * Getter of {@link TraversalStateType traversal type}.
     * 
     * @return the {@link TraversalStateType traversal type} of this state.
     */
    TraversalStateType getTraversalState();

    /**
     * Setter of {@link TraversalStateType traversal type}.
     * 
     * @param traversalState
     *            the new {@link TraversalStateType traversal type}.
     */
    void setTraversalState(TraversalStateType traversalState);

    /**
     * Every {@link IState state} has a unique id that was generated by a {@link IStateSerializer serializer}. An
     * attempt to add a different {@link IState state} to the {@link IDesignSpace design space} with the same id will be
     * ignored. See {@link IDesignSpace#addState(ITransition, Object, java.util.Map)} and
     * {@link IStateSerializer#serializeContainmentTree()} for more details.
     * 
     * @return the id of the {@link IState}.
     */
    Object getId();

    /**
     * Returns the collection of the already discovered incoming {@link ITransition transitions} through which this
     * state can be reached.
     * 
     * @return a modifiable {@link Collection} of {@link ITransition}s.
     */
    Collection<? extends ITransition> getIncomingTransitions();

    /**
     * Returns the collection of the outgoing {@link ITransition transitions} through which this state can be left.
     * 
     * @return a modifiable {@link Collection} of {@link ITransition}s.
     */
    Collection<? extends ITransition> getOutgoingTransitions();

    /**
     * @return true if the {@link IState state} has been initialized already.
     */
    boolean isProcessed();

    /**
     * Mark the {@link IState state} as fully initialized.
     */
    void setProcessed();
}