package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.plugins.git.Branch;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by beazlr02 on 04/05/16.
 */
public final class BuildChangeSet {

    private WorkflowRun workflowRun;

    public BuildChangeSet(WorkflowRun result) {
        this.workflowRun = result;
    }

    @Whitelisted
    public String getChangeSet() {
        StringBuilder changeSet = new StringBuilder();

        if (workflowRun != null) {
            for (ChangeLogSet<? extends ChangeLogSet.Entry> entries : workflowRun.getChangeSets()) {

                for (ChangeLogSet.Entry entry : entries) {

                    changeSet.append(entry.getAuthor())
                            .append(": ")
                            .append(entry.getMsg())
                            .append(" (")
                            .append(entry.getCommitId())
                            .append(")\n");
                }
            }
        }

        return changeSet.toString();
    }

    @Whitelisted
    public String getBranchName() {
        String result = "";
        try {
            BuildData action = workflowRun.getAction(BuildData.class);
            Revision lastBuiltRevision = action.getLastBuiltRevision();
            if (lastBuiltRevision != null) {
                Collection<Branch> branches = lastBuiltRevision.getBranches();
                Branch[] branchArray = branches.toArray(new Branch[]{});
                Branch branch = branchArray[0];
                result = branch.getName().replaceAll("refs/remotes/origin/", "");
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @Whitelisted
    public Collection<String> getBranchNames() {
        Collection<String> branchNames = new ArrayList<>(0);
        try {
            BuildData action = workflowRun.getAction(BuildData.class);
            if (action != null) {
                Revision lastBuiltRevision = action.getLastBuiltRevision();
                if (lastBuiltRevision != null) {
                    Collection<Branch> branches = lastBuiltRevision.getBranches();
                    for (Branch branch : branches) {
                        String name = branch.getName().replaceAll("refs/remotes/origin/", "");
                        branchNames.add(name);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return branchNames;
    }

    public String getBuildTriggerHash() {
        String result = "";
        try {
            BuildData action = workflowRun.getAction(BuildData.class);
            Revision lastBuiltRevision = action.getLastBuiltRevision();
            if (lastBuiltRevision != null && lastBuiltRevision.getSha1() != null) {
                result = lastBuiltRevision.getSha1().getName();
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}
