package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.HashMap;

/**
 * Created by beazlr02 on 25/04/16.
 */

@ExportedBean(defaultVisibility=2)
public class KVStore extends JobProperty<WorkflowJob> {

    @Whitelisted
    private HashMap<String, String> map;

    public KVStore() {
        map = new HashMap<String, String>();
    }

    public void store(String key, String value) {
        map.put(key,value);
    }

    public String retrieve(String key) {
        return map.get(key);
    }

    @Override
    public JobPropertyDescriptor getDescriptor() {
        return new JobPropertyDescriptor() {
            @Override
            public String getDisplayName() {
                return "A Simple KV Store";
            }
        };
    }
}
