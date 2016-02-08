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
package org.eclipse.incquery.runtime;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.incquery.runtime.internal.ExtensionBasedSurrogateQueryLoader;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IncQueryRuntimePlugin extends Plugin {

    public static final String PLUGIN_ID = "org.eclipse.incquery.runtime";

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ExtensionBasedSurrogateQueryLoader.instance().loadKnownSurrogateQueriesIntoRegistry();
	}

}
