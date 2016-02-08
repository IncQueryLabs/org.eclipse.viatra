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
package org.eclipse.incquery.patternlanguage.emf.internal;

import org.apache.log4j.Logger;
import org.eclipse.incquery.patternlanguage.emf.EMFPatternLanguageStandaloneSetup;
import org.eclipse.incquery.runtime.util.IncQueryLoggingUtil;

import com.google.inject.Injector;

/**
 * A singleton provider for Xtext injectors
 *
 * @author Zoltan Ujhelyi
 *
 */
public final class XtextInjectorProvider {

    public final static XtextInjectorProvider INSTANCE = new XtextInjectorProvider();
    private Injector injector;

    private XtextInjectorProvider() {
    }

    public Injector getInjector() {
        return injector;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
        IncQueryLoggingUtil.setExternalLogger(injector.getInstance(Logger.class));
    }

    public void initializeHeadlessInjector() {
        EMFPatternLanguageStandaloneSetup setup = new EMFPatternLanguageStandaloneSetup();
        injector = setup.createInjectorAndDoEMFRegistration();
    }
}
