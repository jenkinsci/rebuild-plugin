package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;

/**
 * Created by beazlr02 on 04/05/16.
 */
public class KVStoreProxy {
    private Run<?, ?> build;

    public KVStoreProxy(Run<?, ?> build) {

        this.build = build;
    }

    public void store(String key, String value) throws IOException {
        Job<?, ?> parent = build.getParent();
        WorkflowJob wParent = (WorkflowJob) parent;
        KVStore property = wParent.getProperty(KVStore.class);
        if(property==null) {
            property = new KVStore();
        } else {
            wParent.removeProperty(KVStore.class);
        }
        property.store(key, value);
        wParent.addProperty(property);
    }

    public String retrieve(String key) {
        Job<?, ?> parent = build.getParent();
        WorkflowJob wParent = (WorkflowJob) parent;
        KVStore property = wParent.getProperty(KVStore.class);
        String result = null;
        if(property!=null) {
            result = property.retrieve(key);
        }
        return result;
    }
}
