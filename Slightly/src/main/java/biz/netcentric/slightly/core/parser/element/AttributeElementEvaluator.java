package biz.netcentric.slightly.core.parser.element;

import biz.netcentric.slightly.core.util.ExpressionResolver;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public class AttributeElementEvaluator implements ElementEvaluator {

    /**
     * transform all attributes according to el-expressions
     *
     * @param engine
     * @param element
     */
    @Override
    public void evalElement(ScriptEngine engine, Element element, List<Node> trash) {
        Attributes attributes = element.attributes();
        attributes.forEach(attr -> {
            attr.setValue(ExpressionResolver.resolveExpression(engine, attr.getValue()));
        });
    }
}
