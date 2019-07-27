package developer.aulia.jasalesprivat.sessions;

public enum Status {
    PENDING("PENDING"),ACCEPTED("ACCEPTED"),DECLINED("DECLINED");
    private String text;
    Status(String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
