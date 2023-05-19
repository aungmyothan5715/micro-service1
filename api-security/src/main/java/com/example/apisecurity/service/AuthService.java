package com.example.apisecurity.service;

import com.example.apisecurity.data.Token;
import com.example.apisecurity.data.User;
import com.example.apisecurity.data.UserDao;
import com.example.apisecurity.exception.InvalidEmailError;
import com.example.apisecurity.exception.PasswordNotMatchError;
import com.example.apisecurity.exception.UnAuthenticatedError;
//import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    @Value("${secret.access-token.key}")
    private String accessSecret;
    @Value("${secret.refresh-token.key}")
    private String refreshSecret;

    public AuthService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String firstName, String lastName,
                         String email, String password, String confirmPassword){
        if(!password.equals(confirmPassword)){
            throw new PasswordNotMatchError();
        }
        try{
            return userDao.save(
                    User.of(firstName,lastName,email,
                            passwordEncoder.encode(password))
            );
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Duplicate Email Error!");
        }


    }


    public Login login(String email, String password) {
        var user=userDao.findByEmail(email)
                .orElseThrow(InvalidEmailError::new);
        if(!passwordEncoder.matches(password,user.getPassword())){
            throw new PasswordNotMatchError();
        }
        var login = Login.of(user.getId(),
                accessSecret,refreshSecret);
        var refreshToken=login.getRefreshToken();
        user.addToken(new Token(refreshToken.getToken(),
                refreshToken.getIssuedAt(),
                refreshToken.getExpiredAt()));
        userDao.save(user);

        return login;
    }


    public User getUserFromToken(String token) {
        return userDao.findById(Jwt.from(token,accessSecret)
                        .getUserId())
                .orElseThrow(()-> new UsernameNotFoundException("User Not Found!"));
    }

    public Login refreshAccess(String refreshToken) {
        var refreshJwt=Jwt.from(refreshToken,refreshSecret);
        var user=userDao
                .findUserIdAndTokenByRefreshToken(refreshJwt.getUserId(),
                        refreshJwt.getToken(),
                        refreshJwt.getExpiredAt());

        if(user.isPresent()){
            User u=user.get();
            return Login.of(
                    u.getId(),
                    accessSecret,
                    refreshSecret
            );
        }
        else {
            System.out.println("Query Error!");
        }
        return null;

    }
}