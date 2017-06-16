package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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
                                "  echo mobileCiSupport.getChangeSet()\n" +
                                "}", true));

                story.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }

    @Test
    public void twoCommitsInChangeList() throws Exception {
        WorkflowRun b = setupJenkins();

        story.assertLogContains("commitForTest", b);
        story.assertLogContains("anotherCommitForTest", b);

        String masterHeadHash = b.getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();
        story.assertLogContains(masterHeadHash, b);
    }

    @Test
    public void changesetShouldHaveTheCorrectFormat() throws Exception {
        WorkflowRun wfr = setupJenkins();

        BuildChangeSet buildChangeSet = new BuildChangeSet(wfr);
        String changeSet = buildChangeSet.getChangeSet();

        for (ChangeLogSet.Entry entry : wfr.getChangeSets().get(0)) {
            String change = String.format("%s: %s (%s)", entry.getAuthor(), entry.getMsg(), entry.getCommitId());
            assertThat(changeSet, containsString(change));
        }
    }

    private WorkflowRun setupJenkins() throws Exception {
        sampleRepo.init();
        String script;
        script = "node {\n" +
                "  echo mobileCiSupport.getChangeSet()\n" +
                "}";
        sampleRepo.write("Jenkinsfile", script);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");

        WorkflowJob wfj = story.jenkins.createProject(WorkflowJob.class, "p");
        wfj.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));

        story.waitForCompletion(wfj.scheduleBuild2(0).get());

        sampleRepo.write("Jenkinsfile2", script);
        sampleRepo.git("add", "Jenkinsfile2");
        sampleRepo.git("commit", "--message=commitForTest");

        sampleRepo.write("Jenkinsfile3", script);
        sampleRepo.git("add", "Jenkinsfile3");
        sampleRepo.git("commit", "--message=anotherCommitForTest");

        WorkflowRun wfr = wfj.scheduleBuild2(0).get();

        story.waitForCompletion(wfr);

        return wfr;
    }
}
