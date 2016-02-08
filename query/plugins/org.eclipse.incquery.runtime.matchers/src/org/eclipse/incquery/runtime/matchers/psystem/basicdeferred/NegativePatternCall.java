/*******************************************************************************
 * Copyright (c) 2004-2010 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.matchers.psystem.basicdeferred;

import java.util.Collections;
import java.util.Set;

import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

/**
 * @author Gabor Bergmann
 * 
 */
public class NegativePatternCall extends PatternCallBasedDeferred {

    public NegativePatternCall(PBody pBody, Tuple actualParametersTuple, PQuery query) {
        super(pBody, actualParametersTuple, query);
    }

    @Override
    public Set<PVariable> getDeducedVariables() {
        return Collections.emptySet();
    }

    /**
     * @return all variables that may potentially be quantified they are not used anywhere else
     */
    @Override
    protected Set<PVariable> getCandidateQuantifiedVariables() {
        return getAffectedVariables();
    }

    @Override
    protected void doDoReplaceVariables(PVariable obsolete, PVariable replacement) {
    }

    @Override
    protected String toStringRest() {
        return "!" + query.getFullyQualifiedName() + "@" + actualParametersTuple.toString();
    }

}
