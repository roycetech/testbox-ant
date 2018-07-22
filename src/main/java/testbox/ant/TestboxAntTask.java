/**
 *
 */
package testbox.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 */
public class TestboxAntTask extends Task {

    private final String version = "0.0.1";
    String server;
    int port = 80;
    String connectionMethod = "http";

    String outputdir;
    String testboxHome = "testbox";

    boolean verbose = false;
    boolean haltOnError = false;
    boolean haltOnFailure = false;

    // Name of the summary file that hold info about all tests that were run
    String testResultsSummary = "testresults.properties";

    String params;

    Vector<Testcase> testcases = new Vector<>();
    Vector<Directory> directories = new Vector<>();

    /** */
    private String failureProperty;

    /** */
    private String errorProperty;

    private transient String protocol = "http";

    private transient HtmlResultParser testResultParser;

    public TestboxAntTask() {
        this.testResultParser = new HtmlResultParser();
    }

    public void addText(final String text)
    {
        this.params = text.trim();
    }


    public Directory createDirectory()
    {
        final Directory directory = new Directory();
        this.directories.add(directory);
        return directory;
    }

    public Testcase createTestcase()
    {
        final Testcase testCase = new Testcase();
        this.testcases.add(testCase);
        return testCase;
    }

    /**
     *
     * @param queryString
     * @param fileName
     * @param runner
     * @throws BuildException when you set the halt on failure/error property to
     *             true.
     */
    private void doTest(final String filePath, final String fileName)
            throws BuildException
    {
        try {
            log("[Filename] " + filePath);
            final int status = runTest(this.server, this.port, filePath);

            final String summary = this.testResultParser.getSummary();

            log("[HttpStatus] " + status);

            if (status > 304) {
                log(
                    "[HttpStatus] -- error. Output was: "
                            + this.testResultParser.getResponseBody());
                throw new BuildException(
                    "Http Status returned an error. HttpStatus code: "
                            + status);
            }

            if (this.testResultParser.hasFailure()
                    && getFailureProperty() != null) {
                getProject().setProperty(getFailureProperty(), "true");

                if (this.haltOnFailure) {
                    throw new BuildException(
                        "haltonFailure property set to TRUE and one or more FAILURES occured. No files generated. Set the haltonfailure property to true in the TestboxUnit Ant task to view test result details");
                }
            }

            if (this.testResultParser.hasError()
                    && getErrorProperty() != null) {
                getProject().setNewProperty(getErrorProperty(), "true");

                if (this.haltOnError) {
                    throw new BuildException(
                        "haltonError property set to TRUE and one or more ERRORS occured. No files generated. Set the haltonerror property to true in the Testbox Ant task to view test result details");
                }
            }

            System.out.println("[Testresults] " + summary);

            if (this.outputdir != null) {
                final File file = new File(fileName);
                file.createNewFile();
                log("writing file : " + fileName);
                try (PrintWriter out = new PrintWriter(fileName)) {
                    out.println(this.testResultParser.getResponseBody());
                }
            }
        } catch (final java.io.IOException ioe) {
            System.out.println(
                "[testboxunit error] Error trying to write to : "
                        + this.outputdir
                        + ". Please make sure this output dircetory exists.");
            System.out.println(
                "[testboxunit error] Exiting ... see stacktrace for details.");
            ioe.printStackTrace();
        } catch (final BuildException be) {
            throw new BuildException(be);
        } catch (final Exception e) {
            throw new BuildException(e);
        }
    }

    @Override
    public void execute() throws BuildException
    {
        final DecimalFormat df = new DecimalFormat(".00");

        log("Greetings, earth being ...");
        log("Running TestboxAntTask version : " + this.version);

        if (this.outputdir != null) {
            log("Outputting results to: " + this.outputdir);
        }

        if (this.verbose) {
            log("Verbose: " + this.verbose);
            log("Testbox home :" + this.testboxHome);
            log("Using server:port : " + this.server + ":" + this.port);
        }

        runIndividualTestCases();

        log("Total testruns: " + this.testResultParser.getTotalTestRuns());
        log("Total errors: " + this.testResultParser.getTotalErrors());
        log("Total failures: " + this.testResultParser.getTotalFailures());
        log("Total time: " + this.testResultParser.getTotalTime());
        log(
            "Failure ratio: "
                    + df.format(this.testResultParser.getFailureRatio()));
        log("Error ratio: " + df.format(this.testResultParser.getErrorRatio()));
        log(
            "Success ratio: "
                    + df.format(this.testResultParser.getSuccessRatio()));

        if (this.outputdir != null) {
            // Can be used by Ant to conditionally process other tasks
            final Properties properties = new Properties();
            properties.setProperty(
                "total.runs",
                String.valueOf(this.testResultParser.getTotalTestRuns()));
            properties.setProperty(
                "total.errors",
                String.valueOf(this.testResultParser.getTotalErrors()));
            properties.setProperty(
                "total.failures",
                String.valueOf(this.testResultParser.getTotalFailures()));
            properties.setProperty(
                "total.time",
                String.valueOf(this.testResultParser.getTotalTime()));
            properties.setProperty(
                "failure.ratio",
                df.format(this.testResultParser.getFailureRatio()));
            properties.setProperty(
                "error.ratio",
                df.format(this.testResultParser.getErrorRatio()));
            properties.setProperty(
                "success.ratio",
                df.format(this.testResultParser.getSuccessRatio()));

            try {
                properties.store(
                    new FileOutputStream(
                        this.outputdir + "/" + this.testResultsSummary),
                    null);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        log("Fare thee well, human.");
    }

    public String getErrorProperty()
    {
        return this.errorProperty;
    }

    public String getFailureProperty()
    {
        return this.failureProperty;
    }

    private void runIndividualTestCases()
    {
        for (int i = 0; i < this.testcases.size(); i++) {
            final Testcase test = this.testcases.get(i);
            log("Loading Testcase : " + test.getName());

            final String outputFile =
                    this.outputdir + "/testboxtestcase_" + i + ".xml";

            log("Output file: " + outputFile);

            doTest(test.getAppName() + '/' + test.getTestPath(), outputFile);
        }
    }

    public int runTest(final String server, final int port,
                       final String filePath)
            throws Exception
    {
        if (port == 443) {
            this.protocol = "https";
        }

        final String url =
                this.protocol + "://" + server + ":" + port + '/' + filePath;

        System.out.println("Running URL : " + url);

        return this.testResultParser.parse(url);
    }

    public void setConnectionMethod(final String connectionMethod)
    {
        this.connectionMethod = connectionMethod;
    }

    public void setErrorProperty(final String errorProperty)
    {
        this.errorProperty = errorProperty;
    }

    public void setFailureProperty(final String failureProperty)
    {
        this.failureProperty = failureProperty;
    }

    public void setHaltOnError(final boolean haltOnError)
    {
        this.haltOnError = haltOnError;
    }

    public void setHaltOnFailure(final boolean haltOnFailure)
    {
        this.haltOnFailure = haltOnFailure;
    }

    public void setOutputdir(final String outputdir)
    {
        this.outputdir = outputdir;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public void setServer(final String server)
    {
        this.server = server;
    }

    public void setTestboxunitHome(final String testboxunitHome)
    {
        this.testboxHome = testboxunitHome;
    }
}
