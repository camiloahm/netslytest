package biz.netcentric.slightly.core.parser.element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by dev-camiloh on 2/25/17.
 */
public class AttibuteElementEvaluatorTest {

    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";
    private ElementEvaluator attributeElementEvaluator;

    @Before
    public void init() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
        engine.eval(NASHRON_MOZILLA);
        engine.eval("importClass(Packages.biz.netcentric.app.Person);\n" +
                "var id=\"1\";\n" +
                "var person=Person.lookup(id);");
        attributeElementEvaluator = new AttributeElementEvaluator();
    }

    @Test()
    public void evalNonAttribute() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 tittle=\\\"tittle\\\"></h1>\";");
        String htmlBeforeEval = doc.html();
        Elements elements = doc.getAllElements();
        elements.forEach(x -> attributeElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(htmlBeforeEval, equalTo(doc.html()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void evalInvalidAttribute() {
        Document doc = Jsoup.parseBodyFragment("<h1 title=\"${wrong.wrong}\"></h1>");
        Elements elements = doc.getElementsByTag("h1");
        elements.forEach(x -> attributeElementEvaluator.evalElement(engine, x, new ArrayList<>()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void evalInvalidPropertyInAttribute() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 title=\"${person.wrong}\"></h1>\";\"");
        Elements elements = doc.getElementsByTag("h1");
        elements.forEach(x -> attributeElementEvaluator.evalElement(engine, x, new ArrayList<>()));
    }

    @Test()
    public void evalValidAttribute() {
        Document doc = Jsoup.parseBodyFragment("\"<h1 title=\"${person.name}\">${person.name}</h1>\";\"");
        Elements elements = doc.getAllElements();
        elements.forEach(x -> attributeElementEvaluator.evalElement(engine, x, new ArrayList<>()));
        assertThat(doc.html(), equalTo("<html>\n <head></head>\n <body>\n  \"\n  <h1 title=\"Name 1\">${person.name}</h1>\";\"\n </body>\n</html>"));
    }

}