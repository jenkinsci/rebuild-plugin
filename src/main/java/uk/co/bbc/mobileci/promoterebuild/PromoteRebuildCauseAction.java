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
package uk.co.bbc.mobileci.promoterebuild;

import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.util.BuildData;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.*;

/**
 * A cause specifying that the build was a rebuild of another build. Extends
 * UpstreamCause so that a lot of the magic that Jenkins does with Upstream
 * builds is inherited (linking, etc).
 *
 * @author Joel Johnson
 * @author Oleg Nenashev
 */

@ExportedBean(defaultVisibility = 3)
public class PromoteRebuildCauseAction implements Action {

    private final PromoteRebuildCause upstreamCause;

    @Exported(visibility=2)
    public PromoteRebuildCause getPromoteRebuildCause() {
        return (upstreamCause);
    }

    /**
     * PromoteRebuildAction constructor.
     * @param  up Run.
     */
    public PromoteRebuildCauseAction(Run<?, ?> up) {
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
        private String buildHash;
        private String buildRemote;

        public PromoteRebuildCause(Run<?, ?> up) {
            upstreamCause = new Cause.UpstreamCause(up);
            try {
                GitSCM jobBaseSCM = getScm(up);
                List<BuildData> actions = up.getActions(BuildData.class);
                Map<String, String> commitHashes = new HashMap<>();
                for (BuildData action : actions) {
                    commitHashes.put(action.getRemoteUrls().iterator().next(), action.getLastBuiltRevision().getSha1().getName());
                }
                buildRemote = getBaseRemote(jobBaseSCM);
                buildHash = commitHashes.get(buildRemote);
            } catch(ClassCastException ignored) {
            }
        }

        private GitSCM getScm(Run<?, ?> up) throws ClassCastException {
            return (GitSCM) ((CpsScmFlowDefinition) ((WorkflowJob) up.getParent()).getDefinition()).getScm();
        }

        private String getBaseRemote(GitSCM jobBaseSCM) {
            return jobBaseSCM.getUserRemoteConfigs().get(0).getUrl();
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

        public String getBuildHash() {
            return this.buildHash;
        }

        public String getBuildRemote() {
            return this.buildRemote;
        }
    }
}
