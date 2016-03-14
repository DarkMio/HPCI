package piwik_interface.piwik_endpoints;

import piwik_interface.PiwikConnector;

import java.util.HashMap;

public class PiwikTransitionsForAction extends PiwikConnector {

    private String actionName;

    public PiwikTransitionsForAction(String HOST_URL, String ID_SITE, String API_KEY, String urlTarget) {
        super(HOST_URL, ID_SITE, API_KEY);
        actionName = urlTarget;
    }

    @Override
    protected String[] getAPIEndpoint() {
        return new String[]{"Transitions", "getTransitionsForAction"};
    }

    @Override
    protected HashMap<String, String> getParameters() {
        return new HashMap<String, String>(){{
            put("limitBeforeGrouping", "-1");
            put("actionType", "url");
            put("actionName", actionName);
        }};
    }
}
