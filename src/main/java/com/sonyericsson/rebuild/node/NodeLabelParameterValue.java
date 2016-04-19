package com.sonyericsson.rebuild.node;

import hudson.model.AbstractBuild;
import hudson.model.Label;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;
import org.kohsuke.stapler.export.Exported;

import java.util.List;

public class NodeLabelParameterValue extends ParameterValue {
    private static final String NODE_LABEL_PARAMETER_NAME = "com.sonyericsson.rebuild.node.NODE_LABEL";

    @Exported(visibility = 3)
    private final String label;

    public NodeLabelParameterValue(String nodeLabel) {
        super(NODE_LABEL_PARAMETER_NAME);
        if (nodeLabel == null || nodeLabel.trim().isEmpty()) {
            label = "master";
        } else {
            label = nodeLabel;
        }
    }

    @Override
    public Label getAssignedLabel(SubTask task) {
        return Label.get(label);
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return NodeLabelParameterValue.this.name.equals(name) ? label : null;
            }
        };
    }

    @Exported(name = "value")
    public String getLabel() {
        return label;
    }

    @Override
    public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
        ParametersDefinitionProperty property =
                build.getProject().getProperty(hudson.model.ParametersDefinitionProperty.class);
        if (property != null) {
            final List<ParameterDefinition> parameterDefinitions = property.getParameterDefinitions();
            for (ParameterDefinition paramDef : parameterDefinitions) {
                if (NodeLabelParameterDefinition.class.isInstance(paramDef)) {
                    return new NodeLabelBuildWrapper();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NodeLabelParameterValue{" +
                "label='" + label + '\'' +
                '}';
    }
}
