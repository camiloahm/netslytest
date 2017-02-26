package biz.netcentric.slightly.core.parser;

import javax.servlet.http.HttpServletRequest;

/**
 * This is the core interface, activates Slightly processing
 * <p>
 * Created by dev-camiloh on 2/24/17.
 */
public interface SlightlyParser {

    String parse(HttpServletRequest request, String html);

}
