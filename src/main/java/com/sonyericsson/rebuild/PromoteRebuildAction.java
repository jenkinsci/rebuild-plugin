/*
 *  The MIT License
 *
 *  Copyright 2013 Joel Johnson, Oleg Nenashev and contributors.
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

import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Run;

/**
 * A cause specifying that the build was a rebuild of another build. Extends
 * UpstreamCause so that a lot of the magic that Jenkins does with Upstream
 * builds is inherited (linking, etc).
 *
 * @author Joel Johnson
 * @author Oleg Nenashev
 */

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExportedBean(defaultVisibility = 3)
public class PromoteRebuildAction implements Action {

    private final PromoteRebuildCause upstreamCause;

    @Exported(visibility=2)
    public List<PromoteRebuildCause> getPromoteRebuild() {
        return Collections.unmodifiableList(Arrays.asList(upstreamCause));
    }

    /**
     * PromoteRebuildAction constructor.
     * @param  up Run.
     */
    public PromoteRebuildAction(Run<?, ?> up) {
        upstreamCause = new PromoteRebuildCause(up);
    }



    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "PromoteRebuildAction";
    }

    @Override
    public String getUrlName() {
        return "promoteRebuildAction";
    }

    @ExportedBean
    public static class PromoteRebuildCause {


        private final Cause.UpstreamCause upstreamCause;

        public PromoteRebuildCause(Run<?, ?> up) {
            upstreamCause = new Cause.UpstreamCause(up);
        }

        @Exported(
                visibility = 3
        )
        public String getReason() {
            return "RELEASE";
        }

        @Exported(
                visibility = 3
        )
        public String getUpstreamProject() {
            return upstreamCause.getUpstreamProject();
        }

        @Exported(
                visibility = 3
        )
        public int getUpstreamBuild() {
            return upstreamCause.getUpstreamBuild();
        }

        @Exported(
                visibility = 3
        )
        public String getUpstreamUrl() {
            return upstreamCause.getUpstreamUrl();
        }

    }
}
