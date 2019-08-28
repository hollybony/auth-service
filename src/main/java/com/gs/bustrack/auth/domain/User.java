package com.gs.bustrack.auth.domain;

import javax.persistence.*;
import java.util.Set;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

/**
 * 
 * @author Carlos Juarez
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
            "user_name"
        }),
        @UniqueConstraint(columnNames = {
            "email"
        })
})
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @NotNull
    @Size(max = 40)
    @Email
    private String email;
    
    @NotBlank
    @Size(max = 15)
    @Column(name = "user_name")
    private String name;

    @NotBlank
    private String password;

    @Size(min = 8, max = 8)
    @Column(name = "service_id")
    private String serviceId;
    
    @Column(name = "enabled")
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}