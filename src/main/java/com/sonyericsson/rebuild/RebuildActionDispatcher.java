package com.sonyericsson.rebuild;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Run;

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
     */
    public static ExtensionList<RebuildActionDispatcher> all() {
        return ExtensionList.lookup(RebuildActionDispatcher.class);
    }
}
