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
import hudson.model.Action;
import java.util.Map;
import java.net.URLEncoder;

public class RebuildAction implements Action {

    private String rebuildurl;
    public static final String BASE = new String("../buildWithParameters?");

    private void loadString(EnvVars env) {
        StringBuffer url = new StringBuffer(BASE);

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
                System.out.println("Error :" + err.getMessage());
            }

        }
        rebuildurl = url.toString();
    }

    public RebuildAction(EnvVars env) {
        loadString(env);
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
}
