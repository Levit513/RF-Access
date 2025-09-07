#!/usr/bin/env python3
"""
Manual Admin User Creation Script
Run this if login isn't working
"""

from app import app, db, User
from werkzeug.security import generate_password_hash

def create_admin():
    with app.app_context():
        # Delete existing admin if any
        existing_admin = User.query.filter_by(username='admin').first()
        if existing_admin:
            db.session.delete(existing_admin)
            db.session.commit()
            print("Deleted existing admin user")
        
        # Create fresh admin user
        admin = User(
            username='admin',
            email='admin@example.com',
            password_hash=generate_password_hash('admin123'),
            is_admin=True
        )
        
        db.session.add(admin)
        db.session.commit()
        
        print("✅ Admin user created successfully!")
        print("Username: admin")
        print("Password: admin123")
        
        # Test the login
        from werkzeug.security import check_password_hash
        test_user = User.query.filter_by(username='admin').first()
        if check_password_hash(test_user.password_hash, 'admin123'):
            print("✅ Password verification works!")
        else:
            print("❌ Password verification failed!")

if __name__ == '__main__':
    create_admin()
