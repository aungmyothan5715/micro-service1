package com.example.apisecurity.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.MappedCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ToString
public class User {
    @Id
    @Getter
    private Long id;
    @Getter
    @Setter
    @JsonProperty("first_name")
    private String firstName;
    @Getter
    @Setter
    @JsonProperty("last_name")
    private String lastName;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String password;

    @MappedCollection
    private final Set<Token> tokens = new HashSet<>();

    public static User of(String firstName, String lastName, String email, String password){
        // this is static factory pattern
        // if you want to show data you can use in method as argument
        // this is pattern can use private constructor call other class
        return new User(null, firstName, lastName, email, password, Collections.emptySet());
    }

    //you must write default constructor
    public User(){}

    public void addToken(Token token){
        this.tokens.add(token);
    }


    @PersistenceConstructor
    private User(Long id, String firstName, String lastName, String email, String password, Collection<Token> tokens) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.tokens.addAll(tokens);
    }
}
