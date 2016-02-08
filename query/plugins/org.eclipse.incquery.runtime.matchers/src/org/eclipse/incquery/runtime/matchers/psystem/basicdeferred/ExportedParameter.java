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

import org.eclipse.incquery.runtime.matchers.planning.QueryProcessingException;
import org.eclipse.incquery.runtime.matchers.psystem.PBody;
import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.VariableDeferredPConstraint;

/**
 * @author Gabor Bergmann
 *
 */
public class ExportedParameter extends VariableDeferredPConstraint {
    PVariable parameterVariable;
    String parameterName;

    public ExportedParameter(PBody pBody, PVariable parameterVariable,
            String parameterName) {
        super(pBody, Collections.singleton(parameterVariable));
        this.parameterVariable = parameterVariable;
        this.parameterName = parameterVariable.getName();
    }

    @Override
    public void doReplaceVariable(PVariable obsolete, PVariable replacement) {
        if (obsolete.equals(parameterVariable))
            parameterVariable = replacement;
    }

    @Override
    protected String toStringRest() {
        Object varName = parameterVariable.getName();
        return parameterName.equals(varName) ? parameterName : parameterName + "(" + varName + ")";
    }

    @Override
    public Set<PVariable> getDeducedVariables() {
        return Collections.emptySet();
    }

    public String getParameterName() {
        return parameterName;
    }

    public PVariable getParameterVariable() {
        return parameterVariable;
    }

    @Override
    public Set<PVariable> getDeferringVariables() {
        return Collections.singleton(parameterVariable);
    }

    @Override
    public void checkSanity() throws QueryProcessingException {
        super.checkSanity();
        if (!parameterVariable.isDeducable()) {
            String[] args = { parameterName };
            String msg = "Impossible to match pattern: "
                    + "exported pattern variable {1} can not be determined based on the pattern constraints. "
                    + "HINT: certain constructs (e.g. negative patterns or check expressions) cannot output symbolic parameters.";
            String shortMsg = "Could not deduce value of parameter";
            throw new QueryProcessingException(msg, args, shortMsg, null);
        }

    }

}
