package am.itspace.shortest.url.dto.response;

import am.itspace.shortest.url.model.User;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {

    private Long id;
    private String shortKey;
    private String originalUrl;
    private Integer clickCount;
    private Boolean isActive;
    private User user;
}
