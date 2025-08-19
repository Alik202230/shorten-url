package am.itspace.shortest.url.mapper;

import am.itspace.shortest.url.dto.response.UserAuthResponse;
import am.itspace.shortest.url.model.User;
import org.springframework.http.HttpStatus;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserAuthResponse toAuthResponse(User user) {
        return UserAuthResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accessToken(user.getEmail())
                .role(user.getRole())
                .statusCode(HttpStatus.OK.value())
                .build();
    }

}
