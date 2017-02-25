package biz.netcentric.slightly.core.util;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public class ExpressionResolver {

    public static String resolveExpression(ScriptEngine engine, String html) {
        Pattern el$expression = Pattern.compile("\\$\\{(\\w+)\\.?(\\w+)\\}");
        //String html ="<h1 title=\"${person.name}\">${person.name} ${person}</h1>";
        Matcher action = el$expression.matcher(html);
        StringBuffer sb = new StringBuffer(html.length());
        while (action.find()) {
            String text = action.group(0);
            String unwrapped = text.substring(2, text.length() - 1);
            try {
                String eval = (String) engine.eval(unwrapped);
                action.appendReplacement(sb, Matcher.quoteReplacement(eval));
            } catch (ScriptException e) {
               throw new IllegalArgumentException("Expression cannot be resolved "+ html);
            }
        }
        action.appendTail(sb);
        return sb.toString();
    }

}
