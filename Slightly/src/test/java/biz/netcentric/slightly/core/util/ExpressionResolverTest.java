package biz.netcentric.slightly.core.util;

import org.junit.Before;
import org.junit.Test;
import org.hamcrest.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by dev-camiloh on 2/25/17.
 */
public class ExpressionResolverTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";

    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
        engine.eval("importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"1\";\n" +
                "var person=Person.lookup(id);");
    }

    @Test
    public void resolveNonELExpression() {
        String el = "<h1 tittle=\"tittle\"></h1>";
        String result = ExpressionResolver.resolveExpression(engine, el);
        assertThat(result, equalTo(el));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveInvalidPropertyInExpression() {
        String el = "${person.somethinsWrong}";
        ExpressionResolver.resolveExpression(engine, el);
    }

    @Test()
    public void resolveValidExpression() {
        String el = "<h1 title=\"${person.name}\">${person.name}</h1>\";";
        String result = ExpressionResolver.resolveExpression(engine, el);
        assertThat(result, equalTo("<h1 title=\"Name 1\">Name 1</h1>\";"));
    }

}
