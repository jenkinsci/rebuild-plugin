package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.Extension;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.ProxyWhitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;

import java.io.IOException;

/**
 * Created by beazlr02 on 23/04/16.
 */
@Extension
public class StagePromotionDSL extends PipelineDSLGlobal {

    @Override
    public String getFunctionName() {
        return "stagePromotion";
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
