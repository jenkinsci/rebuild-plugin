/*
 *  The MIT License
 *
 *  Copyright 2012 Rino Kadijk.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.rebuild;

import hudson.matrix.MatrixConfiguration;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.Collection;
import jenkins.model.TransientActionFactory;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * Enables rebuild for builds that ran before installing the rebuild plugin.
 */
@Extension
public class RebuildActionFactory extends TransientActionFactory<Run> {

    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @Override
    public Collection<? extends Action> createFor(Run build) {
        // TODO should this not just use RebuildAction.isRebuildAvailable? Or conversely, is that method needed if we
        // are already filtering here?
        if (build.getParent() instanceof MatrixConfiguration) {
            return emptyList();
        }
        if (!(build.getParent() instanceof Queue.Task)) {
            return emptyList();
        }


        // These actions used to be permanently attached to the Run
        // Build#getActions is deprecated but the only way to prevent a stack overflow here
        boolean hasRebuildAction = build.getActions().stream().anyMatch(it -> it instanceof RebuildAction);
        if (hasRebuildAction) {
            return emptyList();
        }
        for (RebuildValidator rebuildValidator : Jenkins.get().
                getExtensionList(RebuildValidator.class)) {
            if (rebuildValidator.isApplicable(build)) {
                return emptyList();
            }
        }
        return singleton(new RebuildAction(build));
    }
}
