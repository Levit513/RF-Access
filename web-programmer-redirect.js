/**
 * Mobile App Redirect Script for Web Programmer
 * Add this to your programmer.513solutions.com pages
 */

function redirectToApp() {
    // Get the current URL and extract the token
    const currentUrl = window.location.href;
    const urlParts = currentUrl.split('/program/');
    
    if (urlParts.length < 2) {
        console.log('No program token found in URL');
        return;
    }
    
    const token = urlParts[1];
    console.log('Extracted token:', token);
    
    // Detect if user is on mobile
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    
    if (isMobile) {
        // Try to open the app first
        const appUrl = `rfaccess://open?username=user_from_web&cardData=${token}&action=program`;
        const fallbackUrl = `https://play.google.com/store/apps/details?id=com.example.rfaccess`;
        
        console.log('Mobile detected, attempting app redirect:', appUrl);
        
        // Create invisible iframe to trigger app
        const iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = appUrl;
        document.body.appendChild(iframe);
        
        // Fallback to Play Store after 2 seconds if app doesn't open
        setTimeout(() => {
            console.log('App may not be installed, showing install option');
            if (confirm('RF Access app is required. Install from Play Store?')) {
                window.location.href = fallbackUrl;
            }
        }, 2000);
        
        // Also try intent:// scheme for Android
        setTimeout(() => {
            const intentUrl = `intent://open?username=user_from_web&cardData=${token}&action=program#Intent;scheme=rfaccess;package=com.example.rfaccess;S.browser_fallback_url=${encodeURIComponent(fallbackUrl)};end`;
            window.location.href = intentUrl;
        }, 500);
        
    } else {
        console.log('Desktop detected, showing mobile instructions');
        showMobileInstructions();
    }
}

function showMobileInstructions() {
    const instructions = document.createElement('div');
    instructions.innerHTML = `
        <div style="background: #f0f8ff; border: 2px solid #4CAF50; padding: 20px; margin: 20px; border-radius: 8px; text-align: center;">
            <h3>📱 Mobile Device Required</h3>
            <p>This programming link is designed for mobile devices with the RF Access app.</p>
            <p><strong>To program your card:</strong></p>
            <ol style="text-align: left; display: inline-block;">
                <li>Open this link on your Android device</li>
                <li>Install RF Access app if prompted</li>
                <li>Follow the card programming instructions</li>
            </ol>
            <p><strong>Link:</strong><br>
            <code style="background: #eee; padding: 5px; word-break: break-all;">${window.location.href}</code></p>
        </div>
    `;
    
    // Insert after the main content or at the top
    const mainContent = document.querySelector('main') || document.body;
    mainContent.insertBefore(instructions, mainContent.firstChild);
}

// Auto-run when page loads
document.addEventListener('DOMContentLoaded', redirectToApp);

// Also run immediately in case DOM is already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', redirectToApp);
} else {
    redirectToApp();
}
