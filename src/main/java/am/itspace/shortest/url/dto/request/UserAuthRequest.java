package am.itspace.shortest.url.dto.request;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthRequest {
    private String email;
    private String password;
}
