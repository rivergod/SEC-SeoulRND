package net.rivergod.sec.seoulrnd.android.menu_seoulrnd;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.navercorp.volleyextensions.volleyer.VolleyerConfiguration;
import com.navercorp.volleyextensions.volleyer.factory.DefaultNetworkResponseParserFactory;
import com.navercorp.volleyextensions.volleyer.http.HttpContent;
import com.navercorp.volleyextensions.volleyer.request.VolleyerRequest;
import com.navercorp.volleyextensions.volleyer.request.creator.RequestCreator;
import com.navercorp.volleyextensions.volleyer.request.executor.DefaultRequestExecutor;
import com.navercorp.volleyextensions.volleyer.request.executor.RequestExecutor;
import com.navercorp.volleyextensions.volleyer.response.parser.NetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.util.VolleyerLog;

/**
 * Created by rivergod on 2015-12-02.
 */
public class CommunicatorVolleyerConfigurationFactory {
    private static Response.ErrorListener defaultErrorListener = new Response.ErrorListener() {
        public void onErrorResponse(VolleyError error) {
            VolleyerLog.debug(error, "ERROR : ", new Object[0]);
        }
    };

    public CommunicatorVolleyerConfigurationFactory() {
    }

    public static NetworkResponseParser createNetworkResponseParser() {
        return DefaultNetworkResponseParserFactory.create();
    }

    public static RequestCreator createRequestCreator() {
        return new RequestCreator() {
            @Override
            public <T> Request<T> createRequest(HttpContent httpContent, Class<T> clazz, NetworkResponseParser responseParser, Response.Listener<T> listener, Response.ErrorListener errorListener) {
                return new VolleyerRequest(httpContent, clazz, responseParser, listener, errorListener);
            }
        };
    }

    public static RequestExecutor createRequestExecutor() {
        return new DefaultRequestExecutor();
    }

    public static Response.ErrorListener createErrorListener() {
        return defaultErrorListener;
    }

    public static VolleyerConfiguration create() {
        return CommunicatorVolleyerConfigurationFactory.ConfigurationHolder.configuration;
    }

    private static class ConfigurationHolder {
        private static final VolleyerConfiguration configuration;

        private ConfigurationHolder() {
        }

        private static VolleyerConfiguration getConfiguration() {
            return configuration;
        }

        static {
            RequestCreator requestCreator = CommunicatorVolleyerConfigurationFactory.createRequestCreator();
            RequestExecutor requestExecutor = CommunicatorVolleyerConfigurationFactory.createRequestExecutor();
            NetworkResponseParser networkResponseParser = CommunicatorNetworkResponseParserFactory.create();
            Response.ErrorListener errorListener = CommunicatorVolleyerConfigurationFactory.defaultErrorListener;
            configuration = new VolleyerConfiguration(requestCreator, requestExecutor, networkResponseParser, errorListener);
        }
    }
}
