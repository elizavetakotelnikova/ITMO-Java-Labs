package org.example.entities.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entities.owner.Owner;

import java.util.List;

@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(targetEntity = Owner.class)
    @JoinColumn(name="owner_id", nullable = false)
    private Owner owner;
    private String password;
    private String username;
    @ManyToMany(targetEntity = Role.class, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authorities",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "authority_id") }
    )
    private List<Role> authorities;

    public User(Owner owner, String username, String password, List<Role> role) {
        this.owner = owner;
        this.username = username;
        this.password = password;
        this.authorities = role;
    }
}