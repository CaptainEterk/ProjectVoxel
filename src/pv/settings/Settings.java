package settings;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Settings {
    private final List<Setting<?>> settings;

    public Settings() {
        settings = new ArrayList<>();
    }

    public void load() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream("settings.txt"));
        byte[] settingBytes = bufferedInputStream.readAllBytes();
        StringBuilder settingFileContents = new StringBuilder();
        for (byte b : settingBytes) {
            settingFileContents.append((char) b);
        }
        String[] settings = settingFileContents.toString().split("[\n\r]");
        for (String setting : settings) {
            if (setting.contains("=")) {
                String[] keyValue = setting.split("=");
                this.settings.add(new Setting<>(keyValue[0], keyValue[1]));
            }
        }
    }

    public Setting<?> getSetting(String settingName) {
        for (Setting<?> setting : settings) {
            if (Objects.equals(setting.key(), settingName)) {
                return setting;
            }
        }
        return null;
    }
}