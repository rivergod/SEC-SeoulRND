package net.rivergod.sec.seoulrnd.android.menu_seoulrnd;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NoCache;
import com.navercorp.volleyextensions.cache.universalimageloader.disc.impl.UniversalUnlimitedDiscCache;
import com.navercorp.volleyextensions.cache.universalimageloader.memory.impl.UniversalLimitedAgeMemoryCache;
import com.navercorp.volleyextensions.cache.universalimageloader.memory.impl.UniversalLruMemoryCache;

import net.rivergod.sec.seoulrnd.android.menu_seoulrnd.dto.CuisineDTO;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.navercorp.volleyextensions.volleyer.Volleyer.volleyer;

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

        StringBuffer urlBuff = new StringBuffer();
        urlBuff.append("http://www.welstory.com/menu/seoulrnd/menu.jsp");

        Log.d(TAG, "Request GET getMenu URL = " + urlBuff.toString());

        //meal_type=2&course=AA&dt=20151201&dtFlag=2
        volleyer(Communicator.getRequestQueue())
                .post(urlBuff.toString())
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0")
                .withListener(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received data length = " + response.length());

                        HtmlCleaner cleaner = new HtmlCleaner();
                        CleanerProperties props = cleaner.getProperties(); // cleaner의 속성을 정의 변수

                        props.setAllowHtmlInsideAttributes(true);
                        props.setAllowMultiWordAttributes(true);
                        props.setRecognizeUnicodeChars(true);
                        props.setOmitXmlDeclaration(true); // ‘선언문을 제거’ 하라는 속성을 설정
                        props.setOmitComments(true); // ‘주석을 제거’ 하라는 속성을 설정

                        TagNode node = null; // 대상파일, URL자료를node 에 배열
                            node = cleaner.clean(response);

                        Object[] menus = null;

                        //breakfast

                        menus = new Object[0];
                        try {
                            menus = node.evaluateXPath("//div[@id='layer1']//div[@class='container_CafeA']");
                        } catch (XPatherException e) {
                            e.printStackTrace();
                        }

                        List<CuisineDTO> cuisines = new ArrayList<CuisineDTO>();

                        for (Object menu : menus) {
                            if (menu instanceof TagNode) {
                                CuisineDTO cuisine = new CuisineDTO();
                                TagNode cafeATagNode = (TagNode) menu;
                                cuisine.setMealCode(CuisineDTO.MEALCODE_BREAKFAST);
                                cuisine.setCafeteriaUrl(cafeATagNode.getChildTags()[0].getChildTags()[0].getChildTags()[0].getAttributeByName("src"));
                                cuisine.setTitle(cafeATagNode.getChildTags()[0].getText().toString());
                                cuisine.setContent(cafeATagNode.getChildTags()[1].getText().toString());

                                cuisines.add(cuisine);
                            }
                        }
                        Communicator.getEventBus().post(cuisines);

                    }
                })
                .withErrorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Communicator.getEventBus().post(error);
                    }
                })
                .execute();

    }
}
