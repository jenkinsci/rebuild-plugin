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

import hudson.ExtensionList;
import hudson.XmlFile;
import hudson.model.FileParameterDefinition;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.PasswordParameterDefinition;
import hudson.model.RunParameterDefinition;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class to manage rebuild global settings.
 *
 * @author Romain Seguy  (http://openromain.blogspot.com)
 */
public class RebuildConfig {

  private final static String CONFIG_FILE = "com.sonyericsson.rebuild.RebuildConfig.xml";
  private static RebuildConfig rebuildConfig;

  /**
   * Contains the set of {@link ParameterDefinition}s that must block the
   * possibility to use the Rebuild action.
   */
  private Set<String> blockingParamDefinitionClasses;

  private RebuildConfig() {
    blockingParamDefinitionClasses = new LinkedHashSet<String>();

    // default values for the first time the config is created
    addBlockingParameterDefinition(FileParameterDefinition.class.getName());
    addBlockingParameterDefinition(RunParameterDefinition.class.getName());
    addBlockingParameterDefinition(PasswordParameterDefinition.class.getName());
  }

  /**
   * @param className The class name of a {@link ParameterDescriptor} to be added
   *                  to the list of parameters which will prevent the rebuild
   *                  action to be enabled for a build
   */
  public void addBlockingParameterDefinition(String className) {
    blockingParamDefinitionClasses.add(className);
  }

  public void clear() {
    blockingParamDefinitionClasses.clear();
  }

  public static RebuildConfig getInstance() {
    if(rebuildConfig == null) {
      rebuildConfig = load();
    }
    return rebuildConfig;
  }

  /**
   * Returns a map of all {@link ParameterDefinition}s that can be used in jobs.
   *
   * <p>The key is the class name of the {@link ParameterDefinition}, the value
   * is its display name.</p>
   */
  public static Map<String, String> getParameterDefinitions() {
    Map<String, String> params = new HashMap<String, String>();

    ExtensionList<ParameterDefinition.ParameterDescriptor> paramExtensions =
            Hudson.getInstance().getExtensionList(ParameterDefinition.ParameterDescriptor.class);
    for(ParameterDefinition.ParameterDescriptor paramExtension: paramExtensions) {
      // we need the getEnclosingClass() to drop the inner ParameterDescriptor
      // and work directly with the ParameterDefinition
      params.put(paramExtension.getClass().getEnclosingClass().getName(), paramExtension.getDisplayName());
    }

    return params;
  }

  public boolean isBlocking(String paramDefinitionClassName) {
    return blockingParamDefinitionClasses.contains(paramDefinitionClassName);
  }

  private static XmlFile getConfigFile() {
    return new XmlFile(new File(Hudson.getInstance().getRootDir(), CONFIG_FILE));
  }

  public static RebuildConfig load() {
    LOGGER.entering(CLASS_NAME, "load");
    try {
      return (RebuildConfig) getConfigFile().read();
    }
    catch(FileNotFoundException fnfe) {
      LOGGER.log(Level.WARNING,
              "No configuration found for Rebuild plugin");
    }
    catch(Exception e) {
      LOGGER.log(Level.WARNING,
              "Unable to load Rebuild plugin configuration from " + CONFIG_FILE, e);
    }

    return new RebuildConfig();
  }

  public static void save(RebuildConfig config) throws IOException {
    LOGGER.entering(CLASS_NAME, "save");
    getConfigFile().write(config);
    LOGGER.exiting(CLASS_NAME, "save");
  }

  private final static String CLASS_NAME = RebuildConfig.class.getName();
  private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

}
