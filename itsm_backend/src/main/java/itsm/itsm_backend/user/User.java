package itsm.itsm_backend.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import itsm.itsm_backend.chat.Chat;
import itsm.itsm_backend.common.BaseAuditingEntity;
import itsm.itsm_backend.ticket.Ticket;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@NamedQuery(name = UserConstants.FIND_USER_BY_EMAIL,query = "select u from User  u where u.email= :email")
@NamedQuery(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF
        ,query = "select u from User  u where u.id!= :publicId")
@NamedQuery(name = UserConstants.FIND_USER_BY_PUBLIC_ID,
        query = "select u from User  u where u.id = :publicId")
@JsonIgnoreProperties(ignoreUnknown = true)
public  class User extends BaseAuditingEntity implements UserDetails, Principal {
    @Id
    @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enable;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    @Column(name = "\"group\"")
    private Group group;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Chat> chatsAsSender;
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    private List<Chat> chatsAsRecipient;
    @OneToMany(mappedBy = "recipient",cascade = CascadeType.ALL)
    private List<Ticket> ticketsAsRecipient;
    @OneToMany(mappedBy = "sender",cascade = CascadeType.ALL)
    private List<Ticket> ticketsAsSender;
    @ManyToOne(fetch = FetchType.LAZY)
    private LevelOfUser levelOfUser;

    @Override
    public String getName() {
        return email;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }

    public String fullName(){
        return firstname+" "+lastname;
    }


    public boolean isUserOnline() {
        return true;
    }
}
