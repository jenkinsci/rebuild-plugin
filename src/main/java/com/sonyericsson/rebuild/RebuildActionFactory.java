package com.sonyericsson.rebuild;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.TransientBuildActionFactory;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * Enables rebuild for builds that ran before installing the rebuild plugin.
 */
@Extension
public class RebuildActionFactory extends TransientBuildActionFactory {

    @Override
    public Collection<? extends Action> createFor(Run target) {
        AbstractBuild build = (AbstractBuild) target;
        boolean hasRebuildAction = target.getAction(RebuildAction.class) != null;
        if (hasRebuildAction) {
            return emptyList();
        }
        for (RebuildValidator rebuildValidator : Hudson.getInstance().
                getExtensionList(RebuildValidator.class)) {
            if (rebuildValidator.isApplicable(build)) {
                return emptyList();
            }
        }
        return singleton(new RebuildAction());
    }
}
