/*******************************************************************************
 * Copyright (c) 2010-2014, Marton Bur, Akos Horvath, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marton Bur, Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.tooling.localsearch.ui;
import org.eclipse.incquery.runtime.extensibility.QueryBackendRegistry;
import org.eclipse.incquery.runtime.localsearch.matcher.integration.LocalSearchBackend;
import org.eclipse.incquery.runtime.localsearch.matcher.integration.LocalSearchBackendFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Marton Bur, Zoltan Ujhelyi
 *
 */
public class LocalSearchToolingActivator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.incquery.tooling.localsearch.ui";
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        //Register local search backend inside the engine
        QueryBackendRegistry.getInstance().registerQueryBackendFactory(LocalSearchBackend.class, LocalSearchBackendFactory.INSTANCE);
    }

}
