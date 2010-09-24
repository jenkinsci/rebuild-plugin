/*
 *  The MIT License
 * 
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

import hudson.Extension;
import hudson.model.ManagementLink;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Adds a new "Configure Rebuild plugin" item in Hudson's Manage screen, allowing
 * to select the parameters to be taken into account by the plugin.
 *
 * @author Romain Seguy  (http://openromain.blogspot.com)
 */
@Extension
public class RebuildManagementLink extends ManagementLink {

  public void doSave(StaplerRequest req, StaplerResponse rsp) throws IOException {
    LOGGER.entering(CLASS_NAME, "doSave");

    try {
      getConfig().clear();

      // blocking param definitions
      LOGGER.fine("Processing the blockingParamDefs and blockingParamDefs JSON objects");

      JSONArray paramDefinitions = req.getSubmittedForm().getJSONArray("blockingParamDefs");
      JSONArray selectedParamDefinitions = req.getSubmittedForm().getJSONArray("selectedBlockingParamDefs");
      for(int i = 0; i < selectedParamDefinitions.size(); i++) {
        if(selectedParamDefinitions.getBoolean(i)) {
          getConfig().addBlockingParameterDefinition(paramDefinitions.getString(i));
        }
      }

      RebuildConfig.save(getConfig());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,
              "Failed to save Rebuild plugin configuration", e);
    }

    rsp.sendRedirect(req.getContextPath());
  }

  public RebuildConfig getConfig() {
    return RebuildConfig.getInstance();
  }

  @Override
  public String getIconFileName() {
    return "/plugin/rebuild/images/clock-48x48.png";
  }

  @Override
  public String getUrlName() {
    return "rebuild";
  }

  public String getDisplayName() {
    return ResourceBundleHolder.get(RebuildManagementLink.class).format("DisplayName");
  }

  private final static String CLASS_NAME = RebuildManagementLink.class.getName();
  private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

}
