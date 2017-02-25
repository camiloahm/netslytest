package biz.netcentric.slightly.core;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import biz.netcentric.slightly.core.element.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by camiloh on 2/21/17.
 */
public class SlightlyParserDefaultImpl implements SlightlyParser {

    private Logger log = LoggerFactory.getLogger(SlightlyParserDefaultImpl.class);
    private ScriptEngine engine;
    private static final String NASHRON_ENGINE = "nashorn";
    private static final String NASHRON_MOZILLA = "load(\"nashorn:mozilla_compat.js\");";

    public SlightlyParserDefaultImpl() {
        initEngine();
        log = LoggerFactory.getLogger(SlightlyParserDefaultImpl.class);
    }

    public String parse(HttpServletRequest request, String html) {
        Document doc = Jsoup.parse(html);
        Bindings session = engine.getBindings(engine.getContext().ENGINE_SCOPE);
        session.put("request", request);
        List<ElementEvaluator> elementEvaluators = getChainOfElementEvaluators();
        List<Node> trash = new ArrayList<>();
        doc.traverse(new SlightlyNodeVisitor(engine, elementEvaluators, trash));
        trash.forEach(x -> x.remove());

        return doc.html();
    }

    private List<ElementEvaluator> getChainOfElementEvaluators() {
        List<ElementEvaluator> elementEvaluators = new ArrayList<>();
        elementEvaluators.add(new ScriptElementEvaluator());
        elementEvaluators.add(new AttributeElementEvaluator());
        elementEvaluators.add(new IfElementEvaluator());
        elementEvaluators.add(new ForElementEvaluator());
        return elementEvaluators;
    }

    private void initEngine() {
        try {
            engine = new ScriptEngineManager().getEngineByName(NASHRON_ENGINE);
            engine.eval(NASHRON_MOZILLA);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Could no start engine " + NASHRON_MOZILLA);
        }
    }

}


