package biz.netcentric.slightly.core;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dev-camiloh on 2/24/17.
 */
public interface SlightlyParser {

    String parse(HttpServletRequest request, String html);

}
