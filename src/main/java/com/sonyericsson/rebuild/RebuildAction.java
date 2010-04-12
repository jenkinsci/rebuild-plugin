/*********************************************************************
 *  ____                      _____      _                           *
 * / ___|  ___  _ __  _   _  | ____|_ __(_) ___ ___ ___  ___  _ __   *
 * \___ \ / _ \| '_ \| | | | |  _| | '__| |/ __/ __/ __|/ _ \| '_ \  *
 *  ___) | (_) | | | | |_| | | |___| |  | | (__\__ \__ \ (_) | | | | *
 * |____/ \___/|_| |_|\__, | |_____|_|  |_|\___|___/___/\___/|_| |_| *
 *                    |___/                                          *
 *                                                                   *
 *********************************************************************
 * Copyright 2010 Sony Ericsson Mobile Communications AB.            *
 * All rights, including trade secret rights, reserved.              *
 ********************************************************************/

package com.sonyericsson.rebuild;

import hudson.EnvVars;
import hudson.model.Action;
import java.util.Map;

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
            url.append(e.getKey());
            url.append('=');
            url.append(e.getValue());
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
