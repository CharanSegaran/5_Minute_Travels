package ibf.tfip.__minute_travels.dtos;


public class LoginResponse {
    private String token;

    private long expiresIn;

    public String getToken() {
        return token;
    }
    public void setToken(String token){
        this.token = token;
    }
    public void setExpiresIn(Long expirationTime){
        this.expiresIn = expirationTime;
    }
    public long getExpiresIn(){
        return expiresIn;
    }
}
