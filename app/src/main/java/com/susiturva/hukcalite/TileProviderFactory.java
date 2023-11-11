package com.susiturva.hukcalite;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class TileProviderFactory {
    public static WMSTileProvider getOsgeoWmsTileProvider() {


        //This is configured for:
        // http://beta.sedac.ciesin.columbia.edu/maps/services
        // (TODO check that this WMS service still exists at the time you try to run this demo,
        // if it doesn't, find another one that supports EPSG:900913
        /*final String WMS_FORMAT_STRING =
                "https://tiles.kartat.kapsi.fi/peruskartta?" +
                        "service=WMS" +
                        "&version=1.1.1" +
                        "&request=GetMap" +
                        "&layers=topp:states" +
                        "&bbox=%f,%f,%f,%f" +
                        "&width=256" +
                        "&height=256" +
                        "&srs=EPSG:3067" +  // NB This is important, other SRS's won't work.
                        "&format=image/jpeg" +
                        "&transparent=true";*/
        //https://tiles.kartat.kapsi.fi/ortokuva?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image/jpeg&TRANSPARENT=false&LAYERS=ortokuva&CRS=EPSG:3067&BGCOLOR=0xffffff&STYLES=&WIDTH=500&HEIGHT=500&BBOX=350000,6939200,350500,6939700
        final String WMS_FORMAT_STRING =
                "https://tiles.kartat.kapsi.fi/peruskartta?" +
                        "&SERVICE=WMS" +
                        "&VERSION=1.3.0" +
                        "&REQUEST=GetMap" +
                        "&FORMAT=image/jpeg" +
                        "&TRANSPARENT=false" +
                        "&LAYERS=Peruskartta" +
                        "&CRS=EPSG:3067" +
                        "&BGCOLOR=0xffffff" +
                        "&STYLES=" +

                        "&WIDTH=256" +
                        "&HEIGHT=256" +  // NB This is important, other SRS's won't work.
                        "&BBOX=%f,%f,%f,%f";


        WMSTileProvider tileProvider = new WMSTileProvider(256,256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                //String s = String.format(Locale.US, WMS_FORMAT_STRING, bbox[MINX],
                        //bbox[MINY], bbox[MAXX], bbox[MAXY]);
                String s = String.format("https://tiles.kartat.kapsi.fi/peruskartta/%d/%d/%d.jpg", zoom, x, y);
                if (!checkTileExists(x, y, zoom)){
                    return null;
                }
                Log.d("WMSDEMO", s);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
            private boolean checkTileExists(int x, int y, int zoom) {
                int minZoom = 12;
                int maxZoom = 16;

                return (zoom >= minZoom && zoom <= maxZoom);
            }
        };
        return tileProvider;
    }
}
