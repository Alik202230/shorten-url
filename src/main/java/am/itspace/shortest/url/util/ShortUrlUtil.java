package am.itspace.shortest.url.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class ShortUrlUtil {

  private ShortUrlUtil() {}

  public static final Supplier<String> generateKey = () -> {
    final String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    final StringBuilder stringBuilder = new StringBuilder();

    ThreadLocalRandom random = ThreadLocalRandom.current();

    for (int i = 0; i < 6; i++) {
      int randomIndex = random.nextInt(character.length());
      stringBuilder.append(character.charAt(randomIndex));
    }
    return stringBuilder.toString();
  };
}
