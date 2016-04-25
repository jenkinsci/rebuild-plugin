package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.plugins.git.util.BuildData;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class PromotedJobWithGITGlobalTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();



    @Test
    public void twoCommitsOneHash() throws Exception {

        sampleRepo.init();
        String script =
                "node " +
                        "{\n" +
                        "  if( promotedJob.promotion ) {" +
                        "    echo 'PROMOTED:' + promotedJob.getHash()\n" +
                        "    echo 'BUILDNUMBER:' + promotedJob.getFromBuildNumber()\n" +
                        "    \n" +
                        "  } else {\n" +
                        "    echo 'not a promotion'\n" +
                        "    \n" +
                        "  }\n" +
                        "}";

        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");

        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));

        story.waitForCompletion(p.scheduleBuild2(0).get());

        sampleRepo.write("Jenkinsfile2", script);
        sampleRepo.git("add", "Jenkinsfile2");
        sampleRepo.git("commit", "--message=commitForTest");

        sampleRepo.write("Jenkinsfile3", script);
        sampleRepo.git("add", "Jenkinsfile3");
        sampleRepo.git("commit", "--message=anotherCommitForTest");

        WorkflowRun b = p.scheduleBuild2(0).get();

        story.waitForCompletion(b);

        String masterHeadHash = b.getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();

        // do a couple more builds
        b = p.scheduleBuild2(0).get();
        story.waitForCompletion(b);
        b = p.scheduleBuild2(0).get();
        story.waitForCompletion(b);


        //PROMOTE build 2
        b = p.getBuildByNumber(2);
        story.createWebClient().getPage(b, "promoterebuild");
        b = p.getLastBuild();
        story.waitForCompletion(b);

        story.assertBuildStatusSuccess(b);

        story.assertLogContains("PROMOTED:"+masterHeadHash, b);
        story.assertLogContains("BUILDNUMBER:2", b);

    }

    @Test
    public void promotingCommitNotFromHead() throws Exception {

        sampleRepo.init();
        String script =
                "node " +
                        "{\n" +
                        "  if( promotedJob.promotion ) {" +
                        "    echo 'PROMOTED:' + promotedJob.getHash()\n" +
                        "    echo 'BUILDNUMBER:' + promotedJob.getFromBuildNumber()\n" +
                        "    \n" +
                        "  } else {\n" +
                        "    echo 'not a promotion'\n" +
                        "    \n" +
                        "  }\n" +
                        "}";

        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");

        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));

        story.waitForCompletion(p.scheduleBuild2(0).get());

        sampleRepo.write("Jenkinsfile2", script);
        sampleRepo.git("add", "Jenkinsfile2");
        sampleRepo.git("commit", "--message=commitForTest");

        sampleRepo.write("Jenkinsfile3", script);
        sampleRepo.git("add", "Jenkinsfile3");
        sampleRepo.git("commit", "--message=anotherCommitForTest");

        WorkflowRun b = p.scheduleBuild2(0).get();

        story.waitForCompletion(b);

        String masterHeadHash = b.getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();

        // do a couple more builds with some commits
        b = p.scheduleBuild2(0).get();
        story.waitForCompletion(b);


        sampleRepo.write("Jenkinsfile4", script);
        sampleRepo.git("add", "Jenkinsfile4");
        sampleRepo.git("commit", "--message=commitForTest4");

        sampleRepo.write("Jenkinsfile5", script);
        sampleRepo.git("add", "Jenkinsfile5");
        sampleRepo.git("commit", "--message=anotherCommitForTest5");

        b = p.scheduleBuild2(0).get();
        story.waitForCompletion(b);


        //PROMOTE build 2
        b = p.getBuildByNumber(2);
        story.createWebClient().getPage(b, "promoterebuild");
        b = p.getLastBuild();
        story.waitForCompletion(b);

        story.assertBuildStatusSuccess(b);

        story.assertLogContains("PROMOTED:"+masterHeadHash, b);
        story.assertLogContains("BUILDNUMBER:2", b);

    }
}
