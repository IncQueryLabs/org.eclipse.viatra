/*******************************************************************************
 * Copyright (c) 2010-2014, Tamas Szabo (itemis AG), Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo (itemis AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.tooling.ui.queryexplorer.content.matcher;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.incquery.tooling.ui.queryexplorer.QueryExplorer;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

/**
 * A {@link TreeStructureAdvisor} implementation for the {@link QueryExplorer}.
 * 
 * @author Tamas Szabo (itemis AG)
 * 
 */
public class QueryExplorerTreeStructureAdvisor extends TreeStructureAdvisor {

    @Override
    public Object getParent(Object element) {
        return ((BaseContent<?>) element).getParent();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Boolean hasChildren(Object element) {
        if (element instanceof PatternMatcherRootContent && 
                (((PatternMatcherRootContent) element).isTainted()
                || ((PatternMatcherRootContent) element).getStatus().getSeverity() == IStatus.ERROR)) {
            return false;
        }
        if (element instanceof CompositeContent<?, ?> && ((CompositeContent) element).getChildren() != null) {
            return !((CompositeContent) element).getChildren().isEmpty();
        }
        return false;
    }

}
