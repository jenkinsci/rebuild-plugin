package com.sonyericsson.rebuild.node;

import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

public final class NodeLabelParameterDefinition extends SimpleParameterDefinition {
    private NodeLabelParameterDefinition(String name) {
        super(name);
    }

    @Override
    public SimpleParameterDefinition copyWithDefaultValue(ParameterValue defaultValueObj) {
        if (defaultValueObj instanceof NodeLabelParameterValue) {
            return new NodeLabelParameterDefinition(getName());
        } else {
            return this;
        }
    }

    @Override
    public NodeLabelParameterValue getDefaultParameterValue() {
        return new NodeLabelParameterValue(getName());
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        NodeLabelParameterValue value = req.bindJSON(NodeLabelParameterValue.class, jo);
        if(StringUtils.isBlank(value.getLabel())) {
            final String label = jo.optString("value");
            return new NodeLabelParameterValue(label);
        } else {
            return value;
        }
    }

    @Override
    public ParameterValue createValue(String value) {
        return new NodeLabelParameterValue(value);
    }
}
