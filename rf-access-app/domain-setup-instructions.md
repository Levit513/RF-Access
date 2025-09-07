# Custom Subdomain Setup for programmer.513solutions.com

## Railway Custom Domain Configuration

### Step 1: Add Custom Domain in Railway
1. Go to your Railway project dashboard
2. Navigate to **Settings** → **Domains**
3. Click **Add Domain**
4. Enter: `programmer.513solutions.com`
5. Railway will provide you with DNS records to configure

### Step 2: DNS Configuration
Add this DNS record to your 513solutions.com domain:

**CNAME Record (recommended):**
```
Type: CNAME
Name: programmer
Value: [your-railway-app].up.railway.app
TTL: 300
```

**Alternative - A Record:**
```
Type: A
Name: programmer
Value: [Railway-provided-IP]
TTL: 300
```

### Step 3: Subdomain Configuration
The app is configured to work directly at the subdomain:
- Main URL: `https://programmer.513solutions.com`
- Admin login: `https://programmer.513solutions.com/login`
- Dashboard: `https://programmer.513solutions.com/admin`

### Step 4: SSL Certificate
Railway will automatically provision SSL certificates for your custom subdomain.

### Step 5: Environment Variables
Set in Railway dashboard:
```
FLASK_HOST=0.0.0.0
PORT=8080
SECRET_KEY=[generate-secure-key]
```

### Verification
After DNS propagation (5-60 minutes):
- Test: `https://programmer.513solutions.com`
- Admin login: `admin` / `admin123`

### Troubleshooting
- Check DNS propagation: `nslookup programmer.513solutions.com`
- Verify Railway domain mapping in dashboard
- Check Railway logs for any routing issues

### Benefits of Subdomain Approach
- Cleaner URLs (no path prefix)
- Simpler configuration
- Better SEO and bookmarking
- Independent SSL certificate
