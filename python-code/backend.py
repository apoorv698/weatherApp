from flask import Flask, render_template, request, redirect, url_for, jsonify

app = Flask(__name__)

@app.route('/', methods = ['GET'])
def index():
	if request.method == "GET":
		print("GET REQUEST")
		
		return jsonify({"GET_REQUEST_HANDLED!!": "GET"})


if __name__ =="__main__":
	app.run(debug=True,port=5000)