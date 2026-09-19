# AV AI — Central API + Windows Electron

این نسخه فقط برای جریان ساده‌ی موردنظر ساخته شده است:

1. کاربر داخل برنامه ویندوز ثبت‌نام می‌کند.
2. برای ورود، نام کاربری و رمز را می‌زند.
3. API مرکزی یک کد ۶ رقمی تولید می‌کند و Electron آن را در پیام ویندوز نشان می‌دهد.
4. کاربر کد را وارد می‌کند و وارد برنامه می‌شود.
5. کاربر فقط سؤالش را می‌پرسد و پاسخ از OpenRouter برمی‌گردد.

کاربر لازم نیست وارد سایت OpenRouter شود و کلید OpenRouter فقط روی API مرکزی قرار می‌گیرد.

## Central API
- Java 21
- Spring Boot 4.1.1
- PostgreSQL برای محیط ابری (با تنظیم DATABASE_URL) یا H2 برای تست محلی
- OpenRouter API

OpenRouter از endpoint `https://openrouter.ai/api/v1/chat/completions` استفاده می‌کند.

### متغیرهای سرور
```text
OPENROUTER_API_KEY=...
OPENROUTER_MODEL=openrouter/free
DATABASE_URL=jdbc:postgresql://...
DB_DRIVER=org.postgresql.Driver
DB_USER=...
DB_PASSWORD=...
```

## Electron Windows
داخل `electron-app/config.json` فقط آدرس API مرکزی را وارد کن:
```json
{
  "apiUrl": "https://YOUR-RENDER-API.onrender.com"
}
```

سپس:
```bash
npm install
npm start
```

برای ساخت Setup ویندوز:
```bash
npm run dist
```

## Render
فایل `render.yaml` و `central-api/Dockerfile` برای استقرار API روی Render آماده شده‌اند.
برای دیتای حساب‌ها در محیط ابری، PostgreSQL را به پروژه وصل کن و `DATABASE_URL` و متغیرهای DB را در Environment قرار بده.

### نکته امنیتی کد ورود
در این نسخه چون کاربر پیام کد را روی همان Windows app می‌خواهد، کد برای همان برنامه برگردانده و در یک پنجره ویندوز نمایش داده می‌شود. این «کد تأیید دستگاه» است و به‌تنهایی معادل 2FA با ایمیل/SMS نیست.
