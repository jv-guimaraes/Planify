package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.planify.planify.dtos.category.CategoryShortResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_categories")
public class Category implements Comparable<Category> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "category")
    @JsonManagedReference
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "category")
    @JsonManagedReference
    private List<Goal> goals;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public CategoryShortResponseDto toShortResponseDto() {
        return new CategoryShortResponseDto(categoryId, name);
    }

    @Override
    public int compareTo(Category o) {
        return name.compareTo(o.getName());
    }
}
