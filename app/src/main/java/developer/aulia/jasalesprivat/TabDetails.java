package developer.aulia.jasalesprivat;

public class TabDetails {
    private String tabName;
    private SignInSignUp.PlaceholderFragment fragment;

    public TabDetails(String tabName, SignInSignUp.PlaceholderFragment fragment) {
        this.tabName = tabName;
        this.fragment = fragment;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public SignInSignUp.PlaceholderFragment getFragment() {
        return fragment;
    }

    public void setFragment(SignInSignUp.PlaceholderFragment fragment) {
        this.fragment = fragment;
    }
}
