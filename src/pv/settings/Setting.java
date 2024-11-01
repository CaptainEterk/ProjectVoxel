package settings;

public record Setting<T>(String key, T value) {
    @Override
    public String toString() {
        return value.toString();
    }
}