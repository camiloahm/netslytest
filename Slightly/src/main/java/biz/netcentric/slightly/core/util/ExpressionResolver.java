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
        Pattern elExpression = Pattern.compile("\\$\\{(\\w+)\\.?(\\w+)\\}");
        Matcher action = elExpression.matcher(html);
        StringBuffer sb = new StringBuffer(html.length());
        while (action.find()) {
            String text = action.group(0);
            String unwrapped = text.substring(2, text.length() - 1);
            try {
                String eval = (String) engine.eval(unwrapped);
                action.appendReplacement(sb, Matcher.quoteReplacement(eval));
            } catch (ScriptException e) {
               throw new IllegalArgumentException("Expression cannot be resolved "+ html);
            } catch (NullPointerException e){
                throw new IllegalArgumentException("Value does not exist "+ html);
            }
        }
        action.appendTail(sb);
        return sb.toString();
    }

}
