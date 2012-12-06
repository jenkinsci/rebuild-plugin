package com.sonyericsson.rebuild;

import static java.util.Collections.singleton;

import java.util.Collection;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

/**
 * Makes the rebuild button available on the project level.
 * Rebuilds the last completed build.
 */
@Extension
public class RebuildProjectActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject abstractProject) {
        return singleton(new RebuildLastCompletedBuildAction());
    }
}
