package am.itspace.shortest.url.repository;

import am.itspace.shortest.url.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

  Optional<ShortUrl> findByShortKey(String shortKey);

  Optional<ShortUrl> findByOriginalUrl(String originalUrl);

}
