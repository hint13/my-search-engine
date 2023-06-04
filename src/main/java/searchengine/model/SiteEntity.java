package searchengine.model;

import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    private SiteStatus status;

    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    @UpdateTimestamp
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(name = "name", columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PageEntity> pages;

    public SiteEntity(String url, String name) {
        this.url = url;
        this.name = name;
        this.lastError = "";
    }

    public void updateStatus(SiteStatus status) {
        updateStatus(status, "");
    }

    public void updateStatus(SiteStatus status, String lastError) {
        setStatus(status);
        setStatusTime(LocalDateTime.now());
        setLastError(lastError);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SiteEntity site = (SiteEntity) o;
        return getId() != null && Objects.equals(getId(), site.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Site(<" + id + "> " + url + " [" + (pages != null ? pages.size() : 0) + "])";
    }
}
