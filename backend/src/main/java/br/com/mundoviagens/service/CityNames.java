package br.com.mundoviagens.service;

import java.text.Normalizer;
import java.util.Locale;

final class CityNames {
    private CityNames() {}
    static String normalize(String city) {
        return Normalizer.normalize(city.replaceAll("\\s*\\([A-Za-z]{3}\\)\\s*$", "").strip(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
    static boolean same(String first, String second) { return normalize(first).equals(normalize(second)); }
}
