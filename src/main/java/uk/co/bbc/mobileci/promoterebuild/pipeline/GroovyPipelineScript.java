package uk.co.bbc.mobileci.promoterebuild.pipeline;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.CpsThread;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
* Created by beazlr02 on 29/04/16.
*/
public class GroovyPipelineScript {

    public Object getGroovyScriptObject(CpsScript script, String functionName) throws InstantiationException, IllegalAccessException, IOException {
        Binding binding = script.getBinding();

        CpsThread c = CpsThread.current();
        if (c == null) {
            throw new IllegalStateException("Expected to be called from CpsThread");
        }

        ClassLoader cl = getClass().getClassLoader();

        String scriptPath = "dsl/" + functionName + ".groovy";
        Reader r = new InputStreamReader(cl.getResourceAsStream(scriptPath), "UTF-8");

        GroovyCodeSource gsc = new GroovyCodeSource(r, functionName + ".groovy", cl.getResource(scriptPath).getFile());
        gsc.setCachable(true);


        Object pipelineDSL = c.getExecution()
                .getShell()
                .getClassLoader()
                .parseClass(gsc)
                .newInstance();
        binding.setVariable(functionName, pipelineDSL);
        r.close();


        return pipelineDSL;
    }
}
