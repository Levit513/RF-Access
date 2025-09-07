#!/usr/bin/env python3
"""
Check if card programs were saved
"""

from app import app, db, CardProgram, User

def check_programs():
    with app.app_context():
        # Check all programs
        programs = CardProgram.query.all()
        print(f"Total programs in database: {len(programs)}")
        
        for program in programs:
            print(f"- ID: {program.id}")
            print(f"  Name: {program.name}")
            print(f"  Description: {program.description}")
            print(f"  Created by: {program.created_by}")
            print(f"  Created at: {program.created_at}")
            print(f"  Sector data length: {len(program.sector_data)} chars")
            print(f"  Active: {program.is_active}")
            print()

if __name__ == '__main__':
    check_programs()
