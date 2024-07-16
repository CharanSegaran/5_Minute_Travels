package ibf.tfip.__minute_travels.dtos;


public class RegisterUserDto {
    private String signUpEmail;
    
    private String signUpPassword;

    private String signUpConfirmPassword;
    
    private String fullName;

    public String getSignUpEmail() {
        return signUpEmail;
    }

    public void setSignUpEmail(String email) {
        this.signUpEmail = email;
    }

    public String getSignUpPassword() {
        return signUpPassword;
    }

    public void setSignUpPassword(String password) {
        this.signUpPassword = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
