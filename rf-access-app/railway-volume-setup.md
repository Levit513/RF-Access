# Railway Database Persistence Fix

## Problem
SQLite database is not persisting between Railway deployments/restarts, causing programs to disappear.

## Solution Options

### Option 1: Railway PostgreSQL (Recommended)
1. In Railway dashboard, go to your project
2. Click "New" → "Database" → "Add PostgreSQL"
3. Railway will create a PostgreSQL database and set DATABASE_URL automatically
4. Your app will automatically use PostgreSQL instead of SQLite

### Option 2: Railway Volume (Alternative)
1. In Railway dashboard, go to your project settings
2. Add a volume mount:
   - Mount Path: `/app/data`
   - Size: 1GB
3. Update DATABASE_URL environment variable:
   - `DATABASE_URL=sqlite:////app/data/mifare_system.db`

### Option 3: External Database
Use external PostgreSQL service like:
- Supabase (free tier available)
- ElephantSQL (free tier available)
- Heroku Postgres (free tier available)

## Current Configuration
The app now supports DATABASE_URL environment variable and will use PostgreSQL if available, falling back to SQLite for local development.

## Next Steps
1. Add PostgreSQL database in Railway dashboard
2. Railway will automatically set DATABASE_URL
3. Redeploy - database will persist between restarts
4. Test program creation and persistence
