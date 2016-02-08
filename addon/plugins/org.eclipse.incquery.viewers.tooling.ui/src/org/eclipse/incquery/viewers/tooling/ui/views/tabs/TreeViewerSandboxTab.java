package org.eclipse.incquery.viewers.tooling.ui.views.tabs;

import java.util.List;

import org.eclipse.incquery.viewers.runtime.IncQueryViewerSupport;
import org.eclipse.incquery.viewers.runtime.model.ViewerState;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class TreeViewerSandboxTab extends AbstractViewerSandboxTab {

    TreeViewer viewer;

    @Override
    public String getTabTitle() {
        return "Tree";
    }

    @Override
    public void bindState(ViewerState state) {
        IncQueryViewerSupport.bind(viewer, state);//, filter);
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();

    }

    @Override
    protected StructuredViewer getViewer() {
        return viewer;
    }

    @Override
    protected StructuredViewer createViewer(Composite parent) {
        viewer = new TreeViewer(parent);
        viewer.setUseHashlookup(true);
        return viewer;
    }

    @Override
    public List<IContributionItem> getDropDownMenuContributions() {
        return null;
    }

    @Override
    public List<IContributionItem> getToolBarContributions() {
        return null;
    }

}
