package biz.netcentric.slightly.core.parser.element;

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
public class IfElementEvaluator implements ElementEvaluator {

    private static final String IF_TAG = "if";

    /**
     * check for data-if attribute, if it doesn't eval to true,
     * then trash element and its children
     *
     * @param engine
     * @param element
     */
    @Override
    public void evalElement(final ScriptEngine engine, final Element element, final List<Node> trash) {
        Map<String, String> dataSet = element.dataset();
        Bindings session = engine.getBindings(engine.getContext().ENGINE_SCOPE);

        try {
            String condition = dataSet.get(IF_TAG);
            if (condition != null && !condition.isEmpty()) {
                //What does this if resolve to? evaluate in nashorn
                Boolean dataIf = (Boolean) engine.eval(condition);
                if (!dataIf) {
                    trash.add(element);
                }
            }


        } catch (ScriptException e) {
            throw new IllegalArgumentException("If element cannot be evaluated: \n" + element.html());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("If element cannot be evaluated: \n" + element.html() + " " + e.getMessage());
        }

    }
}
