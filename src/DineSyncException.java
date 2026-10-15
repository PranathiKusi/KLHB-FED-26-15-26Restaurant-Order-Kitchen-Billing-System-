
/**
 * Parent of every DineSync business exception.
 * It extends Exception, so it is a CHECKED exception: the compiler forces
 * callers to either catch it or declare it with "throws".
 * Inheritance chain (multilevel): Throwable -> Exception -> DineSyncException -> e.g. InvalidPinException
 */
public class DineSyncException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String module;   // which DineSync module raised it, e.g. "M01"

    public DineSyncException(String module, String message) {
        super(message);
        this.module = module;
    }

    public String getModule() {
        return module;
    }
}
