package picky.parser.service;

public class FuncUtil {

    private FuncUtil() {}

    public static String normalizeName(String name) {
        return name.replace("https", "").chars()
            .filter(c -> Character.isAlphabetic(c) || Character.isDigit(c))
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append
            ).toString();
    }
}
