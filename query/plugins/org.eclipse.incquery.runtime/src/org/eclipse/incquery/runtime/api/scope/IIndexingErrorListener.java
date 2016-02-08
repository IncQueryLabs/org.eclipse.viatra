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
package org.eclipse.incquery.runtime.api.scope;

import org.eclipse.incquery.runtime.base.api.NavigationHelper;

/**
 * 
 * This interface contains callbacks for various internal errors from the {@link NavigationHelper base index}.
 * 
 * @author Zoltan Ujhelyi
 * @since 0.9
 *
 */
public interface IIndexingErrorListener {

    void error(String description, Throwable t);
    void fatal(String description, Throwable t);
}
