package net.rivergod.sec.seoulrnd.android.menu_seoulrnd;

import com.navercorp.volleyextensions.volleyer.response.parser.IntegratedNetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.response.parser.Jackson2NetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.response.parser.JacksonNetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.response.parser.NetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.response.parser.SimpleXmlNetworkResponseParser;
import com.navercorp.volleyextensions.volleyer.util.ClassUtils;

/**
 * Created by rivergod on 2015-12-02.
 */
public class CommunicatorNetworkResponseParserFactory {

    public CommunicatorNetworkResponseParserFactory() {
    }

    public static NetworkResponseParser create() {
        CommunicatorIntegratedNetworkResponseParser.Builder builder = new CommunicatorIntegratedNetworkResponseParser.Builder();
        return builder.build();
    }

}
