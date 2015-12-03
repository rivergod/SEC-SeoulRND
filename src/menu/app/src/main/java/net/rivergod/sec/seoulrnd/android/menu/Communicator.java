package net.rivergod.sec.seoulrnd.android.menu;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NoCache;
import com.navercorp.volleyextensions.cache.universalimageloader.disc.impl.UniversalUnlimitedDiscCache;
import com.navercorp.volleyextensions.cache.universalimageloader.memory.impl.UniversalLimitedAgeMemoryCache;
import com.navercorp.volleyextensions.cache.universalimageloader.memory.impl.UniversalLruMemoryCache;

import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO;
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by rivergod on 15. 12. 1.
 */
public class Communicator {
    /**
     * Set default max cache size = 10 MiB
     */
    private static final int DEFAULT_CACHE_SIZE = 10 * 1024 * 1024;

    /**
     * Set default max age = 5min.
     */
    private static final long DEFAULT_MAX_AGE = 300;

    private static final String TAG = "Communicator";

    private static RequestQueue requestQueue;
    private static ImageLoader imageLoader;
    private static EventBus eventBus;

    public static void init(Context context) {
        if (context == null)
            throw new NullPointerException("context must not be null.");

        Cache diskCache = getDefaultDiskCache(context);
        ImageLoader.ImageCache memoryCache = getDefaultImageCache(context);
        requestQueue = new RequestQueue(diskCache, new BasicNetwork(
                new HurlStack()));

        imageLoader = new ImageLoader(requestQueue, memoryCache);

        eventBus = new EventBus();

        requestQueue.start();
    }


    public static RequestQueue getRequestQueue() {
        if (requestQueue == null)
            throw new IllegalStateException("RequestQueue is not initialized.");
        return requestQueue;
    }

    public static ImageLoader getImageLoader() {
        if (imageLoader == null)
            throw new IllegalStateException("ImageLoader is not initialized.");
        return imageLoader;
    }

    public static EventBus getEventBus() {
        if (eventBus == null)
            throw new IllegalStateException("EventBus is not initialized.");
        return eventBus;
    }

    private static ImageLoader.ImageCache getDefaultImageCache(Context context) {
        return new UniversalLimitedAgeMemoryCache(new UniversalLruMemoryCache(
                DEFAULT_CACHE_SIZE), DEFAULT_MAX_AGE);
    }

    private static Cache getDefaultDiskCache(Context context) {
        File cacheDir = getCacheDir(context);
        if (cacheDir == null) {
            return new NoCache();
        }

        return new UniversalUnlimitedDiscCache(cacheDir);
    }

    private static File getCacheDir(Context context) {
        File file = new File(context.getCacheDir().getPath()
                + "/test-universal");
        return file;
    }


    public static void getMenu(final Date targetDate) {
        if (targetDate == null) {

        }

        Communicator.getRequestQueue().add(new Request<String>(Request.Method.POST, "http://www.welstory.com/menu/seoulrnd/menu.jsp", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Communicator.getEventBus().post(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0");

                return headers;
            }

            @Override
            protected void deliverResponse(String response) {
                Log.d(TAG, "received data length = " + response.length());

                Communicator.getEventBus().post(processMenuPageHtml(response));
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                try {
                    parsed = new String(response.data, "EUC-KR");
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
        });
    }

    private static DayCuisionsDTO processMenuPageHtml(String menuPageHtml) {

        final DayCuisionsDTO retVal = new DayCuisionsDTO();

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();

        props.setAllowHtmlInsideAttributes(true);
        props.setAllowMultiWordAttributes(true);
        props.setRecognizeUnicodeChars(true);
        props.setOmitXmlDeclaration(true);
        props.setOmitComments(true);

        TagNode node = null;
        node = cleaner.clean(menuPageHtml);

        Object[] menus = null;

        Object[] dates = null;
        try {
            dates = node.evaluateXPath("//span[@class='date']");
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        if (dates != null && dates.length > 0) {
            Log.d(TAG, "date = " + ((TagNode) dates[0]).getText().toString());
            retVal.setDate(((TagNode) dates[0]).getText().toString().trim());
        }

        //breakfast
        menus = new Object[0];
        try {
            menus = node.evaluateXPath("//div[@id='layer1']//div[@class='container_CafeA']");
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        for (Object menu : menus) {
            if (menu instanceof TagNode) {
                CuisineDTO cuisine = new CuisineDTO();
                TagNode cafeATagNode = (TagNode) menu;
                cuisine.setMealCode(CuisineDTO.MEALCODE_BREAKFAST);
                cuisine.setCafeteriaUrl(cafeATagNode.getChildTags()[0].getChildTags()[0].getChildTags()[0].getAttributeByName("src"));
                cuisine.setTitle(Html.fromHtml(cafeATagNode.getChildTags()[0].getText().toString()).toString());
                cuisine.setContent(Html.fromHtml(cafeATagNode.getChildTags()[1].getText().toString()).toString());

                retVal.addCuisine(cuisine);
            }
        }

        //lunch
        menus = new Object[0];
        try {
            menus = node.evaluateXPath("//div[@id='layer2']//div[@class='container_CafeA']");
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        for (Object menu : menus) {
            if (menu instanceof TagNode) {
                CuisineDTO cuisine = new CuisineDTO();
                TagNode cafeATagNode = (TagNode) menu;
                cuisine.setMealCode(CuisineDTO.MEALCODE_LAUNCH);
                cuisine.setCafeteriaUrl(cafeATagNode.getChildTags()[0].getChildTags()[0].getChildTags()[0].getAttributeByName("src"));
                cuisine.setTitle(Html.fromHtml(cafeATagNode.getChildTags()[0].getText().toString()).toString());
                cuisine.setContent(Html.fromHtml(cafeATagNode.getChildTags()[1].getText().toString()).toString());

                retVal.addCuisine(cuisine);
            }
        }

        //dinner
        menus = new Object[0];
        try {
            menus = node.evaluateXPath("//div[@id='layer3']//div[@class='container_CafeA']");
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        for (Object menu : menus) {
            if (menu instanceof TagNode) {
                CuisineDTO cuisine = new CuisineDTO();
                TagNode cafeATagNode = (TagNode) menu;
                cuisine.setMealCode(CuisineDTO.MEALCODE_DINNER);
                cuisine.setCafeteriaUrl(cafeATagNode.getChildTags()[0].getChildTags()[0].getChildTags()[0].getAttributeByName("src"));
                cuisine.setTitle(Html.fromHtml(cafeATagNode.getChildTags()[0].getText().toString()).toString());
                cuisine.setContent(Html.fromHtml(cafeATagNode.getChildTags()[1].getText().toString()).toString());

                retVal.addCuisine(cuisine);
            }
        }

        return retVal;

    }
}
