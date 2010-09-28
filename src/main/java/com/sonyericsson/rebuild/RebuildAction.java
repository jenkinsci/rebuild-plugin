/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.rebuild;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.RunParameterValue;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RebuildAction implements Action {

    private String rebuildurl;
    public static final String BASE = new String("../buildWithParameters?");
    private static final Logger logger = Logger.getLogger(RebuildAction.class.getName());
    private static final String UTF_8 = "UTF-8";

    private void loadString(AbstractBuild<?, ?> build, ParametersAction p) {

        StringBuilder url = new StringBuilder(BASE);

        List<ParameterValue> parameters = p.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (url.length() > BASE.length()) {
                url.append('&');
            }
            if (parameters.get(i) instanceof RunParameterValue) {
                RunParameterValue runparam = (RunParameterValue) parameters.get(i);
                url = handleRunParameter(runparam, url);
            } else {
                ParameterValue paramvalue = parameters.get(i);
                EnvVars env = new EnvVars();
                paramvalue.buildEnvVars(build, env);
                for (Map.Entry<String, String> e : env.entrySet()) {
                    try {
                        url.append(URLEncoder.encode(e.getKey(), UTF_8));
                        url.append('=');
                        url.append(URLEncoder.encode(e.getValue(), UTF_8));
                    } catch (UnsupportedEncodingException ex) {
                        logger.log(Level.SEVERE, "Parameter Encoding is not supported", ex);
                    }

                }

            }

        }
        rebuildurl = url.toString();
    }

    public RebuildAction(AbstractBuild<?, ?> build, ParametersAction p) {
        loadString(build, p);
    }

    public String getIconFileName() {
        return "clock.gif";
    }

    public String getDisplayName() {
        return "Rebuild";
    }

    public String getUrlName() {
        return rebuildurl;
    }

    /**
     * Handle the run parameter value.
     *
     * During rebuild run parameter is handle by this function , the function
     * will add the run parameter url part to rebuild url.
     * @param runparam
     *        runparam is an instance of RunParameter class.
     * @param url
     *        url for rebuild.
     */
    public StringBuilder handleRunParameter(RunParameterValue runparam, StringBuilder url) {
        try {
            url.append(URLEncoder.encode(runparam.getName(), UTF_8));
            url.append('=');
            url.append(URLEncoder.encode(runparam.getRunId(), UTF_8));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "Parameter Encoding is not supported", ex);
        }
        return url;
    }
}
