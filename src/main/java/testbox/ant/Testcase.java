package testbox.ant;

/**
 * Represents a nested testcase element
 */
public class Testcase {
    String name;
    String appName;
    String testPath;

    String packageName = "testbox.testresults";


    public Testcase() {}

    public String getAppName()
    {
        return this.appName;
    }

    public String getName()
    {
        return this.name;
    }

    public String getPackageName()
    {
        return this.packageName;
    }

    public String getTestPath()
    {
        return this.testPath;
    }

    public void setAppName(final String appName)
    {
        this.appName = appName;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setPackageName(final String packageName)
    {
        this.packageName = packageName;
    }


    public void setTestPath(final String testPath)
    {
        this.testPath = testPath;
    }
}