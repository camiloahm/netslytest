package biz.netcentric.slightly.core.element;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public interface ElementEvaluator {

    /**
     * @param engine
     * @param element
     * @throws IllegalArgumentException
     */
    void evalElement(final ScriptEngine engine, final Element element, final List<Node> trash);

}
