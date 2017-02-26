package biz.netcentric.slightly.core.parser;

import biz.netcentric.slightly.core.parser.element.ElementEvaluator;
import biz.netcentric.slightly.core.util.ExpressionResolver;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * Provide an Slightly implementing class to Jsoup NodeTraversor to iterate through nodes.
 * The head method is called when the node is first seen,
 * and the tail method when all of the node's children have been visited
 * <p>
 *
 * Created by dev-camiloh on 2/24/17.
 */
public class SlightlyNodeVisitor implements NodeVisitor {

    private ScriptEngine engine;
    private List<ElementEvaluator> elementEvaluator;
    private List<Node> trash;

    public SlightlyNodeVisitor(ScriptEngine engine, List<ElementEvaluator> elementEvaluator, List<Node> trash) {
        this.engine = engine;
        this.elementEvaluator = elementEvaluator;
        this.trash = trash;
    }

    @Override
    public void head(Node node, int i) {
        if (node instanceof Element) {
            Element element = (Element) node;
            for (ElementEvaluator x : elementEvaluator)
                x.evalElement(engine, element, trash);

        } else if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            textNode.text(ExpressionResolver.resolveExpression(engine, textNode.text()));

        }
    }

    @Override
    public void tail(Node node, int i) {
    }
}
