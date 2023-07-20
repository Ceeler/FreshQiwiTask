public class Valute {
    String id;
    String name;
    String charCode;
    double value;
    int nominal;

    @Override
    public String toString() {
        //USD (Доллар США): 61,2475
        return charCode + " (" + name + "): " + value;
    }
}
