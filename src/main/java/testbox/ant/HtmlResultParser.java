/**
 *
 */
package testbox.ant;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author Royce
 */
class HtmlResultParser {

    public static void main(final String[] args) throws Exception
    {
        final HtmlResultParser parser = new HtmlResultParser();
        System.out.println(
            parser.parse(
                "http://localhost:8080/ecgateway/_tests/unit/utils/ListUtilSpec.cfc"));

        System.out.println(parser.getSummary());
    }

    private transient int totalTestRuns = 1;
    private transient int totalFailures = 0;
    private transient int totalErrors = 1;
    private transient int totalTime = 0;

    private transient String responseBody;

    private transient String summary = "1,0,1,0";

    /**
     * @param summary in the format: 'Pass: 3 Fail: 0 Errors: 0'
     */
    private void computeTotal(final Document doc)
    {
        final Elements titleElement = doc.select("title");
        final String summary = titleElement.text();
        final Pattern pattern =
                Pattern.compile("(?<=Pass|Fail|Errors):\\s(\\d+)");

        final Matcher matcher = pattern.matcher(summary);

        this.totalTestRuns = 0;
        matcher.find();
        this.totalTestRuns += Integer.parseInt(matcher.group(1));

        matcher.find();
        this.totalFailures = Integer.parseInt(matcher.group(1));
        this.totalTestRuns += this.totalFailures;

        matcher.find();
        this.totalErrors = Integer.parseInt(matcher.group(1));
        this.totalTestRuns += this.totalErrors;
    }

    /**
     * Global Stats (1567 ms)
     *
     * @param doc
     */
    private void extractTestTime(final Document doc)
    {
        final Elements statElement = doc.select("#globalStats h2");
        final String timeText = statElement.text();

        final Pattern pattern = Pattern.compile("(\\d+)(?= ms)");
        final Matcher matcher = pattern.matcher(timeText);

        if (matcher.find(0)) {
            this.totalTime = Integer.parseInt(matcher.group(1));
        }
    }

    /**
     * Generate summary compatible to mxunit format. It requires that
     * #computeTotal have been called already.
     *
     * @param doc
     */
    private void generateSummary(final Document doc)
    {
        this.summary =
                String.valueOf(getTotalTestRuns()) + ',' + getTotalErrors()
                        + ',' + getTotalFailures() + ',' + getTotalTime();
    }

    public double getErrorRatio()
    {
        double prod = 0.0;
        double retVal = 0.0;
        if (getTotalTestRuns() > 0) {
            prod = ((double) getTotalErrors() / (double) getTotalTestRuns());
        }
        if (prod == 0.0) {
            retVal = 0.0;
        } else {
            retVal = prod;
        }
        return retVal;
    }

    public double getFailureRatio()
    {
        double prod = 0.0;
        double retVal = 0.0;
        if (getTotalTestRuns() > 0) {
            prod = ((double) getTotalFailures() / getTotalTestRuns());
        }
        if (prod == 0.0) {
            retVal = 0.0;
        } else {
            retVal = prod;
        }
        return retVal;
    }

    String getResponseBody()
    {
        return this.responseBody;
    }

    public double getSuccessRatio()
    {
        double prod = 0.0;
        final double errorsAndFailures =
                (double) getTotalErrors() + (double) getTotalFailures();
        if (getTotalTestRuns() > 0) {
            prod = (errorsAndFailures / getTotalTestRuns());
        }
        return (1 - prod);
    }

    String getSummary()
    {
        return this.summary;
    }

    int getTotalErrors()
    {
        return this.totalErrors;
    }

    int getTotalFailures()
    {
        return this.totalFailures;
    }

    int getTotalTestRuns()
    {
        return this.totalTestRuns;
    }

    int getTotalTime()
    {
        return this.totalTime;
    }


    boolean hasError()
    {
        return getTotalErrors() > 0;
    }

    boolean hasFailure()
    {
        return getTotalFailures() > 0;
    }

    public int parse(final String url) throws IOException
    {
        final Document doc;
        final Connection connection = Jsoup.connect(url);
        doc = connection.ignoreHttpErrors(true).timeout(0).get();
        this.responseBody = doc.html().trim();

        final int httpStatus = connection.response().statusCode();
        if (httpStatus < 400) {
            computeTotal(doc);
            extractTestTime(doc);
            generateSummary(doc);
        }
        return connection.response().statusCode();
    }


    public boolean testsAreClean()
    {
        return getTotalFailures() + getTotalErrors() == 0;
    }
}
