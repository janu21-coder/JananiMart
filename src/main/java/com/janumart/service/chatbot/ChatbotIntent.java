package com.janumart.service.chatbot;

import com.janumart.model.Category;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts a shopping intent + preference hints from a customer message so the
 * assistant can query the real JanuMart catalog. Pure static logic, no I/O —
 * unit tested without a database.
 */
public final class ChatbotIntent {

    /** What the customer is asking about. */
    public enum Kind {
        GREETING, HELP, CATALOG, OFFERS, CART, CHECKOUT, ORDERS, DELIVERY, CATEGORIES, UNRELATED
    }

    private static final BigDecimal AFFORDABLE_MAX = BigDecimal.valueOf(1500);

    private Kind kind = Kind.CATALOG;
    private String category;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private String gender;
    private String searchTerm;
    private final Set<String> flags = new LinkedHashSet<>();

    /** Aliases (lowercase) to canonical category display names, specificity first. */
    private static final Map<String, String> CATEGORY_ALIASES = buildCategoryAliases();

    private static final Pattern PRICE_WORD = Pattern.compile(
            "(?:under|below|less than|within|upto|up to|at most|max|maximum|budget of|budget)\\s+"
                    + "(?:rs\\.?|inr|₹)?\\s*([0-9][0-9,]*)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_CURRENCY = Pattern.compile(
            "(?:rs\\.?|inr|₹)\\s*([0-9][0-9,]*)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_RANGE = Pattern.compile(
            "(?:between|from)\\s+(?:rs\\.?|inr|₹)?\\s*([0-9][0-9,]*)\\s*(?:and|to|-)\\s+"
                    + "(?:rs\\.?|inr|₹)?\\s*([0-9][0-9,]*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern WOMEN = Pattern.compile(
            "\\b(?:women|woman|girl|girls|female|ladies|her|she|wife|mother|mom|mum|sister|daughter|"
                    + "girlfriend|bride|fiancee)\\b");
    private static final Pattern MEN = Pattern.compile(
            "\\b(?:men|man|guy|guys|male|gentleman|gentlemen|him|he|husband|father|dad|brother|son|"
                    + "boyfriend|groom|fiance)\\b");
    private static final Pattern UNISEX = Pattern.compile(
            "\\b(?:unisex|anyone|everyone|all genders|any gender)\\b");

    private static final Pattern GREETING = Pattern.compile(
            "^(?:hi|hii+|hello|hey|heya|hola|namaste|good\\s+(?:morning|afternoon|evening))\\b"
                    + "|\\bhow are you(?: today)?\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HELP = Pattern.compile(
            "\\b(?:what can you do|how do you (?:work|help)|help me|need help|help)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORIES = Pattern.compile(
            "\\b(?:what categories|list categories|available categories|which categories|categories available"
                    + "|how many categories)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern OFFERS = Pattern.compile(
            "\\b(?:offer|offers|discount|discounts|deal|deals|coupon|coupons|promo|promo code|sale|"
                    + "affordable)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CART = Pattern.compile(
            "\\b(?:cart|add to cart|quantity|remove item|cart total|empty my cart)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CHECKOUT = Pattern.compile(
            "\\b(?:checkout|place order|place an order|how to buy|make a purchase|pay|payment)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDERS = Pattern.compile(
            "\\b(?:order status|track|my orders|order history|previous orders|where is my order|"
                    + "cancel my order|cancel order|order detail)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DELIVERY = Pattern.compile(
            "\\b(?:delivery|deliver|shipping|dispatch|dispatched|courier|when will (?:i|it) (?:get|arrive)"
                    + "|how long.*take|arrive)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern AFFORDABLE = Pattern.compile(
            "\\b(?:affordable|cheap|budget|economical|inexpensive|low price)\\b");
    private static final Pattern UNRELATED = Pattern.compile(
            "\\b(?:recipe|receipe|cook|weather|news|sports|match|cricket|football|math|homework|"
                    + "translate|poem|song lyrics|movie|film|joke|jokes|who is|president|prime minister|"
                    + "capital of|write (?:me )?(?:a|an|some)|code|programming|stock market|bitcoin)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "your", "you", "are", "can", "could",
            "please", "show", "find", "give", "want", "need", "some", "under", "below", "less", "than",
            "very", "really", "good", "best", "nice", "what", "which", "accessories", "accessory",
            "recommend", "suggest", "suggestion", "recommendations", "looking", "spend", "budget",
            "price", "prices", "store", "mart", "janumart", "product", "products", "item", "items",
            "help", "there", "their", "about", "around", "within", "between", "i", "me", "my", "myself",
            "be", "to", "of", "in", "on", "at", "it", "as", "is", "was", "has", "have", "had", "do",
            "does", "did", "will", "would", "should", "cant", "canot", "dont", "not", "no", "any",
            "all", "also", "just", "like", "use", "used", "lot", "many", "much", "more", "most");

    private ChatbotIntent() {
    }

    /** Parse a free-text customer message into a structured intent. */
    public static ChatbotIntent parse(String message) {
        if (message == null) {
            message = "";
        }
        String lower = message.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();

        ChatbotIntent intent = new ChatbotIntent();
        parsePrice(intent, lower);
        parseGender(intent, lower);
        parseCategory(intent, lower);
        parseFlags(intent, lower);
        intent.searchTerm = parseSearchTerm(lower);
        classify(intent, lower);
        return intent;
    }

    /* ---------------- detection helpers ---------------- */

    private static void parsePrice(ChatbotIntent intent, String lower) {
        Matcher range = PRICE_RANGE.matcher(lower);
        if (range.find()) {
            BigDecimal min = parseAmount(range.group(1));
            BigDecimal max = parseAmount(range.group(2));
            if (min != null && max != null && min.compareTo(max) <= 0) {
                intent.minPrice = min;
                intent.maxPrice = max;
                return;
            }
        }
        BigDecimal found = null;
        Matcher word = PRICE_WORD.matcher(lower);
        while (word.find()) {
            BigDecimal v = parseAmount(word.group(1));
            if (v != null && (found == null || v.compareTo(found) < 0)) {
                found = v;
            }
        }
        if (found == null) {
            Matcher cur = PRICE_CURRENCY.matcher(lower);
            while (cur.find()) {
                BigDecimal v = parseAmount(cur.group(1));
                if (v != null && (found == null || v.compareTo(found) < 0)) {
                    found = v;
                }
            }
        }
        intent.maxPrice = found;
    }

    private static void parseGender(ChatbotIntent intent, String lower) {
        if (UNISEX.matcher(lower).find()) {
            intent.gender = "Unisex";
            return;
        }
        if (WOMEN.matcher(lower).find()) {
            intent.gender = "Women";
            return;
        }
        if (MEN.matcher(lower).find()) {
            intent.gender = "Men";
        }
    }

    private static void parseCategory(ChatbotIntent intent, String lower) {
        for (Map.Entry<String, String> e : CATEGORY_ALIASES.entrySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(e.getKey().trim()))
                    .matcher(lower).find()) {
                intent.category = e.getValue();
                return;
            }
        }
    }

    private static void parseFlags(ChatbotIntent intent, String lower) {
        if (Pattern.compile("\\b(?:birthday|anniversary|gift|gifts|present)\\b").matcher(lower).find()
                || lower.contains("gift")) {
            intent.flags.add("GIFT");
        }
        if (Pattern.compile("\\b(?:party|wedding|festival|festive|celebration|night out|clubbing)\\b")
                .matcher(lower).find()) {
            intent.flags.add("PARTY");
        }
        if (Pattern.compile("\\b(?:daily|everyday|casual|regular use)\\b").matcher(lower).find()) {
            intent.flags.add("DAILY");
        }
        if (Pattern.compile("\\b(?:college|student|school|exam|placement)\\b").matcher(lower).find()) {
            intent.flags.add("COLLEGE");
        }
        if (Pattern.compile("\\b(?:travel|trip|vacation|holiday trip|weekend trip)\\b").matcher(lower).find()) {
            intent.flags.add("TRAVEL");
        }
        if (AFFORDABLE.matcher(lower).find()) {
            intent.flags.add("AFFORDABLE");
            if (intent.maxPrice == null) {
                intent.maxPrice = AFFORDABLE_MAX;
            }
        }
    }

    private static String parseSearchTerm(String lower) {
        StringBuilder sb = new StringBuilder();
        String[] words = lower.split("[^a-z0-9]+");
        int kept = 0;
        for (String w : words) {
            if (w.length() < 3 || STOP_WORDS.contains(w)) {
                continue;
            }
            if (kept >= 4) {
                break;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(w);
            kept++;
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static void classify(ChatbotIntent intent, String lower) {
        if (GREETING.matcher(lower).find()) {
            intent.kind = Kind.GREETING;
            return;
        }
        if (HELP.matcher(lower).find()) {
            intent.kind = Kind.HELP;
            return;
        }
        if (CATEGORIES.matcher(lower).find()) {
            intent.kind = Kind.CATEGORIES;
            return;
        }
        if (ORDERS.matcher(lower).find()) {
            intent.kind = Kind.ORDERS;
            return;
        }
        if (CHECKOUT.matcher(lower).find()) {
            intent.kind = Kind.CHECKOUT;
            return;
        }
        if (CART.matcher(lower).find()) {
            intent.kind = Kind.CART;
            return;
        }
        if (DELIVERY.matcher(lower).find()) {
            intent.kind = Kind.DELIVERY;
            return;
        }
        if (OFFERS.matcher(lower).find() && intent.category == null) {
            intent.kind = Kind.OFFERS;
            return;
        }
        boolean hasShoppingHint = intent.category != null || intent.gender != null || intent.maxPrice != null
                || !intent.flags.isEmpty() || lower.contains("shop");
        if (!hasShoppingHint && UNRELATED.matcher(lower).find()) {
            intent.kind = Kind.UNRELATED;
            return;
        }
        intent.kind = Kind.CATALOG;
    }

    private static BigDecimal parseAmount(String s) {
        try {
            BigDecimal v = new BigDecimal(s.replace(",", "").trim());
            if (v.compareTo(BigDecimal.ZERO) <= 0 || v.compareTo(new BigDecimal("1000000")) > 0) {
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, String> buildCategoryAliases() {
        Map<String, String> map = new LinkedHashMap<>();
        put(map, "phone case", "Mobile Accessories");
        put(map, "phone cover", "Mobile Accessories");
        put(map, "power bank", "Mobile Accessories");
        put(map, "earbuds", "Mobile Accessories");
        put(map, "headphone", "Mobile Accessories");
        put(map, "charger", "Mobile Accessories");
        put(map, "smartphone", "Mobile Accessories");
        put(map, "mobile", "Mobile Accessories");
        put(map, "phone", "Mobile Accessories");
        put(map, "hair clip", "Hair Accessories");
        put(map, "hair tie", "Hair Accessories");
        put(map, "hairband", "Hair Accessories");
        put(map, "headband", "Hair Accessories");
        put(map, "scrunch", "Hair Accessories");
        put(map, "hair", "Hair Accessories");
        put(map, "wristwatch", "Watches");
        put(map, "timepiece", "Watches");
        put(map, "watch", "Watches");
        put(map, "sunglasses", "Sunglasses");
        put(map, "sunglass", "Sunglasses");
        put(map, "shades", "Sunglasses");
        put(map, "eyewear", "Sunglasses");
        put(map, "jewellery", "Jewellery");
        put(map, "jewelry", "Jewellery");
        put(map, "necklace", "Jewellery");
        put(map, "earring", "Jewellery");
        put(map, "bracelet", "Jewellery");
        put(map, "bangle", "Jewellery");
        put(map, "pendant", "Jewellery");
        put(map, "ring", "Jewellery");
        put(map, "keychain", "Keychains");
        put(map, "key chain", "Keychains");
        put(map, "keyring", "Keychains");
        put(map, "lanyard", "Keychains");
        put(map, "wallet", "Wallets");
        put(map, "card holder", "Wallets");
        put(map, "cardholder", "Wallets");
        put(map, "billfold", "Wallets");
        put(map, "belt", "Belts");
        put(map, "handbag", "Bags");
        put(map, "backpack", "Bags");
        put(map, "tote", "Bags");
        put(map, "sling bag", "Bags");
        put(map, "purse", "Bags");
        put(map, "bags", "Bags");
        put(map, "luggage", "Travel Accessories");
        put(map, "suitcase", "Travel Accessories");
        put(map, "duffel", "Travel Accessories");
        put(map, "passport", "Travel Accessories");
        put(map, "neck pillow", "Travel Accessories");
        put(map, "travel", "Travel Accessories");
        put(map, "stationery", "Office Accessories");
        put(map, "notebook", "Office Accessories");
        put(map, "office", "Office Accessories");
        put(map, "pen", "Office Accessories");
        put(map, "diary", "Office Accessories");
        put(map, "desk", "Office Accessories");
        put(map, "scarf", "Fashion Accessories");
        put(map, "beanie", "Fashion Accessories");
        put(map, "gloves", "Fashion Accessories");
        put(map, "hat", "Fashion Accessories");
        put(map, "cap", "Fashion Accessories");
        put(map, "fashion", "Fashion Accessories");
        put(map, "grooming", "Personal Accessories");
        put(map, "comb", "Personal Accessories");
        put(map, "razor", "Personal Accessories");
        put(map, "diwali", "Seasonal Accessories");
        put(map, "christmas", "Seasonal Accessories");
        put(map, "seasonal", "Seasonal Accessories");
        put(map, "gift", "Gifts");
        put(map, "gifts", "Gifts");
        put(map, "birthday", "Gifts");
        put(map, "anniversary", "Gifts");
        put(map, "present", "Gifts");
        return map;
    }

    private static void put(Map<String, String> map, String alias, String category) {
        map.putIfAbsent(alias, category);
    }

    /* ---------------- accessors ---------------- */

    public Kind getKind() {
        return kind;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public String getGender() {
        return gender;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean hasCategory() {
        return category != null && Category.fromName(category) != null;
    }
}