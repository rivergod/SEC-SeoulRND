package net.rivergod.sec.seoulrnd.android.menu_seoulrnd;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.navercorp.volleyextensions.volleyer.response.parser.NetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.util.Assert;

import java.io.UnsupportedEncodingException;

/**
 * Created by rivergod on 2015-12-02.
 */
public class EucKrStringNetworkResponseParser implements NetworkResponseParser {

    public EucKrStringNetworkResponseParser() {
    }

    protected final String getBodyString(NetworkResponse response) throws UnsupportedEncodingException {
        return new String(response.data, "EUC-KR");
    }

    @Override
    public <T> Response<T> parseNetworkResponse(NetworkResponse response, Class<T> clazz) {
        Assert.notNull(response, "The response");
        Assert.notNull(clazz, "The class token");
        try {
            String result = getBodyString(response);
            @SuppressWarnings("unchecked")
            Response<T> successResponse = (Response<T>) Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
            return successResponse;
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
