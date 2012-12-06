package com.sonyericsson.rebuild;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

import java.util.Collection;

import hudson.Extension;
import hudson.model.*;

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
