# JanuMart — Accessories for Human.

A hands-on **Java Full-Stack Capstone** project: a multi-seller accessories marketplace
(Watches, Bags, Jewellery, Sunglasses, Travel & more) built with **Java 17 + Servlets + JSP +
JSTL + JDBC + H2 + HikariCP**. No Spring, no Hibernate, no JPA, no React — strictly the classic
Java web stack, wired end-to-end so it runs locally with one command.

---

## ✨ Features

- **Homepage** – hero ("Accessories for Human."), category grid, trending / best-sellers /
  new arrivals / curated category rows / seller strip.
- **Shop** – live search, 15 categories, filters (subcategory, brand, colour, size, gender,
  price range, in-stock, minimum rating), sorting, pagination — all via the JSON API.
- **Cart** – add / update quantity / remove, cart badge in the navbar.
- **Checkout** – address + mock payment (**UPI / Card / COD**), **transactional** order creation
  (validate stock → create order → reduce stock → clear cart → commit / rollback).
- **Orders** – history, order-detail with a visual **tracking stepper**, buyer cancellation
  (PENDING / CONFIRMED only).
- **Roles & dashboards**
  - **BUYER** – orders, total spent, cart count, recently viewed.
  - **SELLER** – catalogue CRUD, low-stock alerts, top sellers, revenue, fulfil incoming orders
    (CONFIRMED → SHIPPED → DELIVERED).
  - **ADMIN** – platform stats, manage users / products / orders, status transitions.
- **Reviews** – star ratings ★ 1–5; only buyers with a **DELIVERED** order containing the product
  may review.
- **Profile** – update name, change password (BCrypt only, hashes never serialized).
- **🤖 Janu AI Shopping Assistant** – floating chat on every customer-facing page. Understands
  product/category/budget/gift questions, recommends **real catalogue products** (price, stock,
  link), and explains cart / checkout / orders / delivery. Optional **live AI** via any
  OpenAI-compatible provider (`AI_PROVIDER_API_KEY`); without a key it stays honest in
  "catalog" mode and never pretends to be AI.
- **Security** – BCrypt passwords, session-id regeneration on login, `AuthFilter` +
  `RoleAuthorizationFilter`, server-side input validation, HTML output escaping,
  PreparedStatements only, filtered + whitelisted SQL.

---

## 🧱 Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| Web | Java Servlets (front controller `/api/v1/*` + `ViewController`) |
| Views | JSP + JSTL (c / fmt / fn), CSS (vanilla, lavender ♀ pink theme), JS (fetch, no frameworks) |
| Data | JDBC + H2 (file DB), HikariCP connection pool |
| JSON | Gson (`{ success, message, data }` envelope) |
| Security | jBCrypt, session filters |
| Logging | SLF4J + Logback |
| Build / Run | Maven + Cargo (downloads Apache Tomcat 9.0.85 automatically) |

---

## 📁 Project Layout

```
src/main/java/com/janumart/
├── controller/   ApiServlet (front controller) + Auth/Product/Cart/Order/Review/
│                 Admin/Seller/Chatbot controllers + ViewController + HomeServlet
├── service/      Auth, Product, Cart, Order (transactional checkout), Review, Dashboard, Admin
│                 chatbot/ (intent parsing, rate limiter, ChatbotService)
│                 ai/ (AiProvider + OpenAI-compatible client — key via env only)
├── dao/          User, Product, Cart, Order, Review (+ transaction-scoped variants)
├── model/        User, Product, CartItem, Order, OrderItem, Review, Category (15 + SVG icons)
├── dto/          ApiResponse + request DTOs + ProductQuery + chatbot DTOs
├── filter/       EncodingFilter, AuthFilter, RoleAuthorizationFilter
├── listener/     AppContextListener (pool + schema/seed bootstrap)
├── util/         AppConfig, DBUtil, JsonUtil, PasswordUtil, Validate, SessionUtil
└── exception/    AppException + NotFound/Forbidden/Unauthorized/Validation

src/main/resources/
├── schema.sql              idempotent DDL (PK/FK/unique/check, DECIMAL(10,2), timestamps)
├── seed.sql                11 users · 122 products · 8 orders · 43 order_items · 30 reviews
├── application.properties  H2 URL, Hikari pool, app name/tagline
└── logback.xml

src/main/webapp/
├── WEB-INF/views/          JSP views + includes/header, includes/footer, fragments/product-card
├── css/style.css           complete stylesheet (design tokens, responsive)
├── css/chatbot.css         Janu AI chat widget styles (reuses the same design tokens)
├── js/                     api.js, header.js, main.js, auth.js, shop.js, product.js,
│                           cart.js, checkout.js, orders.js, seller.js, admin.js, profile.js,
│                           chatbot.js (floating chat widget)
└── images/                 favicon.svg + fallback.svg
```

---

## 🚀 Run It Locally

Requirements: **JDK 17** and **Maven 3.8+** (no Tomcat needed — Cargo downloads it).

```bash
mvn clean package        # compile + unit tests + build janumart.war
mvn cargo:run            # downloads Tomcat 9.0.85 into target/, starts http://localhost:8080
```

Then open **http://localhost:8080/janumart/** in your browser.

- First boot creates the H2 file database `data/janumart.mv.db` (under
  `target/cargo/configurations/tomcat9x/data/` when running via `mvn cargo:run`)
  and seeds demo data (only when empty, so restarts keep your changes).
- Stop the server with `Ctrl+C` in the terminal.

### Fresh start

Delete the `data/` folder (and optionally `target/`) to re-seed the database from scratch.

---

## 🔑 Demo Credentials

| Role | Email | Password |
| --- | --- | --- |
| Buyer | `demo@janumart.com` | `Buyer@123` |
| Seller | `stylehub@janumart.com` | `Seller@123` |
| Admin | `admin@janumart.com` | `Admin@123` |

Other seller accounts (all `Seller@123`): `trendytouch@`, `urbanacc@`, `fashionnest@`,
`dailyessentials@`, `elegantchoice@`, `travelmate@`, `giftcorner@` — all `@janumart.com`.

You can also **register new accounts** on the public page as a Buyer or Seller
(Admin accounts are never registerable).

### Try the demo flow

1. **Buyer** login → add any in-stock product to the cart → cart → checkout (choose COD) →
   order placed at status **CONFIRMED**.
2. **Seller** login (e.g. `stylehub@`) → *Orders I Received* → advance: **Shipped → Delivered**.
3. **Buyer** → open the DELIVERED order's product → leave a ★★★★★ review.
4. **Admin** login → *Manage Users / Products / Orders*, try filtering and status changes.
5. **Seller** → *Add Product*, edit, delete — then refresh the Shop to see it live.

---

## 🔌 Main API Endpoints (JSON)

| Method & Path | Description |
| --- | --- |
| `POST /api/v1/auth/login` · `POST /api/v1/auth/register` · `POST /api/v1/auth/logout` · `GET /api/v1/auth/me` | Authentication |
| `PUT /api/v1/profile` · `PUT /api/v1/profile/password` | Profile settings |
| `GET /api/v1/products?q=&category=&sort=&minPrice=&page=…` · `GET /api/v1/meta/filters` | Catalogue browsing |
| `GET/POST/PUT/DELETE /api/v1/products[/{id}]` | Product CRUD (write = SELLER/ADMIN) |
| `GET/POST /api/v1/cart` · `PUT/DELETE /api/v1/cart/{productId}` | Cart (BUYER) |
| `POST /api/v1/orders` · `GET /api/v1/orders[/{id}]` · `PUT /api/v1/orders/{id}/status` | Orders |
| `GET /api/v1/products/{id}/reviews` · `POST /api/v1/reviews` | Reviews |
| `GET /api/v1/admin/stats` · `GET/DELETE /api/v1/admin/{users,products}[/{id}]` · `GET /api/v1/admin/orders` | Admin |
| `POST /api/v1/chatbot/message` · `GET /api/v1/chatbot` | Janu AI Shopping Assistant |

Every response uses the envelope `{ "success": true|false, "message": "...", "data": ... }`.

---

## 🤖 Janu AI Shopping Assistant

A floating chat widget (bottom-right) on all customer-facing pages brings the assistant to
your store. It parses what you ask (category, budget, gender, gifts, occasion), queries the
**real** product catalog, and returns `{ reply, products, mode }` where `products` are actual
catalog entries (with real prices, stock and product-page links) — never fabricated.

### Two honest modes

| Mode | When | Reply source |
| --- | --- | --- |
| `ai` | `AI_PROVIDER_API_KEY` is set | Real AI provider (OpenAI-compatible chat completions) with the live catalog as context |
| `catalog` | No key configured, provider down, or a help/greeting question | Deterministic reply built locally from live catalog data — clearly not AI |

### Configure live AI

The API key is loaded **server-side only** and never ships to the browser:

```bash
set AI_PROVIDER_API_KEY=sk-...        # Windows cmd
export AI_PROVIDER_API_KEY=sk-...     # Linux/macOS
mvn cargo:run
```

Optional overrides (also in `.env.example`):

| Env var | Default |
| --- | --- |
| `AI_ENABLED` | `false` |
| `AI_BASE_URL` | `https://api.openai.com/v1` (any OpenAI-compatible endpoint works) |
| `AI_MODEL` | `gpt-4o-mini` |
| `AI_TIMEOUT_SECONDS` | `20` |

Without a key the assistant still works honestly in **catalog** mode using live JanuMart
data — it never claims replies came from AI. Per-session rate limiting (30 msgs/min) guards
the provider, messages are capped at 500 chars, history is capped and sanitized server-side,
and provider errors degrade gracefully to catalog replies.

---

## ✅ Unit Tests

```bash
mvn test
```

`com.janumart.ValidateTest` and `com.janumart.PasswordUtilTest` cover input validation rules
and BCrypt hashing/checking. `com.janumart.service.chatbot.*` covers intent parsing, rate
limiting and message/history sanitization.

---

## 🧪 Grading Checklist Map

- Layered MVC: Browser → JSP/CSS/JS → Servlet front controller → controllers → services →
  DAOs → JDBC → H2 ✔
- No Spring / Hibernate / JPA / React / Node ✔
- Roles BUYER / SELLER / ADMIN with role-guarded pages + APIs ✔
- BCrypt-only password storage; no plain text anywhere ✔
- 15 categories, 122 seeded products (INR), 8 sellers, orders with snapshot `unit_price` ✔
- Public registration limited to BUYER/SELLER (no admin signup) ✔
- Reviews restricted to DELIVERED-order buyers ✔
- Transactional checkout with rollback ✔
- Session fixation protection (ID regenerated at login), httpOnly cookies, 30-min timeout ✔
- SQL injection-safe (PreparedStatements + whitelisted sorts + validated filters) ✔
- XSS-safe (output escaping in JSPs + JS `esc()`) ✔
- AI chatbot: product-aware, honest catalog fallback, server-side key only, rate-limited ✔

---

## 🛠 VS Code Setup

1. Install **Extension Pack for Java** (Microsoft).
2. Open the project folder — Maven imports automatically (confirm when prompted).
3. Run **explorer context menu → Run Java** or use the Maven panel: `cargo:run`.

Suggested settings (`.vscode/settings.json`):

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "maven.terminal.customEnv": [{ "environmentVariable": "JAVA_HOME", "value": "<your JDK 17 path>" }]
}
```