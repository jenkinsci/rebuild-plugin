package com.sonyericsson.rebuild;

/**
 * Reschedules last completed build for the project if available.
 * Otherwise it behaves as if the user click on the build now button.
 */
public class RebuildLastCompletedBuildAction extends RebuildAction {

    @Override
    public String getUrlName() {
        if (getProject() != null && getProject().getLastCompletedBuild() != null && getProject().isBuildable()) {
            return getProject().getLastCompletedBuild().getNumber() + "/rebuild";
        } else {
            return "build?delay=0sec";
        }
    }

    @Override
    public String getDisplayName() {
        return "Rebuild Last";
    }
}
