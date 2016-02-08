/*******************************************************************************
 * Copyright (c) 2010-2012, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.testing.ui.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.api.scope.IncQueryScope;
import org.eclipse.incquery.runtime.emf.EMFScope;
import org.eclipse.incquery.snapshot.EIQSnapshot.EIQSnapshotFactory;
import org.eclipse.incquery.snapshot.EIQSnapshot.IncQuerySnapshot;
import org.eclipse.incquery.testing.core.ModelLoadHelper;
import org.eclipse.incquery.testing.core.SnapshotHelper;
import org.eclipse.incquery.tooling.ui.queryexplorer.content.matcher.PatternMatcherContent;
import org.eclipse.incquery.tooling.ui.queryexplorer.content.matcher.PatternMatcherRootContent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.inject.Inject;

/**
 * @author Abel Hegedus
 *
 */
public class SaveSnapshotHandler extends AbstractHandler {

	@Inject
	SnapshotHelper helper;
	@Inject
	ModelLoadHelper loader;
	@Inject
	private Logger logger;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection instanceof TreeSelection) {
			saveSnapshot((TreeSelection) selection, event);
		}
		return null;
	}

	
	private void saveSnapshot(TreeSelection selection, ExecutionEvent event) {
		Object obj = selection.getFirstElement();
		
		IEditorPart editor = null;
		List<PatternMatcherContent> matchers = new ArrayList<PatternMatcherContent>();
		IncQueryEngine engine = null;
		if(obj instanceof PatternMatcherContent) {
		    PatternMatcherContent observablePatternMatcher = (PatternMatcherContent) obj;
			editor = observablePatternMatcher.getParent().getEditorPart();
			matchers.add(observablePatternMatcher);
			IncQueryMatcher<?> matcher = observablePatternMatcher.getMatcher();
			if(matcher != null) {
			    engine = matcher.getEngine();
			}
			
		} else if(obj instanceof PatternMatcherRootContent) {
		    PatternMatcherRootContent matcherRoot = (PatternMatcherRootContent) obj;
			editor = matcherRoot.getEditorPart();
			Iterator<PatternMatcherContent> iterator = matcherRoot.getChildrenIterator();
			
			while (iterator.hasNext()) {
			    PatternMatcherContent patternMatcherContent = iterator.next();
			    matchers.add(patternMatcherContent);
			    
                IncQueryMatcher<?> matcher = patternMatcherContent.getMatcher();
                if(matcher != null && matcher.getEngine() != null) {
                    engine = matcher.getEngine();
                }
			}
		}
		if(engine == null) {
			logger.error("Cannot save snapshot without IncQueryEngine!");
			return;
		}
		ResourceSet resourceSet = getResourceSetForScope(engine.getScope());
		if(resourceSet == null) {
			logger.error("Cannot save snapshot, models not in ResourceSet!");
			return;
		}
		IFile snapshotFile = null;
		IFile[] files = WorkspaceResourceDialog.openFileSelection(HandlerUtil.getActiveShell(event), "Existing snapshot", "Select existing EMF-IncQuery snapshot file (Cancel for new file)", false, null, null);
		IncQuerySnapshot snapshot = null;
			
		if(files.length == 0) {
			snapshotFile = WorkspaceResourceDialog.openNewFile(HandlerUtil.getActiveShell(event), "New snapshot", "Select EMF-IncQuery snapshot target file (.eiqsnapshot extension)", null, null);
			if(snapshotFile != null && !snapshotFile.exists()) {
				snapshot = EIQSnapshotFactory.eINSTANCE.createIncQuerySnapshot();
				Resource res = resourceSet.createResource(URI.createPlatformResourceURI(snapshotFile.getFullPath().toString(),true));
				res.getContents().add(snapshot);
			} else {
				logger.error("Selected file name must use .eiqsnapshot extension!");
				return;
			}
		} else {
			snapshotFile = files[0];
			if(snapshotFile != null && snapshotFile.getFileExtension().equals("eiqsnapshot")) {
			
				snapshot = loader.loadExpectedResultsFromFile(resourceSet,snapshotFile);
				
				if(snapshot != null) {
					if(!validateInputSpecification(engine, snapshot)) {
						return;
					}
				} else {
					logger.error("Selected file does not contain snapshot!");
					return;
				}
			} else {
				logger.error("Selected file not .eiqsnapshot!");
				return;
			}
		} 
		for (PatternMatcherContent matcher : matchers) {
			if(matcher.getMatcher() != null) {
				IPatternMatch filter = matcher.getMatcher().newMatch(matcher.getFilter());
			    helper.saveMatchesToSnapshot(matcher.getMatcher(), filter, snapshot);
			}
		}
		
		if(editor != null) {
			editor.doSave(new NullProgressMonitor());
		} else {
			try {
				snapshot.eResource().save(null);
			} catch(IOException e) {
				logger.error("Error during saving snapshot into file!",e);
			}
		}
	}


	/**
	 * @param engine
	 * @param snapshot
	 */
	private boolean validateInputSpecification(IncQueryEngine engine, IncQuerySnapshot snapshot) {
		if(snapshot.getInputSpecification() != null) {
			Notifier root = helper.getEMFRootForSnapshot(snapshot);
            Notifier matcherRoot = getScopeRoot(engine.getScope());
			if(matcherRoot != root) {
				logger.error("Existing snapshot model root (" + root + ") not equal to selected input (" + matcherRoot + ")!");
				return false;
			}
			return true;
			/*switch(snapshot.getInputSpecification()) {
				case EOBJECT:
					if(matcherRoot instanceof EObject && root instanceof EObject) {
						if(matcherRoot != root) {
							engine.getLogger().logError("Existing snapshot model root (" + root + ") not equal to selected input (" + matcherRoot + ")!");
						}
					}
					break;
				case RESOURCE:
					if(matcherRoot instanceof Resource && root instanceof Resource) {
						Resource res = (Resource) matcherRoot;
						for (EObject eobj : res.getContents()) {
							if(!snapshot.getModelRoots().contains(eobj)) {
								engine.getLogger().logError("Existing snapshot model root not equal to selected input! Missing model root: " + eobj);
							}
						}
						for(EObject eobj : snapshot.getModelRoots()) {
							if(!res.getContents().contains(eobj)) {
								engine.getLogger().logError("Existing snapshot model root not equal to selected input! Missing snapshot root: " + eobj);
							}
						}
					}
					break;
				case RESOURCE_SET:
					if(matcherRoot instanceof ResourceSet && root instanceof ResourceSet) {
						ResourceSet set = (ResourceSet) matcherRoot;
						for (Resource res : set.getResources()) {
							for (EObject eobj : res.getContents()) {
								if(!snapshot.getModelRoots().contains(eobj)) {
									engine.getLogger().logError("Existing snapshot model root not equal to selected input!");
								}
							}
						}
					}
					break;
			}*/
		}
		return true;
	}
	
	private ResourceSet getResourceSetForScope(IncQueryScope scope) {
	    if (scope instanceof EMFScope) {
            EMFScope emfScope = (EMFScope) scope;
            Set<? extends Notifier> scopeRoots = emfScope.getScopeRoots();
            if (scopeRoots.size() > 1) {
                throw new IllegalArgumentException("EMF scopes with multiple ResourceSets are not supported!");
            } else {
                Notifier notifier = scopeRoots.iterator().next();
                if(notifier instanceof EObject) {
                    Resource resource = ((EObject) notifier).eResource();
                    if(resource != null) {
                        return resource.getResourceSet();
                    } else {
                        return null;
                    }
                } else if(notifier instanceof Resource) {
                    return ((Resource) notifier).getResourceSet();
                } else if(notifier instanceof ResourceSet) {
                    return (ResourceSet) notifier;
                } else {
                    return null;
                }
            }
        } else {
            throw new IllegalArgumentException("Non-EMF scopes are not supported!");
        }
	}
	
	private Notifier getScopeRoot(IncQueryScope scope) {
        if (scope instanceof EMFScope) {
            EMFScope emfScope = (EMFScope) scope;
            Set<? extends Notifier> scopeRoots = emfScope.getScopeRoots();
            if (scopeRoots.size() > 1) {
                throw new IllegalArgumentException("EMF scopes with multiple ResourceSets are not supported!");
            } else {
                return scopeRoots.iterator().next();
            }
        } else {
            throw new IllegalArgumentException("Non-EMF scopes are not supported!");
        }
    }
}
