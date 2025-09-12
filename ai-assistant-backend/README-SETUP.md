# AI Assistant Backend Setup

## Environment Configuration

This application requires sensitive configuration that should not be committed to version control.

### Setup Instructions

1. **Copy the example configuration:**
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   ```

2. **Update the configuration with your actual values:**

   Edit `src/main/resources/application.properties` and replace the placeholder values:

   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://your-supabase-url:5432/postgres
   spring.datasource.username=your-supabase-username
   spring.datasource.password=YOUR_ACTUAL_SUPABASE_PASSWORD

   # API Keys
   replicate.api.key=YOUR_ACTUAL_REPLICATE_API_KEY
   gemini.api.key=YOUR_ACTUAL_GEMINI_API_KEY

   # SSLCommerz Configuration
   sslcommerz.store.id=YOUR_ACTUAL_SSLCOMMERZ_STORE_ID
   sslcommerz.store.password=YOUR_ACTUAL_SSLCOMMERZ_PASSWORD
   ```

### Required API Keys

- **Replicate API Key**: Get from [Replicate.com](https://replicate.com/account/api-tokens)
- **Gemini API Key**: Get from [Google AI Studio](https://makersuite.google.com/app/apikey)
- **Supabase Database**: Get connection details from your Supabase project
- **SSLCommerz**: Get credentials from your SSLCommerz account

### Security Notes

- Never commit `application.properties` to version control
- Use environment variables in production
- The `application-example.properties` serves as a template with placeholder values

### Running the Application

```bash
mvn spring-boot:run
```

Make sure you have configured your `application.properties` file before running.
