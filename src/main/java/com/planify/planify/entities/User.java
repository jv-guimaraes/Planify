package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.planify.planify.dtos.UserResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Category> categories;

    @CreationTimestamp
    private Instant creationTimestamp;

    @UpdateTimestamp
    private Instant updateTimestamp;

    public UserResponseDto toResponseDto() {
        var responseTransactions = transactions.stream().map(Transaction::toResponseDto).toList();
        var responseCategories = categories.stream().map(Category::toResponseDto).toList();
        return new UserResponseDto(userId, username, email, responseTransactions, responseCategories, creationTimestamp, updateTimestamp);
    }
}
