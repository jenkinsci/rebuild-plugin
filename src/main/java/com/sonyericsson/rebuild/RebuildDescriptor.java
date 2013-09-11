package com.sonyericsson.rebuild;

import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class holds the configuration values for the rebuild action.
 */
public final class RebuildDescriptor extends GlobalConfiguration {
    private final RebuildConfiguration rebuildConfiguration = new RebuildConfiguration(Boolean.TRUE);

    /**
     * Constructs a new Descriptor implementation.
     */
    public RebuildDescriptor() {
        load();
    }

    @Override
    public String getDisplayName() {
        return "Rebuild";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        this.rebuildConfiguration.setRememberPasswordEnabled(
                Boolean.valueOf(formData.getString("rememberPasswordEnabled")));
        save();
        return true;
    }

    /**
     * Gets the configuration object.
     *
     * @return the configuration object.
     */
    public RebuildConfiguration getRebuildConfiguration() {
        return rebuildConfiguration;
    }
}
