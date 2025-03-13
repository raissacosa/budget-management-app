package com.raissac.budget_management.category.entity;

import com.raissac.budget_management.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "category")
    private List<Transaction> transactions;
}
