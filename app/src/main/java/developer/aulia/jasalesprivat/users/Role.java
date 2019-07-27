package developer.aulia.jasalesprivat.users;

public enum Role {
    STUDENT("STUDENT"),TUTOR("TUTOR"),ADMIN("ADMIN");
    private String text;
    Role(String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
