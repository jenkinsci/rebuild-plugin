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
import hudson.model.Hudson;
import hudson.model.Queue;
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
    public Collection<? extends Action> createFor(Run build) {
        // TODO should this not just use RebuildAction.isRebuildAvailable? Or conversely, is that method needed if we are already filtering here?
        if (build.getParent() instanceof MatrixConfiguration) {
            return emptyList();
        }
        if (!(build.getParent() instanceof Queue.Task)) {
            return emptyList();
        }

        boolean hasRebuildAction = build.getAction(RebuildAction.class) != null;
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
