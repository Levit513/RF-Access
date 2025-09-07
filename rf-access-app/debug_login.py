#!/usr/bin/env python3
"""
Debug Login Issue
"""

from app import app, db, User
from werkzeug.security import generate_password_hash, check_password_hash

def debug_login():
    with app.app_context():
        # Check if admin user exists
        admin = User.query.filter_by(username='admin').first()
        
        if admin:
            print(f"✅ Admin user found: {admin.username}")
            print(f"   Email: {admin.email}")
            print(f"   Is Admin: {admin.is_admin}")
            print(f"   Password Hash: {admin.password_hash[:20]}...")
            
            # Test password verification
            test_passwords = ['admin123', 'Admin123', 'ADMIN123']
            for pwd in test_passwords:
                result = check_password_hash(admin.password_hash, pwd)
                print(f"   Password '{pwd}': {'✅ WORKS' if result else '❌ FAILS'}")
        else:
            print("❌ No admin user found!")
            
        # List all users
        all_users = User.query.all()
        print(f"\nAll users in database ({len(all_users)}):")
        for user in all_users:
            print(f"  - {user.username} (admin: {user.is_admin})")

if __name__ == '__main__':
    debug_login()
