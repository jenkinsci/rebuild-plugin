/*
 * The MIT License
 * 
 * Copyright (c) 2013 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonyericsson.rebuild;

import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ParameterValue;

/**
 * Extension point to provide views to show parameters in rebuild page.
 * 
 * If you want your custom {@link ParameterValue} to work with rebuild plugin,
 * do as followings:
 * <ol>
 *   <li>Add a dependency to rebuild plugin to your pom.xml.
 *       You should specify &lt;optional&gt;true&lt;/optional&gt;
 *       to have your plugin work without rebuild plugin.</li>
 *   <li>Define a class implementing {@link RebuildParameterProvider}.</li>
 *   <li>Annotate the class with {@link Extension}.
 *       You should specify option=true in {@link Extension} annotation
 *       to have your plugin work without rebuild plugin.</li>
 *   <li>Override {@link RebuildParameterProvider#getRebuildPage(ParameterValue)}.
 *       Don't forget to return <code>null</code> for parameter values
 *       other than your custom {@link ParameterValue}.
 *       There are two recommended ways to set values to {@link RebuildParameterPage}:
 *       <table>
 *           <tr>
 *               <th>&nbsp;</th>
 *               <th>Recommended 1</th>
 *               <th>Recommended 2</th>
 *           </tr>
 *           <tr>
 *               <th>clazz</th>
 *               <td>your custom {@link ParameterValue}</td>
 *               <td>the class implementing {@link RebuildParameterProvider}</td>
 *           </tr>
 *           <tr>
 *               <th>page</th>
 *               <td>a file in the resource directory of your custom {@link ParameterValue}</td>
 *               <td>a file in the resource directory of the class implementing {@link RebuildParameterProvider}</td>
 *           </tr>
 *       </table>
 *   </li>
 * </ol>
 */
public abstract class RebuildParameterProvider implements ExtensionPoint {
    // This is defined not as an interface but as an abstract class.
    // If defined as an interface, developers might carelessly apply this
    // to mandatory class in their plugin and their plugins get not to work
    // without rebuild plugin.
    
    /**
     * Provide a view for specified {@link ParameterValue}.
     * 
     * Return null if cannot handle specified {@link ParameterValue}.
     * 
     * @param value a value to be shown in a rebuild page.
     * @return page for the parameter value. null for parameter values cannot be handled.
     */
    public abstract RebuildParameterPage getRebuildPage(ParameterValue value);
    
    /**
     * @return all {@link RebuildParameterProvider} registered to Jenkins.
     */
    public static ExtensionList<RebuildParameterProvider> all() {
        return Jenkins.getInstance().getExtensionList(RebuildParameterProvider.class);
    }
}
