package uk.co.bbc.mobileci.promoterebuild;

import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class holds the configuration values for the rebuild action.
 */
public final class PromoteRebuildDescriptor extends GlobalConfiguration {
    private final PromoteRebuildConfiguration rebuildConfiguration = new PromoteRebuildConfiguration(Boolean.TRUE);

    /**
     * Constructs a new Descriptor implementation.
     */
    public PromoteRebuildDescriptor() {
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
    public PromoteRebuildConfiguration getRebuildConfiguration() {
        return rebuildConfiguration;
    }
}
