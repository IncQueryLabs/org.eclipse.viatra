/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.viewers.runtime.model.listeners;

import org.eclipse.incquery.viewers.runtime.model.Edge;
import org.eclipse.incquery.viewers.runtime.model.Item;

/**
 * Listener interface for label changes.
 * 
 * @author Zoltan Ujhelyi
 *
 */
public interface IViewerLabelListener {

	void labelUpdated(Item item, String newLabel);
	void labelUpdated(Edge edge, String newLabel);
}
