package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import jakarta.persistence.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "page")
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey = @ForeignKey(name = "FK_PAGE_SITE_ID",
            foreignKeyDefinition = "FOREIGN KEY (site_id) REFERENCES site(id) on delete cascade on update cascade"))
    private SiteEntity site;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL, INDEX IDX_PAGE_PATH (path(512))")
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public PageEntity(SiteEntity site, String pagePath) {
        this.id = 0;
        this.site = site;
        this.path = pagePath;
    }

    public Optional<Document> loadContent(String userAgent, String referrer) {
        Connection conn = Jsoup.connect(getFullPath())
                .userAgent(userAgent)
                .referrer(referrer);
        Connection.Response response;
        try {
            response = conn.execute();
        } catch (IOException e) {
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            return Optional.empty();
        }
        setCode(response.statusCode());
        setContent(doc.toString());
        return Optional.of(doc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PageEntity page = (PageEntity) o;
        return getId() != null && Objects.equals(getId(), page.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Page(" +
                "<" + code + "> " +
                getFullPath() + ")";
    }

    public String getFullPath() {
        return site.getUrl() + path;
    }
}
