package ugnopus.interpreter.models;

import java.util.Objects;

public class Bulynas {
    public Boolean value;

    public Bulynas(String value) {
        this.value = Objects.equals(value, "tru");
    }

    @Override
    public String toString() {
        if (value) {
            return "Tru";
        } else {
            return "Fols";
        }
    }

    public static String parseBoolean(String value)
    {
        if(Objects.equals(value, "Tru") || Objects.equals(value, "Fols"))
            return value;
        throw new IllegalArgumentException();
    }
}