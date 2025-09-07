#!/usr/bin/env python3
"""
Fix Login Issue - Force Create Working Admin User
"""

from app import app, db, User
from werkzeug.security import generate_password_hash, check_password_hash

def fix_admin_login():
    with app.app_context():
        # Delete ALL existing users
        User.query.delete()
        db.session.commit()
        print("Cleared all existing users")
        
        # Create fresh admin user
        password = 'admin123'
        password_hash = generate_password_hash(password)
        
        admin = User(
            username='admin',
            email='admin@example.com',
            password_hash=password_hash,
            is_admin=True
        )
        
        db.session.add(admin)
        db.session.commit()
        
        # Verify it works
        test_user = User.query.filter_by(username='admin').first()
        if test_user and check_password_hash(test_user.password_hash, password):
            print("✅ SUCCESS! Admin login fixed")
            print("Username: admin")
            print("Password: admin123")
        else:
            print("❌ Still not working")

if __name__ == '__main__':
    fix_admin_login()
