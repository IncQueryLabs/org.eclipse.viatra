/*******************************************************************************
 * Copyright (c) 2010-2014, Marton Bur, Akos Horvath, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marton Bur - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.localsearch.planner.cost;

import org.eclipse.incquery.runtime.matchers.planning.SubPlan;
import org.eclipse.incquery.runtime.matchers.psystem.PConstraint;

/**
 * 
 * @author Marton Bur
 *
 * @deprecated This is used by {@link org.eclipse.incquery.runtime.localsearch.planner.LocalSearchPlannerStrategy}. 
 * Use the {@link org.eclipse.incquery.runtime.localsearch.planner.LocalSearchRuntimeBasedStrategy} and its belonging components instead.
 */
@Deprecated
public interface ICostEstimator {

    /**
     * According to the current variable binding estimates the cost of the application of the given constraint
     * 
     * @param currentPlan
     * @param patternConstraint
     */
    public double getCost(SubPlan currentPlan, PConstraint patternConstraint);

}
