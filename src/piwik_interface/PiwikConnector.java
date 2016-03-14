package piwik_interface;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;


public abstract class PiwikConnector {

    protected final String HOST_URL;                // Base URL for all Requests
    protected final String API_KEY;                 // API Key for authentication
    protected static final String FORMAT = "json";  // Change only if you write your own export systems.
    protected final String LANGUAGE;                // Resource Language
    protected String expanded;                      // changeable
    protected String flat;                          // changeable
    protected final String LIMIT_RESULTS;           // Limit Results

    public PiwikConnector(String HOST_URL, String API_KEY) {
        this.HOST_URL = HOST_URL;
        this.API_KEY = API_KEY;
        this.LANGUAGE = "en";
        this.LIMIT_RESULTS = "-1";
        expanded = "0";
        flat = "0";
    }

    public PiwikConnector(String HOST_URL, String LIMIT_RESULTS, String API_KEY,
                          String LANGUAGE, String expanded, String flat) {
        this.LIMIT_RESULTS = LIMIT_RESULTS;
        this.expanded = expanded;
        this.flat = flat;
        this.API_KEY = API_KEY;
        this.LANGUAGE = LANGUAGE;
        this.HOST_URL = HOST_URL;
    }

    /**
     * Gets all data from the pre-configured sources
     * It should always be a JsonObject in response ^(needs validation)
     * @return JsonObject with the entire PIWIK response
     * @throws MalformedURLException Should be thrown if the implementation has a screwed up base url
     * @throws IOException Should be thrown if the remote resource is not available
     */
    protected JsonObject getData() throws MalformedURLException, IOException {
        final String parameter = buildParametricString();
        final String urlString = HOST_URL + parameter;
        final URL url = new URL(urlString);
        final HttpURLConnection request = (HttpURLConnection) url.openConnection();

        final JsonParser jp = new JsonParser();
        final JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        return root.getAsJsonObject();
    }

    /**
     * Should return a String[2] array with: Module, Endpoint
     * @return
     */
    protected abstract String[] getAPIEndpoint();

    /**
     * All other parameters which are not in the standard API description
     * @return
     */
    protected abstract HashMap<String, String> getParameters();

    /**
     * Standard parameter description for any point of time.
     * @return
     */
    protected abstract HashMap<String, String> getPeriod();

    protected String buildParametricString() {
        final String[] endPoint = getAPIEndpoint();
        if(endPoint.length != 2) {
            throw new IllegalArgumentException("Length of getAPIEndpoint is invalid");
        }

        StringBuilder sb = new StringBuilder();
        HashMap<String, String> params = getParameters();
        params.putAll(new HashMap<String, String>(){{
                put("module", endPoint[0]);                      // < This is probably bad practice
                put("method", endPoint[0] + "." + endPoint[1]);  // < If not, then this is
                put("token_auth", API_KEY);
                put("language", LANGUAGE);
                put("limit", LIMIT_RESULTS);
                put("expanded", expanded);
                put("flat", "flat");
            }}
        );
        params.putAll(getPeriod());

        for(final String s: params.keySet()) {
            sb.append(s);                       // key
            sb.append("=");                     // =
            sb.append(params.get(s));           // value
            sb.append("&");                     // &
        }
        return sb.substring(0, sb.length()-1);
    }

    protected static String translateDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
