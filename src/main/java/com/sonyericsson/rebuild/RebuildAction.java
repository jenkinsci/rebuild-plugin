/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications.
 *  Copyright (c) 2010, Manufacture Francaise des Pneumatiques Michelin, Romain Seguy
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
import hudson.model.Action;
import java.util.Map;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RebuildAction implements Action {

    private String rebuildurl;
    public static final String BASE = "../buildWithParameters?";

    public RebuildAction(EnvVars env) {
        loadString(env);
    }

    private void loadString(EnvVars env) {
        StringBuilder url = new StringBuilder(BASE);

        for (Map.Entry<String, String> e : env.entrySet()) {
            //& not required for first Parameters
            if (url.length() > BASE.length()) {
                url.append('&');
            }
            try {
                url.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                url.append('=');
                url.append(URLEncoder.encode(e.getValue(), "UTF-8"));
            } catch (java.io.UnsupportedEncodingException err) {
                LOGGER.log(Level.WARNING, "Error: {0}", err.getMessage());
            }
        }
        rebuildurl = url.toString();
    }

    public String getIconFileName() {
        return "/plugin/rebuild/images/clock-48x48.png";
    }

    public String getDisplayName() {
        return "Rebuild";
    }

    public String getUrlName() {
        return rebuildurl;
    }

    private final static String CLASS_NAME = RebuildAction.class.getName();
    private final static Logger LOGGER = Logger.getLogger(RebuildAction.class.getName());

}
