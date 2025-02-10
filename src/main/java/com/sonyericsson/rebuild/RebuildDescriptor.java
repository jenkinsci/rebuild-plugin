package com.sonyericsson.rebuild;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest2;

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

    @NonNull
    @Override
    public String getDisplayName() {
        return "Rebuild";
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject formData) {
        this.rebuildConfiguration.setRememberPasswordEnabled(
                Boolean.parseBoolean(formData.getString("rememberPasswordEnabled")));
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
