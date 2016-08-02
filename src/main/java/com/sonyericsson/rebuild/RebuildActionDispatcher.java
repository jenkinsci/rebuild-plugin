package com.sonyericsson.rebuild;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.Collection;

/**
 * Extension point to propagate additional actions to the rebuild.
 */
public abstract class RebuildActionDispatcher implements ExtensionPoint {

    /**
     * @return the actions to include in the rebuild.
     */
    public abstract Collection<Action> getPropagatingActions(Run r);

    /**
     * All registered {@link RebuildActionDispatcher}s.
     * TODO: use ExtensionList.lookup() once the Jenkins dependency is upgraded >= 1.572
     */
    public static ExtensionList<RebuildActionDispatcher> all() {
        Jenkins j = Jenkins.getInstance();
        return j == null ? ExtensionList.create((Jenkins) null, RebuildActionDispatcher.class) : j.getExtensionList(RebuildActionDispatcher.class);
    }
}
