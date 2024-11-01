package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.planify.planify.dtos.user.UserResponseDto;
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

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Goal> goals;

    @CreationTimestamp
    private Instant creationTimestamp;

    @UpdateTimestamp
    private Instant updateTimestamp;

    public UserResponseDto toResponseDto() {
        return new UserResponseDto(
                userId,
                username,
                email,
                transactions.stream().map(Transaction::toResponseDto).toList(),
                categories.stream().map(Category::toShortResponseDto).toList(),
                goals.stream().map(Goal::toShortResponseDto).toList(),
                creationTimestamp,
                updateTimestamp
        );
    }
}
