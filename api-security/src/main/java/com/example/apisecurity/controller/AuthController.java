package com.example.apisecurity.controller;

import com.example.apisecurity.data.User;
import com.example.apisecurity.data.UserDao;
import com.example.apisecurity.exception.PasswordNotMatchError;
import com.example.apisecurity.service.AuthService;
import com.example.apisecurity.service.Jwt;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello";
    }
    record RequestUser(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName, String email,
            String password,
            @JsonProperty("confirm_password") String confirmPassword){}
    record RegisterResponse(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,String email){}

    @PostMapping("/register")
    public RegisterResponse
    register(@RequestBody RequestUser requestUser){
        User user= authService.register(
                requestUser.firstName(),
                requestUser.lastName(),
                requestUser.email(),
                requestUser.password(),
                requestUser.confirmPassword());
        return new RegisterResponse(user.getFirstName(),
                user.getLastName(),user.getEmail());
    }

    record LoginRequest(String email,String password){}
    record LoginResponse(String token){}
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest,
                               HttpServletResponse response){

        var login =authService.login(loginRequest.email
                ,loginRequest.password());
        Cookie cookie=new Cookie("refresh_token",
                login.getRefreshToken().getToken());
        cookie.setMaxAge(3600);
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        response.addCookie(cookie);
        return new LoginResponse(
                login.getAccessToken().getToken()
        );
    }
    record UserResponse(
            @JsonProperty("first_name")String firstName,
            @JsonProperty("last_name")String lastName,
            String email
    ){}
    @GetMapping("/user")
    public UserResponse user(HttpServletRequest request){
        User user=(User)request.getAttribute("user");
        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        ) ;
    }
    record RefreshResponse(String token){}
    @PostMapping("/refresh")
    public RefreshResponse
    refresh(@CookieValue("refresh_token")String refreshToken){
        System.out.println(refreshToken);
        return new RefreshResponse(
                authService.refreshAccess(refreshToken)
                        .getAccessToken().getToken()
        );
    }



}