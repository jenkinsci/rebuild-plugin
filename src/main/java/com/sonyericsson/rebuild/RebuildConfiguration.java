package com.sonyericsson.rebuild;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

public class RebuildConfiguration implements Describable {

    public boolean rememberPassword;

    @DataBoundConstructor
    public RebuildConfiguration(boolean rememberPassword) {
        this.rememberPassword = rememberPassword;
    }

    @Override
    public Descriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(RebuildConfiguration.class);
    }

    public boolean getRememberPassword() {
        return rememberPassword;
    }

    @Override
    public String toString() {
        return "RebuildConfiguration{" +
                "rememberPassword=" + rememberPassword +
                '}';
    }
}
