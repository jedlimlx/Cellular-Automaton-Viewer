package sample.model.rules.ruleloader;

/**
 * Represents directives (@...) in *.rule files
 */
public abstract class Directive {
    /**
     * Name of the directive
     */
    protected String directiveName;

    /**
     * Constructs a directive containing the given content
     * @param content The content of the directive
     */
    public Directive(String content) {
        parseContent(content);
    }

    /**
     * Parses the content of the directive
     * @param content The content of the directive
     */
    public abstract void parseContent(String content);

    /**
     * The name of the directive
     * @return The name of the directive
     */
    public String getDirectiveName() {
        return directiveName;
    }

    /**
     * Deep copies the directive
     * @return Return a deepcopy of the directive
     */
    public abstract Object clone();
}
