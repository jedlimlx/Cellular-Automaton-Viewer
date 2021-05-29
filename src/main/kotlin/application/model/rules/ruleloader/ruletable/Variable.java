package application.model.rules.ruleloader.ruletable;

import java.util.Set;

public class Variable {
    private final String name;
    private final boolean unbounded;
    private final Set<Integer> values;

    public Variable(String name, boolean unbounded, Set<Integer> values) {
        this.name = name;
        this.values = values;
        this.unbounded = unbounded;
    }

    public String getName() {
        return name;
    }

    public boolean isUnbounded() {
        return unbounded;
    }

    public Set<Integer> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", unbounded=" + unbounded +
                ", values=" + values +
                '}';
    }
}
