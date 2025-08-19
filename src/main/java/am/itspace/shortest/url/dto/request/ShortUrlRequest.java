package am.itspace.shortest.url.dto.request;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRequest {
    private String originalUrl;
}
