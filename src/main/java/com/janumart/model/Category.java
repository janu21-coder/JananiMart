package com.janumart.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The 15 product categories of JanuMart with a compact inline SVG icon used
 * in the UI (professional icons, not just emoji).
 */
public enum Category {

    WATCHES("Watches", "clock",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><circle cx=\"12\" cy=\"13\" r=\"8\"/><path d=\"M12 9v4l2 2\"/><path d=\"M9 2h6l1 3H8l1-3z\"/></svg>"),
    BAGS("Bags", "handbag",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M4 8h16l-1.5 12h-13L4 8z\"/><path d=\"M8 8V6a4 4 0 0 1 8 0v2\"/></svg>"),
    WALLETS("Wallets", "wallet",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><rect x=\"3\" y=\"6\" width=\"18\" height=\"13\" rx=\"2\"/><path d=\"M3 10h18\"/><path d=\"M16 14h2\"/></svg>"),
    BELTS("Belts", "belt",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M4 12h16\"/><rect x=\"16\" y=\"9\" width=\"5\" height=\"6\" rx=\"1.5\"/><path d=\"M4 9v6\"/></svg>"),
    SUNGLASSES("Sunglasses", "sunglasses",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M3 11l1.5-4c.3-.9 1.1-1.5 2-1.5h3.2c.6 0 1.2.4 1.4 1l.9 3\"/><path d=\"M21 11l-1.5-4c-.3-.9-1.1-1.5-2-1.5h-3.2c-.6 0-1.2.4-1.4 1l-.9 3\"/><path d=\"M3 11h5.5l1 3a3 3 0 0 1-5.5 0l-1-3zm0 0 0 1.5\"/><path d=\"M21 11h-5.5l-1 3a3 3 0 0 0 5.5 0l1-3zm0 0 0 1.5\"/></svg>"),
    JEWELLERY("Jewellery", "ring",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><circle cx=\"12\" cy=\"15\" r=\"5\"/><path d=\"M9 15l3-8 3 8\"/><path d=\"M12 7V4m-2 0h4\"/></svg>"),
    HAIR_ACCESSORIES("Hair Accessories", "hairAccessory",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M9 18c-2.5 0-4.5-2-4.5-4.5S7.5 4 12 4s7.5 6.5 7.5 9.5S13.5 18 12 18\"/><path d=\"M9 18c0 2 1.5 3 3 3s3-1 3-3\"/></svg>"),
    FASHION_ACCESSORIES("Fashion Accessories", "cap",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M4 10a8 8 0 0 1 16 0\"/><path d=\"M3 10h18l-1.5 5h-15L3 10z\"/><path d=\"M9 15v4a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2v-4\"/></svg>"),
    TRAVEL_ACCESSORIES("Travel Accessories", "luggage",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><rect x=\"4\" y=\"7\" width=\"16\" height=\"13\" rx=\"2\"/><path d=\"M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2\"/><path d=\"M2 20h20M9 13h6v4H9z\"/></svg>"),
    MOBILE_ACCESSORIES("Mobile Accessories", "mobile",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><rect x=\"7\" y=\"2\" width=\"10\" height=\"20\" rx=\"2\"/><path d=\"M11 18h2\"/></svg>"),
    KEYCHAINS("Keychains", "key",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><circle cx=\"7.5\" cy=\"16.5\" r=\"4\"/><path d=\"M10.5 13.5L20 4m-3 3l2.5 2.5M14 7.5l2.5 2.5\"/></svg>"),
    GIFTS("Gifts", "gift",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><rect x=\"3\" y=\"8\" width=\"18\" height=\"4\"/><rect x=\"5\" y=\"12\" width=\"14\" height=\"9\"/><path d=\"M12 8v13\"/><path d=\"M12 8c-2 0-4-1-4-3s1.5-3 3-2c1.4.9 1 4 1 5zm0 0c2 0 4-1 4-3s-1.5-3-3-2c-1.4.9-1 4-1 5z\"/></svg>"),
    OFFICE_ACCESSORIES("Office Accessories", "office",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><rect x=\"3\" y=\"4\" width=\"18\" height=\"16\" rx=\"2\"/><path d=\"M3 9h18\"/><path d=\"M7 13h4m-4 3h4\"/></svg>"),
    PERSONAL_ACCESSORIES("Personal Accessories", "personal",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><path d=\"M4 8h16v12H4V8z\"/><path d=\"M8 8V6a4 4 0 0 1 8 0v2\"/><path d=\"M12 13v4m-2-2 4 0\"/></svg>"),
    SEASONAL_ACCESSORIES("Seasonal Accessories", "seasonal",
            "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\" stroke-linecap=\"round\"><circle cx=\"12\" cy=\"12\" r=\"4\"/><path d=\"M12 3v3m0 12v3M3 12h3m12 0h3M5.6 5.6l2.1 2.1m8.6 8.6l2.1 2.1m0-12.8l-2.1 2.1M7.7 16.3l-2.1 2.1\"/></svg>");

    private final String name;
    private final String iconKey;
    private final String svg;

    Category(String name, String iconKey, String svg) {
        this.name = name;
        this.iconKey = iconKey;
        this.svg = svg;
    }

    public String getName() {
        return name;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getSvg() {
        return svg;
    }

    public static List<Category> all() {
        return List.of(values());
    }

    /** Accesses by exact display name (product.category stores the display name). */
    public static Category fromName(String name) {
        if (name == null) {
            return null;
        }
        for (Category c : values()) {
            if (c.name.equalsIgnoreCase(name.trim())) {
                return c;
            }
        }
        return null;
    }
}