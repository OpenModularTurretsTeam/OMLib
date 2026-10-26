package omtteam.omlib.config;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

/**
 * Clamps every {@link Range}-annotated field of a config object, including those of nested config objects. Keys
 * in log messages are the dotted JSON paths.
 */
public final class ConfigRanges {
    private ConfigRanges() {
    }

    /**
     * Clamps the values of a config object into their {@link Range}s. The sections of the config are the classes in the
     * package of {@code target}'s class.
     *
     * @param path prefix of the keys of {@code target}, empty or ending with a dot
     */
    public static void validate(Object target, ConfigValidator v, String path) {
        validate(target, v, path, target.getClass().getPackageName());
    }

    private static void validate(Object target, ConfigValidator v, String path, String sectionPackage) {
        for (Field field : fields(target.getClass())) {
            try {
                String name = path + keyOf(field);
                Range range = field.getAnnotation(Range.class);
                if (range != null && field.getType() == int.class) {
                    field.setInt(target, v.range(name, field.getInt(target), (int) range.min(), (int) range.max()));
                } else if (range != null && field.getType() == double.class) {
                    field.setDouble(target, v.range(name, field.getDouble(target), range.min(), range.max()));
                } else if (isSection(field.getType(), sectionPackage)) {
                    validate(field.get(target), v, name + ".", sectionPackage);
                } else if (isSectionMap(field, sectionPackage)) {
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) field.get(target)).entrySet()) {
                        validate(entry.getValue(), v, name + "." + entry.getKey() + ".", sectionPackage);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** The persisted fields of a config class, in declaration order. */
    static Field[] fields(Class<?> type) {
        return java.util.Arrays.stream(type.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()))
                .peek(f -> f.setAccessible(true))
                .toArray(Field[]::new);
    }

    /** The JSON key of a field. */
    static String keyOf(Field field) {
        SerializedName serialized = field.getAnnotation(SerializedName.class);
        return serialized != null ? serialized.value() : field.getName();
    }

    /** Whether a field holds a nested group of settings (a class of the config's package), as opposed to a value. */
    static boolean isSection(Class<?> type, String sectionPackage) {
        return type.getPackageName().equals(sectionPackage) && !type.isEnum() && !type.isInterface();
    }

    /** Whether a field is a {@code Map<String, section>}, e.g. settings per turret id. */
    static boolean isSectionMap(Field field, String sectionPackage) {
        return Map.class.isAssignableFrom(field.getType())
                && field.getGenericType() instanceof ParameterizedType map
                && map.getActualTypeArguments()[1] instanceof Class<?> value && isSection(value, sectionPackage);
    }
}
