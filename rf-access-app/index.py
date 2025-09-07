from app import app

# Netlify serverless function entry point
def handler(event, context):
    return app(event, context)

if __name__ == "__main__":
    app.run(debug=True)
