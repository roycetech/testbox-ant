/**
 *
 */
package testbox.ant;

/**
 * Represents a nested Directory element
 *
 */
public class Directory {
    String path;

    /**
     * Default runner for directories
     */
    String runner = "";

    String remoteMethod = "run";

    String packageName = "testbox.testresults";

    String recurse = "false";

    /**
     * optional with dirrunner ... prefix of component org.foo.bar.MyComponent
     */
    String componentPath = "";

    String includes;

    String excludes;

    public Directory() {}

    public String getComponentPath()
    {
        return this.componentPath;
    }

    public String getExcludes()
    {
        return this.excludes;
    }

    public String getIncludes()
    {
        return this.includes;
    }

    public String getPackageName()
    {
        return this.packageName;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getRecurse()
    {
        return this.recurse;
    }

    public String getRemoteMethod()
    {
        return this.remoteMethod;
    }

    public String getRunner()
    {
        return this.runner;
    }

    public void setComponentPath(final String componentPath)
    {
        this.componentPath = componentPath;
    }

    public void setExcludes(final String excludes)
    {
        this.excludes = excludes;
    }

    public void setIncludes(final String includes)
    {
        this.includes = includes;
    }

    public void setPackageName(final String packageName)
    {
        this.packageName = packageName;
    }

    public void setPath(final String path)
    {
        this.path = path;
    }

    public void setRecurse(final String recurse)
    {
        this.recurse = recurse;
    }

    public void setRemoteMethod(final String remoteMethod)
    {
        this.remoteMethod = remoteMethod;
    }

    public void setRunner(final String runner)
    {
        this.runner = runner;
    }
}
