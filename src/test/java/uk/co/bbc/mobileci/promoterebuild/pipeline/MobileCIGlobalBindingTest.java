package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class MobileCIGlobalBindingTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void promotedJobGlobalCreated() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  mobileCiSupport\n" +
                        "}", true));
        WorkflowRun run = j.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }

    @Test
    public void promotedJobGlobalIsNotForPromotion() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  if( ! mobileCiSupport.isPromotion()) {" +
                        "       manager.addWarningBadge 'stuff is broken'\n" +
                        "}\n" +
                        "}", true));
        QueueTaskFuture<WorkflowRun> workflowRunQueueTaskFuture = p.scheduleBuild2(0);
        WorkflowRun r = workflowRunQueueTaskFuture.waitForStart();
        while (r.isBuilding()) {
            Thread.sleep(100);
        }

        assertEquals("stuff is broken", r.getAction(GroovyPostbuildAction.class).getText());
    }

    @Test
    public void promotedJobGlobalIsForPromotionAfterPromotingBuild() throws Exception {

        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        String script =
                "node " +
                        "{\n" +
                        "  if( mobileCiSupport.promotion ) {" +
                        "    manager.addWarningBadge 'is a promotion'\n" +
                        "    \n" +
                        "  } else {\n" +
                        "    manager.addWarningBadge 'not promotion'\n" +
                        "    \n" +
                        "  }\n" +
                        "}";


        p.setDefinition(new CpsFlowDefinition(
                script,
                true));
        QueueTaskFuture<WorkflowRun> workflowRunQueueTaskFuture = p.scheduleBuild2(0);
        WorkflowRun r = workflowRunQueueTaskFuture.waitForStart();
        j.waitForCompletion(r);

        assertEquals("not promotion", r.getAction(GroovyPostbuildAction.class).getText());

        //PROMOTE
        WorkflowRun build = p.getLastBuild();
        HtmlPage page = j.createWebClient().getPage(build, "promoterebuild");

        //CHECK IT RAN
        int number = p.getLastCompletedBuild().getNumber();
        assertEquals("Build number should have incremented", 2, number);

        r = p.getLastCompletedBuild();

        j.waitForCompletion(r);

        assertEquals("is a promotion", r.getAction(GroovyPostbuildAction.class).getText());

    }

}
