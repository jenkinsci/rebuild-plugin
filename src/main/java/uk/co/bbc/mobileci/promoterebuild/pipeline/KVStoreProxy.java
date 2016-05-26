package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.*;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

import java.io.IOException;
import java.util.List;

/**
 * Created by beazlr02 on 04/05/16.
 */
public class KVStoreProxy {
    private Run<?, ?> build;

    public KVStoreProxy(Run<?, ?> build) {
        this.build = build;
    }

    public void store(String pkey, String value) throws IOException {
        String key = namespaceKeyForBuild(pkey, build);

        CredentialsStore store = credentialsStore();

        Credentials cred = new StringCredentialsImpl(CredentialsScope.GLOBAL, key, key, Secret.fromString(value));

        StringCredentialsImpl result = loadCredential(key, store);

        if(result==null) {
            store.addCredentials(Domain.global(), cred);
        } else {
            store.updateCredentials(Domain.global(), result, cred);
        }
    }


    public String retrieve(String pkey) {
        String key = namespaceKeyForBuild(pkey, build);

        CredentialsStore store = credentialsStore();

        StringCredentialsImpl result = loadCredential(key, store);

        return result==null?"":result.getSecret().getPlainText();
    }

    private static CredentialsStore credentialsStore() {
        return CredentialsProvider.lookupStores(Jenkins.getInstance()).iterator().next();
    }

    private static StringCredentialsImpl loadCredential(String key, CredentialsStore store) {
        StringCredentialsImpl result = null;

        List<Credentials> credentials = store.getCredentials(Domain.global());
        for (Credentials credential : credentials) {
            if(credential instanceof StringCredentialsImpl) {
                StringCredentialsImpl KVStringCredentials = (StringCredentialsImpl) credential;
                if(KVStringCredentials.getId().equals(key)){
                    result = KVStringCredentials;
                }
            }
        }
        return result;
    }


    private static String namespaceKeyForBuild(String pkey, Run<?, ?> build) {
        return build.getParent().getUrl()+pkey;
    }

}
