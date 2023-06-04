package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "lemma")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_LEMMA_SITE_ID",
            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES site(id) on delete cascade on update cascade"))
    @ToString.Exclude
    private SiteEntity site;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    public LemmaEntity(SiteEntity site, String lemma) {
        this(site, lemma, 0);
    }

    public LemmaEntity(SiteEntity site, String lemma, Integer frequency) {
        this.id = 0;
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        LemmaEntity lemma = (LemmaEntity) o;
        return getId() != null && Objects.equals(getId(), lemma.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
