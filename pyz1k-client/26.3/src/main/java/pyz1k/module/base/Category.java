package pyz1k.module.base;


public enum Category {
    VISUALS("Visuals"),
    ITEM_RENDER("ItemRender"),
    HAND_RENDER("HandRender");

    final String name;

    private Category(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

