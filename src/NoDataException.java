
/** No data (billed orders etc.) for the requested report (module M08). */
public class NoDataException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public NoDataException(String message) {
        super("M08", message);
    }
}
