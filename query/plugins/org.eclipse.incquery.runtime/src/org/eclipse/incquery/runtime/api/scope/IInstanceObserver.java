/*******************************************************************************
 * Copyright (c) 2010-2014, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.api.scope;


/**
 * Listener interface for lightweight observation of changes in edges leaving from given source instance elements.
 * @author Bergmann Gabor
 * @since 0.9
 *
 */
public interface IInstanceObserver {
    void notifyBinaryChanged(Object sourceElement, Object edgeType);
    void notifyTernaryChanged(Object sourceElement, Object edgeType);
}
