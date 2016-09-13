package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by beazlr02 on 23/04/16.
 */
@Extension
public class StagePromotionDSL extends GlobalVariable {


    @Nonnull
    @Override
    public String getName() {
        return "stagePromotion";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull CpsScript cpsScript) throws Exception {
        return new GroovyPipelineScript().getGroovyScriptObject(cpsScript, getName());
    }

    @Extension
    public static class MiscWhitelist extends ProxyWhitelist {
        public MiscWhitelist() throws IOException {
            super(new StaticWhitelist(
                    "method java.util.Map$Entry getKey",
                    "method java.util.Map$Entry getValue"
            ));
        }
    }

}
