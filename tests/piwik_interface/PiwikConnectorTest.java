package piwik_interface;

import org.junit.Test;

import java.sql.Date;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Mio on 14.03.2016.
 */
public class PiwikConnectorTest {

    @Test
    public void testTranslateDate() throws Exception {
        assertEquals("Test String is not equal to expected String", "2016-01-01", PiwikConnector.translateDate(new Date(1451602800000L)));
    }

    @Test
    public void testBuildParametricString() throws Exception {
        PiwikConnector pwc = new PiwikConnector("https://localhost/piwik/index.php?", "s59330") {
            @Override
            protected String[] getAPIEndpoint() {
                return new String[]{"API", "getBulkRequest"};
            }

            @Override
            protected HashMap<String, String> getParameters() {
                return new HashMap<>();
            }

            @Override
            protected HashMap<String, String> getPeriod() {
                return new HashMap<String, String>(){{
                    put("period", "month");
                    put("date", "2016-01-01");
                }};
            }
        };

        assertEquals("Parametric String is not as expected",
                "date=2016-01-01&expanded=0&period=month&method=API.getBulkRequest" +
                        "&flat=flat&module=API&limit=-1&token_auth=s59330&language=en",
                pwc.buildParametricString());
    }
}