package com.harmony;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.gson.Gson;
import com.harmonymod.config.HarmonyConfig;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the config screen's translations against silent drift.
 *
 * <p>NeoForge's {@code ConfigurationScreen} looks up {@code <modid>.configuration.<name>}
 * for each option and section, using only the name at that level. If the lang file moves
 * or a key is renamed, the screen still opens but shows raw keys, which is easy to miss.
 */
class ConfigLocalizationTest {

    private static final String PREFIX = "harmonymod.configuration.";

    private static Map<String, String> loadLang() throws Exception {
        // Must resolve from the namespace path the client actually reads.
        InputStream in = ConfigLocalizationTest.class.getResourceAsStream("/assets/harmonymod/lang/en_us.json");
        assertNotNull(in, "lang file must live at assets/harmonymod/lang/en_us.json");
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = new Gson().fromJson(reader, Map.class);
            return map;
        }
    }

    @Test
    void everyConfigOptionAndSectionHasATranslation() throws Exception {
        Map<String, String> lang = loadLang();
        Set<String> names = new LinkedHashSet<>();

        for (UnmodifiableConfig.Entry section : HarmonyConfig.SPEC.getValues().entrySet()) {
            names.add(section.getKey());
            Object value = section.getRawValue();
            if (value instanceof UnmodifiableConfig leaves) {
                for (UnmodifiableConfig.Entry leaf : leaves.entrySet()) {
                    names.add(leaf.getKey());
                }
            }
        }

        for (String name : names) {
            String key = PREFIX + name;
            assertTrue(lang.containsKey(key),
                    () -> "missing translation key '" + key + "' in assets/harmonymod/lang/en_us.json");
        }
    }

    @Test
    void langKeysUseTheNeoForgeConvention() throws Exception {
        Map<String, String> lang = loadLang();
        for (String key : lang.keySet()) {
            if (key.startsWith("harmonymod.config.")) {
                throw new AssertionError("'" + key + "' uses the old harmonymod.config.* prefix; "
                        + "NeoForge resolves harmonymod.configuration.*");
            }
        }
        assertTrue(lang.containsKey(PREFIX + "title"), "config screen title must be translated");
    }
}
