package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LemmaEntity lemma1)) return false;
        return getId().equals(lemma1.getId()) && getSite().equals(lemma1.getSite()) && getLemma().equals(lemma1.getLemma()) && getFrequency().equals(lemma1.getFrequency());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSite(), getLemma(), getFrequency());
    }
}
