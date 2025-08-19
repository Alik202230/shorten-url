package am.itspace.shortest.url.dto.request;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
