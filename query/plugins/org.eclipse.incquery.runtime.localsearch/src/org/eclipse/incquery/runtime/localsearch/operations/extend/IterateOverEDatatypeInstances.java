/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Akos Horvath, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi, Akos Horvath - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.localsearch.operations.extend;

import java.util.List;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.localsearch.MatchingFrame;
import org.eclipse.incquery.runtime.localsearch.matcher.ISearchContext;

import com.google.common.collect.Lists;

/**
 * Iterates over all {@link EDataType} instances using an {@link NavigationHelper EMF-IncQuery Base indexer}. It is
 * assumed that the indexer is initialized for the selected {@link EDataType}.
 * 
 */
public class IterateOverEDatatypeInstances extends ExtendOperation<Object> {

    private EDataType dataType;

    /**
     * @param position
     * @param dataType
     */
    public IterateOverEDatatypeInstances(int position, EDataType dataType) {
        super(position);
        this.dataType = dataType;
    }

    public EDataType getDataType() {
        return dataType;
    }

    @Override
    public void onInitialize(MatchingFrame frame, ISearchContext context) {
        it = context.getBaseIndex().getDataTypeInstances(dataType).iterator();
    }
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String name = dataType.getName();

        builder.append("extend ")
            .append(name);

        return builder.toString();
    }
    
    @Override
	public List<Integer> getVariablePositions() {
		return Lists.asList(position, new Integer[0]);
	}
    

}
