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
@Table(name = "`index`")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false, foreignKey = @ForeignKey(name = "FK_INDEX_PAGE_ID",
            foreignKeyDefinition = "FOREIGN KEY (page_id) REFERENCES page(id) on delete cascade on update cascade"))
    @ToString.Exclude
    private PageEntity page;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false, foreignKey = @ForeignKey(name = "FK_INDEX_LEMMA_ID",
            foreignKeyDefinition = "FOREIGN KEY (lemma_id) REFERENCES lemma(id) on delete cascade on update cascade"))
    @ToString.Exclude
    private LemmaEntity lemma;

    @Column(name = "`rank`", nullable = false)
    private Float rank;

    public IndexEntity(PageEntity page, LemmaEntity lemma, Float rank) {
        this.id = 0;
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntity index)) return false;
        return getId().equals(index.getId()) && getPage().equals(index.getPage()) && getLemma().equals(index.getLemma()) && getRank().equals(index.getRank());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPage(), getLemma(), getRank());
    }
}
