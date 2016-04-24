package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.console.AnnotatedLargeText;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class BuildChangesGlobalTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void promotedJobGlobalCreated() throws Exception {

                WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  echo pipelineBuildChangeset.getChangeSet()\n" +
                                "}", true));

                story.assertBuildStatusSuccess(p.scheduleBuild2(0));


    }



    @Test public void smokeTests() throws Exception {

        sampleRepo.init();
        String script = "node {\n" +
                "  echo pipelineBuildChangeset.getChangeSet()\n" +
                "  echo 'A_MSG_IN_LOG'\n" +
                "}";
        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");

        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        story.assertLogContains("A_MSG_IN_LOG",
                story.assertBuildStatusSuccess(story.waitForCompletion(b)));

    }
}
