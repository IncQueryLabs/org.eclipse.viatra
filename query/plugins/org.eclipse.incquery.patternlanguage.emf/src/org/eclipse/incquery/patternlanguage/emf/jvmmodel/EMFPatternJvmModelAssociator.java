/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.patternlanguage.emf.jvmmodel;

import org.eclipse.incquery.patternlanguage.emf.util.IErrorFeedback;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;

import com.google.inject.Inject;

/**
 * This subClass is needed for local variable scoping. PatternBody not associated with any Inferred classes.
 *
 * @author Mark Czotter
 *
 */
public class EMFPatternJvmModelAssociator extends JvmModelAssociator {

    @Inject
    private IErrorFeedback feedback;

    @Override
    public void installDerivedState(DerivedStateAwareResource resource, boolean preIndexingPhase) {
        feedback.clearMarkers(resource, IErrorFeedback.JVMINFERENCE_ERROR_TYPE);
        super.installDerivedState(resource, preIndexingPhase);
    }

}
