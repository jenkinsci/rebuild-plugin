package uk.co.bbc.mobileci.promoterebuild.pipeline;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildAction;
import org.jvnet.hudson.test.JenkinsRule;
import jenkins.triggers.SCMTriggerItem;

import static org.junit.Assert.assertEquals;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class StagePromotionDSLTest  {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void createsDefaultBadge() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  stagePromotion {}\n" +
                        "}", false));
        WorkflowRun run = j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        assertEquals("Promote to RELEASE", run.getAction(GroovyPostbuildAction.class).getText());

        String expectedPromoteUrl = "/" + p.getBuildByNumber(1).getUrl() + "promoterebuild";
        assertEquals(expectedPromoteUrl, run.getAction(GroovyPostbuildAction.class).getLink());

    }

    @Test
    public void createsDefaultBadgeWithMinimalCode() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  stagePromotion()\n" +
                        "}", false));
        WorkflowRun run = j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        assertEquals("Promote to RELEASE", run.getAction(GroovyPostbuildAction.class).getText());

        String expectedPromoteUrl = "/" + p.getBuildByNumber(1).getUrl() + "promoterebuild";
        assertEquals(expectedPromoteUrl, run.getAction(GroovyPostbuildAction.class).getLink());

    }

    @Test
    public void createsBadgeWithCustomMessage() throws Exception {
        WorkflowJob p = j.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                        "  stagePromotion {\n" +
                        "    message 'testing a release'\n" +
                        "  }\n" +
                        "}", false));
        WorkflowRun run = j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        assertEquals("testing a release", run.getAction(GroovyPostbuildAction.class).getText());
    }
}
