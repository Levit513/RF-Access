from flask import Flask, render_template, request, jsonify, redirect, url_for, session
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
import secrets
import json
from datetime import datetime, timedelta
import firebase_admin
from firebase_admin import credentials, messaging, firestore
import os

app = Flask(__name__)
app.secret_key = os.environ.get('SECRET_KEY', 'your-secret-key-here')
app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('DATABASE_URL', 'sqlite:///rf_access_admin.db')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# Initialize Firebase Admin SDK
if not firebase_admin._apps:
    cred = credentials.Certificate('firebase-service-account.json')
    firebase_admin.initialize_app(cred)

# Database Models
class AdminUser(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password_hash = db.Column(db.String(120), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class EndUser(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), nullable=True)
    firebase_uid = db.Column(db.String(120), nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)

class RFProgram(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    description = db.Column(db.Text, nullable=True)
    sector_data = db.Column(db.Text, nullable=False)  # JSON string
    created_by = db.Column(db.Integer, db.ForeignKey('admin_user.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    is_active = db.Column(db.Boolean, default=True)

class ProgramDistribution(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    program_id = db.Column(db.Integer, db.ForeignKey('rf_program.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('end_user.id'), nullable=False)
    token = db.Column(db.String(64), unique=True, nullable=False)
    status = db.Column(db.String(20), default='pending')  # pending, completed, expired
    distributed_at = db.Column(db.DateTime, default=datetime.utcnow)
    completed_at = db.Column(db.DateTime, nullable=True)
    expires_at = db.Column(db.DateTime, nullable=False)

# Routes
@app.route('/')
def index():
    if 'admin_id' not in session:
        return redirect(url_for('login'))
    return render_template('admin_dashboard.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        
        admin = AdminUser.query.filter_by(username=username).first()
        if admin and check_password_hash(admin.password_hash, password):
            session['admin_id'] = admin.id
            session['admin_username'] = admin.username
            return redirect(url_for('index'))
        else:
            return render_template('login.html', error='Invalid credentials')
    
    return render_template('login.html')

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

@app.route('/users')
def manage_users():
    if 'admin_id' not in session:
        return redirect(url_for('login'))
    
    users = EndUser.query.all()
    return render_template('manage_users.html', users=users)

@app.route('/users/add', methods=['POST'])
def add_user():
    if 'admin_id' not in session:
        return jsonify({'error': 'Unauthorized'}), 401
    
    username = request.form['username']
    email = request.form.get('email', '')
    
    if EndUser.query.filter_by(username=username).first():
        return jsonify({'error': 'Username already exists'}), 400
    
    user = EndUser(username=username, email=email)
    db.session.add(user)
    db.session.commit()
    
    return jsonify({'success': True, 'user_id': user.id})

@app.route('/programs')
def manage_programs():
    if 'admin_id' not in session:
        return redirect(url_for('login'))
    
    programs = RFProgram.query.filter_by(is_active=True).all()
    return render_template('manage_programs.html', programs=programs)

@app.route('/programs/create', methods=['GET', 'POST'])
def create_program():
    if 'admin_id' not in session:
        return redirect(url_for('login'))
    
    if request.method == 'POST':
        name = request.form['name']
        description = request.form.get('description', '')
        sector_data = request.form['sector_data']
        
        # Validate JSON
        try:
            json.loads(sector_data)
        except json.JSONDecodeError:
            return render_template('create_program.html', error='Invalid sector data JSON')
        
        program = RFProgram(
            name=name,
            description=description,
            sector_data=sector_data,
            created_by=session['admin_id']
        )
        db.session.add(program)
        db.session.commit()
        
        return redirect(url_for('manage_programs'))
    
    return render_template('create_program.html')

@app.route('/distribute', methods=['GET', 'POST'])
def distribute_program():
    if 'admin_id' not in session:
        return redirect(url_for('login'))
    
    if request.method == 'POST':
        program_id = request.form['program_id']
        user_id = request.form['user_id']
        
        # Generate unique token
        token = secrets.token_urlsafe(32)
        expires_at = datetime.utcnow() + timedelta(hours=24)
        
        distribution = ProgramDistribution(
            program_id=program_id,
            user_id=user_id,
            token=token,
            expires_at=expires_at
        )
        db.session.add(distribution)
        db.session.commit()
        
        # Send push notification to user
        send_program_notification(user_id, program_id)
        
        return jsonify({
            'success': True,
            'token': token,
            'message': 'Program distributed successfully'
        })
    
    programs = RFProgram.query.filter_by(is_active=True).all()
    users = EndUser.query.filter_by(is_active=True).all()
    return render_template('distribute_program.html', programs=programs, users=users)

@app.route('/api/program/<token>')
def get_program_data(token):
    distribution = ProgramDistribution.query.filter_by(token=token).first()
    
    if not distribution:
        return jsonify({'success': False, 'message': 'Invalid token'}), 404
    
    if distribution.status == 'completed':
        return jsonify({'success': False, 'message': 'Program already used'}), 400
    
    if distribution.expires_at < datetime.utcnow():
        return jsonify({'success': False, 'message': 'Program expired'}), 400
    
    program = RFProgram.query.get(distribution.program_id)
    if not program:
        return jsonify({'success': False, 'message': 'Program not found'}), 404
    
    return jsonify({
        'success': True,
        'program_name': 'RF Access Programming',  # User-friendly name
        'sector_data': json.loads(program.sector_data)
    })

@app.route('/api/programming-complete/<token>', methods=['POST'])
def mark_programming_complete(token):
    distribution = ProgramDistribution.query.filter_by(token=token).first()
    
    if not distribution:
        return jsonify({'success': False, 'message': 'Invalid token'}), 404
    
    distribution.status = 'completed'
    distribution.completed_at = datetime.utcnow()
    db.session.commit()
    
    return jsonify({'success': True, 'message': 'Programming marked as complete'})

def send_program_notification(user_id, program_id):
    """Send push notification to user about new program"""
    try:
        user = EndUser.query.get(user_id)
        if not user or not user.firebase_uid:
            return
        
        message = messaging.Message(
            notification=messaging.Notification(
                title='RF Access Programming',
                body='You have a new access card ready to program'
            ),
            data={
                'type': 'program_available',
                'program_id': str(program_id)
            },
            topic=f'user_{user.firebase_uid}'
        )
        
        messaging.send(message)
    except Exception as e:
        print(f"Failed to send notification: {e}")

# Initialize database
@app.before_first_request
def create_tables():
    db.create_all()
    
    # Create default admin user if none exists
    if not AdminUser.query.first():
        admin = AdminUser(
            username='admin',
            password_hash=generate_password_hash('admin123')
        )
        db.session.add(admin)
        db.session.commit()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
