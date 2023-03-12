package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Objects;

@ToString
@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_SITE_ID",
            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES site(id) on delete cascade on update cascade"))
    @ToString.Exclude
    private Site site;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL, INDEX IDX_PAGE_PATH (path(512))")
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Page page = (Page) o;
        return getId() != null && Objects.equals(getId(), page.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
