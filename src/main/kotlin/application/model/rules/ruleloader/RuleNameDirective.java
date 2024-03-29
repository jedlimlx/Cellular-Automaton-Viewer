package application.model.rules.ruleloader;

public class RuleNameDirective extends Directive implements Exportable {
    private String name;

    public RuleNameDirective(String content) {
        super(content);
        directiveName = "RULE";
    }

    @Override
    public void parseContent(String content) {
        name = content.replace("@RULE ", "");
    }

    @Override
    public Object clone() {
        return new RuleNameDirective("@RULE " + name);
    }

    public String getName() {
        return name;
    }

    public String export() {
        return "@RULE " + name + "\n";
    }
}
