package am.itspace.shortest.url.controller;

import am.itspace.shortest.url.dto.ShortUrlRequest;
import am.itspace.shortest.url.dto.ShortUrlResponse;
import am.itspace.shortest.url.model.ShortUrl;
import am.itspace.shortest.url.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class ShortUrlControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createShortUrl() throws Exception {

    ShortUrlRequest request = new ShortUrlRequest();
    request.setOriginalUrl("http://www.google.com");

    ShortUrl shortUrl = new ShortUrl();
    shortUrl.setShortKey("123456");
    shortUrl.setOriginalUrl("http://www.google.com");

    ShortUrlResponse response = new ShortUrlResponse();
    response.setShortKey("123456");
    response.setOriginalUrl("http://www.google.com");
    response.setClickCount(0);
    response.setIsActive(false);
    response.setUser(User.builder()
        .email("eden")
        .firstName("Eden")
        .lastName("Li")
        .password("emkenf")

        .build());
    response.setId(1L);

    mockMvc.perform(
            MockMvcRequestBuilders.post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortUrl))
        )
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

}
