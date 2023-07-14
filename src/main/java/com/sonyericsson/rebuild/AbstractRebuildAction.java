package com.sonyericsson.rebuild;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.matrix.MatrixRun;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Run;

public abstract class AbstractRebuildAction implements Action {

    @Override
    public String getIconFileName() {
        if (isRebuildAvailable()) {
            return "clock.png";
        } else {
            return null;
        }
    }

    /**
     * Method for checking whether the rebuild functionality would be available
     * for build.
     *
     * @return boolean
     */
    public boolean isRebuildAvailable() {
        Job project = getProject();
        return project != null
                && project.hasPermission(Item.BUILD)
                && project.isBuildable()
                && project instanceof Queue.Task
                && !isMatrixRun()
                && !isRebuildDisabled();

    }

    // Jelly
    public abstract String getTaskUrl();

    // Jelly
    public abstract boolean isRequiresPOST();

    private boolean isRebuildDisabled() {
        RebuildSettings settings = getProject().getProperty(RebuildSettings.class);
        return settings != null && settings.getRebuildDisabled();
    }

    /**
     * Method will return current project.
     *
     * @return currentProject.
     */
    public abstract Job<?, ?> getProject();

    @CheckForNull
    protected abstract Run<?, ?> getRun();

    /**
     * Method for checking whether current build is sub job(MatrixRun) of Matrix
     * build.
     *
     * @return boolean
     */
    public boolean isMatrixRun() {
        return getRun() instanceof MatrixRun;
    }
}
