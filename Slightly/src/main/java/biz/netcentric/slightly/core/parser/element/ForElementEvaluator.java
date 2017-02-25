package biz.netcentric.slightly.core.parser.element;

import biz.netcentric.slightly.core.util.ExpressionResolver;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public class ForElementEvaluator implements ElementEvaluator {

    private static final String FOR_TAG = "for-";

    /**
     * Check for data-for attribute
     * the example data-for-child="person.children"
     * the child variable is implicitly mapped to the items in the collection
     *
     * @param engine
     * @param element
     */
    @Override

    public void evalElement(final ScriptEngine engine, final Element element, final List<Node> trash) {

        Map<String, String> dataSet = element.dataset();
        Bindings session = engine.getBindings(engine.getContext().ENGINE_SCOPE);

        dataSet.forEach((k, v) -> {
            if (k.startsWith(FOR_TAG)) {
                String implicitItem = k.substring(4, k.length());
                try {
                    List items = (List) engine.eval(v);
                    Element dummy = element.clone();
                    //remove its data specific properties to avoid new calls
                    dummy.attributes().dataset().clear();
                    element.html("");
                    items.forEach(item -> {
                        Element temp = dummy.clone();
                        //put {child} in session
                        session.put(implicitItem, item);
                        temp.html(ExpressionResolver.resolveExpression(engine, temp.text()));
                        element.appendChild(temp);
                    });
                } catch (ScriptException e) {
                    throw new IllegalArgumentException("For element cannot be evaluated: \n" + element.html());
                }
            }
        });
    }
}
