package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.console.AnnotatedLargeText;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class BuildChangesGlobalTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void promotedJobGlobalCreated() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  echo pipelineBuildChangeset.getChangeSet()\n" +
                        "}", true));

        j.assertBuildStatusSuccess(p.scheduleBuild2(0));
        AnnotatedLargeText logText = p.getLastCompletedBuild().getLogText();

        logText.writeRawLogTo(0, System.err);
    }


}
