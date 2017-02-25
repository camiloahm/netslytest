package biz.netcentric.slightly.core.parser.element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by dev-camiloh on 2/25/17.
 */
public class ScriptEvaluatorTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";
    private ElementEvaluator scriptElementEvaluator;

    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
        scriptElementEvaluator = new ScriptElementEvaluator();
    }


    @Test
    public void evalNoneScriptElement() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 tittle=\\\"tittle\\\"></h1>\";");
        String htmlBeforeEval = doc.html();
        Elements elements = doc.getAllElements();
        elements.forEach(x -> scriptElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(htmlBeforeEval, equalTo(doc.html()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void evalInvalidScriptElement() {
        Document doc = Jsoup.parseBodyFragment("<script type=\"server/javascript\">\n" +
                "importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=request.getParameter(\"id\");\n" +
                "var person=Person.lookup(id);\n" +
                "</script>");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> scriptElementEvaluator.evalElement(engine, x, new ArrayList<>()));
    }

    @Test
    public void evalValidScriptElement() throws ScriptException {
        Document doc = Jsoup.parseBodyFragment("<script type=\"server/javascript\">\n" +
                "importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"1\"\n" +
                "var person=Person.lookup(id);\n" +
                "</script>");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> scriptElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        String name= (String) engine.eval("person.name");
        assertThat(name,equalTo("Name 1"));
    }

}
