package biz.netcentric.slightly.core.parser.element;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.List;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public class ScriptElementEvaluator implements ElementEvaluator {

    private static final String SCRIPT_TAG = "script";

    /**
     * Look for script elements execute & evaluate against ScriptEngine
     *
     * @param engine
     * @param element
     */
    @Override
    public void evalElement(final ScriptEngine engine, final Element element, List<Node> trash) {
        String tagName = element.tagName();
        String data = element.data();
        if (tagName.equalsIgnoreCase(SCRIPT_TAG)) {
            try {
                engine.eval(data);
            } catch (ScriptException e) {
                throw new IllegalArgumentException("Script element cannot be evaluated: \n" + element.html());
            }
        }
    }
}
