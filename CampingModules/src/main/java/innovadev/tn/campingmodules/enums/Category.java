package innovadev.tn.campingmodules.enums;

public enum Category {
    MOUNTAIN,
    DESERT,
    FOREST,
    BEACH,
    SURVIVAL;
    public static Category fromString(String category) {
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Catégorie invalide : " + category);
        }
    }

}
