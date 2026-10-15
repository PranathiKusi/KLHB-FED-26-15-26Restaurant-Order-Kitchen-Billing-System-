
/** Customer feedback for one bill (module M07). */
public class Feedback {
    private final String billId;
    private final String customerName;
    private final int overall;
    private final String[] dishNames;
    private final int[] dishRatings;
    private final String comment;

    public Feedback(String billId, String customerName, int overall, String[] dishNames, int[] dishRatings,
                    String comment) {
        this.billId = billId;
        this.customerName = customerName;
        this.overall = overall;
        this.dishNames = dishNames;
        this.dishRatings = dishRatings;
        this.comment = comment;
    }

    public String getBillId() { return billId; }
    public String getCustomerName() { return customerName; }
    public int getOverall() { return overall; }
    public String[] getDishNames() { return dishNames.clone(); }
    public int[] getDishRatings() { return dishRatings.clone(); }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return billId + " " + overall + "/5 by " + customerName + (comment.isEmpty() ? "" : " - \"" + comment + "\"");
    }
}
