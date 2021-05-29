package application.model.rules.ruleloader;

/**
 * Directives that can be exported to Golly / Lifelib should use this
 */
public interface Exportable {
    /**
     * Exports the directive to Golly / Lifelib
     * @return The exported directive to be parsed by Golly or Lifelib
     */
    String export();
}
