/*******************************************************************************
 * Copyright (c) 2010-2015, Marton Bur, Zoltan Ujhelyi, Akos Horvath, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marton Bur - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.matchers.psystem.rewriters;

import org.eclipse.incquery.runtime.matchers.psystem.basicenumerables.PositivePatternCall;


/**
 * Interface used by the PQueryFlattener to decide which pattern calls to flatten
 * 
 * @author Marton Bur
 *
 */
public interface IFlattenCallPredicate {
	
	/**
     * Decides whether the called query by the pattern call should be flattened into the caller or not.
     * 
     * @param positivePatternCall
     *            the pattern call
     * @return true if the call should be flattened
     */
	boolean shouldFlatten(PositivePatternCall positivePatternCall);
	
}
