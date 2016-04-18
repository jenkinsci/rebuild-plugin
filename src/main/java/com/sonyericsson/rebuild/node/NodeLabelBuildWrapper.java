package com.sonyericsson.rebuild.node;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class NodeLabelBuildWrapper extends BuildWrapper {

    @Override
    public BuildWrapper.Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        ParametersAction originalParametersAction = build.getAction(ParametersAction.class);
        if (originalParametersAction == null) {
            return new BuildWrapper.Environment() {};
        } else {
            return new TriggerNextBuildEnvironment();
        }
    }

    private class TriggerNextBuildEnvironment extends BuildWrapper.Environment {

        @Override
        public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
            triggerBuilds(build, listener);
            return true;
        }

        private void triggerBuilds(AbstractBuild<?, ?> build, BuildListener listener) {
            final ParametersAction originalParametersAction = build.getAction(ParametersAction.class);
            final List<ParameterValue> originalParams = originalParametersAction.getParameters();
            final List<ParameterValue> newParams = new ArrayList<ParameterValue>();

            boolean triggerNewBuild = false;
            for (ParameterValue parameterValue : originalParams) {
                if (parameterValue instanceof NodeLabelParameterValue) {
                    NodeLabelParameterValue originalNodeLabelParameter = (NodeLabelParameterValue) parameterValue;
                    String label = originalNodeLabelParameter.getLabel();
                    NodeLabelParameterValue newNodeLabelParameter = new NodeLabelParameterValue(label);
                    newParams.add(newNodeLabelParameter);
                    listener.getLogger().print("scheduling single build on node " + label);
                    triggerNewBuild = true;
                } else {
                    newParams.add(parameterValue);
                }
            }
            if (triggerNewBuild) {
                build.getProject().scheduleBuild(0, null, new ParametersAction(newParams));
            }
        }
    }
}
