package biz.netcentric.slightly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by camiloh on 2/21/17.
 */
public class SlightlySoup {

    private Logger log = LoggerFactory.getLogger(SlightlySoup.class);
    private ScriptEngine engine;

    public SlightlySoup() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        log = LoggerFactory.getLogger(SlightlySoup.class);
    }

    public String parse(String html, HttpServletRequest request) throws ScriptException {

        Document doc = Jsoup.parse(html);
        engine.eval("load(\"nashorn:mozilla_compat.js\");");
        Bindings global = engine.getBindings(engine.getContext().GLOBAL_SCOPE);
        Bindings session = engine.getBindings(engine.getContext().ENGINE_SCOPE);
        session.put("request", request);

        List<Node> trash = new ArrayList<Node>();

        doc.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {

                if (node instanceof Element) {
                    Element element = (Element) node;
                    String tagName = element.tagName();
                    String data = element.data();

                    evalJavascript(tagName, data);
                    evalAttributes(element);
                    evalIf(element, trash);
                    evalFor(element, session);

                } else if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    log.info("Entering tag: " + textNode.nodeName() + " Depth: " + depth);
                    log.info("TextNode: " + textNode.text());
                    textNode.text(expression(textNode.text()));
                } else {
                    //Unsupported Types, DO NOTHING
                    //Comment, DataNode, DocumentType, XmlDeclaration
                    //FormElement and Document are subclasses of Element
                }
            }

            public void tail(Node node, int depth) {
                log.info("Exiting tag: " + node.nodeName() + " Depth: " + depth);
            }
        });

        //Remove all nodes that are in the trash
        trash.forEach(node -> {
            node.remove();
        });

        System.out.println("=====================================================================================");
        System.out.println("TRANSFORMED");
        System.out.println("=====================================================================================");
        System.out.println(doc.html());
        // Print engine scopes
        engine.getContext().getScopes().forEach(System.out::println);
        System.out.println("=====================================================================================");
        System.out.println("GLOBAL BINDINGS");
        System.out.println("=====================================================================================");
        // Print global bindings
        global.forEach((k, v) -> System.out.println("Key: " + k + " Value : " + v));
        System.out.println("=====================================================================================");
        System.out.println("SESSION BINDINGS");
        System.out.println("=====================================================================================");
        // Print session bindings
        session.forEach((k, v) -> System.out.println("Key: " + k + " Value : " + v));
        return doc.html();
    }

    private void evalFor(Element element, Bindings session) {
        //check for data-for attribute
        // ala the example data-for-child="person.children"
        // the child variable is implicitly mapped to the items in the collection
        // referenced in the data for value, i.e. for child in children
        // notice the code below, neither child nor children is mention, implicit mapping is taking place

        Map<String, String> dataSet = element.dataset();

        dataSet.forEach((k, v) -> {
            if (k.startsWith("for-")) {
                String implicitItem = k.substring(4, k.length()); //get the suffix of for-*
                log.info("Implicit item: " + implicitItem + " Collection: " + v);
                try {
                    @SuppressWarnings("rawtypes")
                    List items = (List) engine.eval(v);
                    //Create the dummy element from a clone and remove its data specific properties
                    //to prevent subsequent calls on the traverse
                    Element dummy = element.clone();
                    dummy.attributes().dataset().clear();
                    element.html("");
                    //For each item in items, set the implicitItem variable and then evaluate the expression
                    items.forEach(item -> {
                        Element temp = dummy.clone();
                        session.put(implicitItem, item);
                        temp.html(expression(temp.text())); // Evaluate the expression
                        element.appendChild(temp);//Add another element after this one
                    });
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void evalAttributes(Element element) {
        Attributes attributes = element.attributes();
        //transform all attributes according to el-expressions
        attributes.forEach(attr -> {
            attr.setValue(expression(attr.getValue()));
        });
    }

    private void evalIf(Element element, List<Node> trash) {
        //check for data-if attribute, if it doesn't resolve to true,
        //then trash element and its children
        Map<String, String> dataSet = element.dataset();

        try {
            String condition = dataSet.get("if");
            if (condition != null && !condition.isEmpty()) {
                //What does this if resolve to? evaluate in nashorn
                Boolean dataIf = (Boolean) engine.eval(condition);
                //can not remove while traversing, so we must trash and remove later
                if (!dataIf) {
                    trash.add(element);
                }
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    private void evalJavascript(String tagName, String data) {
        //look for script elements execute & evaluate against ScriptEngine
        if (tagName.equalsIgnoreCase("script")) {
            try {
                engine.eval(data);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    public String expression(String html) {
        Pattern el$expression = Pattern.compile("\\$\\{(\\w+)\\.?(\\w+)\\}");
        //String html ="<h1 title=\"${person.name}\">${person.name} ${person}</h1>";
        log.info("$Expression: " + html);
        Matcher action = el$expression.matcher(html);
        StringBuffer sb = new StringBuffer(html.length());
        while (action.find()) {
            String text = action.group(0);
            String unwrapped = text.substring(2, text.length() - 1);
            try {
                String eval = (String) engine.eval(unwrapped);
                action.appendReplacement(sb, Matcher.quoteReplacement(eval));
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        action.appendTail(sb);
        return sb.toString();
    }

}


