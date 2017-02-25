package biz.netcentric.slightly.core.reader;

import biz.netcentric.slightly.core.reader.Resource;

import java.net.URI;

/**
 * Created by dev-camiloh on 2/21/17.
 */
public interface ResourceReader {

    Resource getResource(URI uri);

}
