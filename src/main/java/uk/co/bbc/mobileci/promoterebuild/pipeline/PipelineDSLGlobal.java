package uk.co.bbc.mobileci.promoterebuild.pipeline;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by beazlr02 on 23/04/16.
 */

public abstract class PipelineDSLGlobal extends GlobalVariable {

    public abstract String getFunctionName();

    @Override
    public String getName() {
        return getFunctionName();
    }



    @Override
    public Object getValue(CpsScript script) throws Exception {
        Binding binding = script.getBinding();

        CpsThread c = CpsThread.current();
        if (c == null)
            throw new IllegalStateException("Expected to be called from CpsThread");

        ClassLoader cl = getClass().getClassLoader();

        String scriptPath = "dsl/" + getFunctionName() + ".groovy";
        Reader r = new InputStreamReader(cl.getResourceAsStream(scriptPath), "UTF-8");

        GroovyCodeSource gsc = new GroovyCodeSource(r, getFunctionName() + ".groovy", cl.getResource(scriptPath).getFile());
        gsc.setCachable(true);


        Object pipelineDSL = c.getExecution()
                .getShell()
                .getClassLoader()
                .parseClass(gsc)
                .newInstance();
        binding.setVariable(getName(), pipelineDSL);
        r.close();


        return pipelineDSL;
    }

}