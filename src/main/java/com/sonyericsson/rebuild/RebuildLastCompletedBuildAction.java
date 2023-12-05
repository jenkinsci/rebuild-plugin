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

import hudson.model.AbstractProject;
import hudson.model.Run;

/**
 * Reschedules last completed build for the project if available.
 * Otherwise it behaves as if the user clicked on the build now button.
 */
public class RebuildLastCompletedBuildAction extends AbstractRebuildAction {

    private final AbstractProject<?, ?> project;

    public RebuildLastCompletedBuildAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    @Override
    public AbstractProject<?, ?> getProject() {
        return project;
    }

    @Override
    protected Run<?, ?> getRun() {
        return null;
    }

    @Override
    public String getUrlName() {
        boolean isBuildable = project.isBuildable();
        if (isBuildable) {
            final Run<?, ?> lastCompletedBuild = project.getLastCompletedBuild();
            if (lastCompletedBuild != null) {
                final RebuildAction action = lastCompletedBuild.getAction(RebuildAction.class);
                if (action == null) {
                    return null;
                }
                // TODO This will have unexpected results if the job configuration changed between link rendering
                //  and when the user clicks. Seems preferable to rebuilding a "wrong" build (finished since link was
                //  rendered though).
                return "lastCompletedBuild/" + action.getTaskUrl();
            } else {
                // This used to link to "Build Now" but that doesn't work for parameterized builds
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getIconFileName() {
        return super.getIconFileName();
    }

    @Override
    public String getTaskUrl() {
        return getUrlName();
    }

    @Override
    public boolean isRequiresPOST() {
        final Run<?, ?> lastCompletedBuild = project.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return false;
        }
        final RebuildAction action = lastCompletedBuild.getAction(RebuildAction.class);
        // TODO This will have unexpected results if the job configuration changed between link rendering
        //  and when the user clicks. Seems preferable to rebuilding a "wrong" build (finished since link was
        //  rendered though).
        return action.isRequiresPOST();
    }

    @Override
    public String getDisplayName() {
        return "Rebuild Last";
    }
}
