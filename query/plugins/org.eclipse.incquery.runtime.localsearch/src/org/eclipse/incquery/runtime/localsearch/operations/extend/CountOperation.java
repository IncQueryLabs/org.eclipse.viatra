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
package org.eclipse.incquery.runtime.localsearch.operations.extend;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.incquery.runtime.localsearch.MatchingFrame;
import org.eclipse.incquery.runtime.localsearch.exceptions.LocalSearchException;
import org.eclipse.incquery.runtime.localsearch.matcher.ISearchContext;
import org.eclipse.incquery.runtime.localsearch.matcher.LocalSearchMatcher;
import org.eclipse.incquery.runtime.localsearch.matcher.MatcherReference;
import org.eclipse.incquery.runtime.localsearch.operations.IMatcherBasedOperation;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Calculates the count of matches for a called matcher
 * 
 * @author Zoltan Ujhelyi
 *
 */
public class CountOperation extends ExtendOperation<Integer> implements IMatcherBasedOperation{

    PQuery calledQuery;
    Map<Integer, Integer> frameMapping;
	private LocalSearchMatcher matcher;

	@Override
	public LocalSearchMatcher getAndPrepareCalledMatcher(MatchingFrame frame, ISearchContext context) {
		Set<Integer> adornment = Sets.newHashSet();
		for (Entry<Integer, Integer> mapping : frameMapping.entrySet()) {
			Integer source = mapping.getKey();
			if (frame.get(source) != null) {
				adornment.add(mapping.getValue());
			}
		}
		matcher = context.getMatcher(new MatcherReference(calledQuery, adornment));
        return matcher;
	}

	@Override
	public LocalSearchMatcher getCalledMatcher() {
		return matcher;
	}
	
    public CountOperation(PQuery calledQuery, Map<Integer, Integer> frameMapping, int position) {
        super(position);
        this.calledQuery = calledQuery;
        this.frameMapping = frameMapping;
    }

    @Override
    public void onInitialize(MatchingFrame frame, ISearchContext context) throws LocalSearchException {
        getAndPrepareCalledMatcher(frame, context);
        final MatchingFrame mappedFrame = matcher.editableMatchingFrame();
        for (Entry<Integer, Integer> entry : frameMapping.entrySet()) {
            mappedFrame.setValue(entry.getValue(), frame.getValue(entry.getKey()));
        }
        it = Iterators.singletonIterator(matcher.countMatches(mappedFrame));
        
    }

    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
		builder.append("CountOperation, pattern: ")
			.append(calledQuery.getFullyQualifiedName().substring(calledQuery.getFullyQualifiedName().lastIndexOf('.') + 1));
		return builder.toString();
    }
    
    @Override
	public List<Integer> getVariablePositions() {
    	List<Integer> variables = Lists.newArrayList();
    	variables.addAll(frameMapping.keySet());
		return variables;
	}


    
}
