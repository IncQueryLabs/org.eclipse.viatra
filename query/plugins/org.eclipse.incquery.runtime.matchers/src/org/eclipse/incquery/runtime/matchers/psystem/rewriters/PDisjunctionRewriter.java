/*******************************************************************************
 * Copyright (c) 2010-2014, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.matchers.psystem.rewriters;

import org.eclipse.incquery.runtime.matchers.psystem.queries.PDisjunction;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;

/**
 * An abstract base class for creating alternative representations for PDisjunctions.
 * @author Zoltan Ujhelyi
 *
 */
public abstract class PDisjunctionRewriter {

    public abstract PDisjunction rewrite(PDisjunction disjunction) throws RewriterException;
    
    public PDisjunction rewrite(PQuery query) throws RewriterException {
        return rewrite(query.getDisjunctBodies());
    }
}
