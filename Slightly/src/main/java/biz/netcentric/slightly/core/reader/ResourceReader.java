package biz.netcentric.slightly.core.reader;

import java.net.URI;

/**
 * Reads resources, for this case index.html
 * With Adobe expirience manager you can upload resources
 *
 * Created by dev-camiloh on 2/21/17.
 */
public interface ResourceReader {

    Resource getResource(URI uri);

}
