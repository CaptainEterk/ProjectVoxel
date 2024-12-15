package pv.settings;

public record Setting(String key, String value) {
    @Override
    public String toString() {
        return value;
    }
}