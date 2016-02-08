/*******************************************************************************
 * Copyright (c) 2010-2015, Csaba Debreceni, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Csaba Debreceni - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.viewers.runtime.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.util.IncQueryLoggingUtil;
import org.eclipse.incquery.viewers.runtime.model.Containment;
import org.eclipse.incquery.viewers.runtime.model.Edge;
import org.eclipse.incquery.viewers.runtime.model.Item;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2containmentMatch;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2containmentMatcher;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2edgeMatch;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2edgeMatcher;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2itemMatch;
import org.eclipse.incquery.viewers.runtime.model.patterns.Param2itemMatcher;

import com.google.common.collect.Lists;

public final class ViewerTraceabilityUtil {

    /**
     * Disable constructor
     */
    private ViewerTraceabilityUtil() {

    }

    public static Collection<Item> traceToItem(IncQueryEngine engine, Object source) {

        ArrayList<Item> list = Lists.newArrayList();
        Collection<Param2itemMatch> allMatches = executeParam2itemMatcher(engine, source);
        
        for (Param2itemMatch match : allMatches) {
            list.add(match.getItem());
        }
        return list;
    }

	private static Collection<Param2itemMatch> executeParam2itemMatcher(IncQueryEngine engine, Object source) {
		try {

            Param2itemMatcher matcher = Param2itemMatcher.on(engine);
            return matcher.getAllMatches(source, null, null);

        } catch (IncQueryException e) {
            Logger logger = IncQueryLoggingUtil.getLogger(ViewerTraceabilityUtil.class);
            logger.error(e.getMessage());
        }
		return Collections.emptySet(); 
	}

    public static Collection<Edge> traceToEdge(IncQueryEngine engine, Object source, Object target) {

        ArrayList<Edge> list = Lists.newArrayList();
        Collection<Param2edgeMatch> allMatches = executeParam2edgeMatcher(engine, source, target);
        
        for (Param2edgeMatch match : allMatches) {
            list.add(match.getEdge());
        }
        return list;
    }

	private static Collection<Param2edgeMatch> executeParam2edgeMatcher(IncQueryEngine engine, Object source, Object target) {
		try {

            Param2edgeMatcher matcher = Param2edgeMatcher.on(engine);
            return matcher.getAllMatches(source, target, null, null);

        } catch (IncQueryException e) {
            Logger logger = IncQueryLoggingUtil.getLogger(ViewerTraceabilityUtil.class);
            logger.error(e.getMessage());
        }
		return Collections.emptySet(); 
	}

    public static Collection<Containment> traceTocontainment(IncQueryEngine engine, Object source, Object target) {

        ArrayList<Containment> list = Lists.newArrayList();
        Collection<Param2containmentMatch> allMatches = executeParam2containmentMatcher(engine, source, target);
        
        for (Param2containmentMatch match : allMatches) {
            list.add(match.getContainment());
        }
        return list;
    }

	private static Collection<Param2containmentMatch> executeParam2containmentMatcher(IncQueryEngine engine, Object source, Object target) {
		try {

            Param2containmentMatcher matcher = Param2containmentMatcher.on(engine);
            return matcher.getAllMatches(source, target, null, null);

        } catch (IncQueryException e) {
            Logger logger = IncQueryLoggingUtil.getLogger(ViewerTraceabilityUtil.class);
            logger.error(e.getMessage());
        }
		return Collections.emptySet(); 
	}
	
	public static Collection<Item> deleteTracesAndItems(IncQueryEngine engine, Object source) {
    	ArrayList<Item> list = Lists.newArrayList();
        Collection<Param2itemMatch> allMatches = executeParam2itemMatcher(engine, source);
        
        for (Param2itemMatch match : allMatches) {
        	EcoreUtil.delete(match.getTrace());
        	EcoreUtil.delete(match.getItem());
            list.add(match.getItem());
        }
        
        return list;
    }
    
    public static Collection<Edge> deleteTracesAndEdges(IncQueryEngine engine, Object source, Object target) {
    	ArrayList<Edge> list = Lists.newArrayList();
        Collection<Param2edgeMatch> allMatches = executeParam2edgeMatcher(engine, source, target);
        
        for (Param2edgeMatch match : allMatches) {
        	EcoreUtil.delete(match.getTrace());
        	EcoreUtil.delete(match.getEdge());
            list.add(match.getEdge());
        }
        
        return list;
    }
    
    public static Collection<Containment> deleteTracesAndContainments(IncQueryEngine engine, Object source, Object target) {
    	ArrayList<Containment> list = Lists.newArrayList();
        Collection<Param2containmentMatch> allMatches = executeParam2containmentMatcher(engine, source, target);
        
        for (Param2containmentMatch match : allMatches) {
        	match.getContainment().getTarget().setParent(null);
        	EcoreUtil.delete(match.getTrace());
        	EcoreUtil.delete(match.getContainment());
            list.add(match.getContainment());
        }
        
        return list;
    }
}
