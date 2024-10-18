import os
import redis
from datetime import datetime
from flask import Flask, flash, jsonify, redirect, render_template, request, session
from flask_session import Session
from flask_sqlalchemy import SQLAlchemy
from flask_marshmallow import Marshmallow
from tempfile import mkdtemp
from werkzeug.exceptions import default_exceptions, HTTPException, InternalServerError
#from werkzeug.security import check_password_hash, generate_password_hash
#from utils import errorPage

#from helpers import errorPage, login_required, lookup, usd

# Configure application
application = Flask(_name_)
basedir = os.path.abspath(os.path.dirname(_file_))

# Ensure templates are auto-reloaded
application.config["TEMPLATES_AUTO_RELOAD"] = True

# Ensure responses aren't cached
@application.after_request
def after_request(response):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
    response.headers["Expires"] = 0
    response.headers["Pragma"] = "no-cache"
    return response

# Custom filter
application.jinja_env.filters["usd"] = usd

# Configure Redis for storing the session data locally on the server-side
application.secret_key = 'BAD_SECRET_KEY'
application.config['SESSION_TYPE'] = 'redis'
application.config['SESSION_PERMANENT'] = False
application.config['SESSION_USE_SIGNER'] = True
application.config['SESSION_REDIS'] = redis.from_url('redis://localhost:6379')
# Create and initialize the Flask-Session object AFTER app has been configured
server_session = Session(application)

# Configure Flask to use local SQLite3 database with SQLAlchemy
application.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + os.path.join(basedir, 'finances.db')
application.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
application.config['SQLALCHEMY_ECHO'] = True
db = SQLAlchemy(application)

# Configure marshmallow
ma = Marshmallow(application)

# Create classes/models
class Users(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(length=50))
    hash = db.Column(db.String(length=200))
    cash = db.Column(db.Integer)
    # Create initializer/constructor
    def _init_(self, username, hash, cash):
        self.username = username
        self.hash = hash
        self.cash = cash
class Portfolio(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer)
    symbol = db.Column(db.String(length=5))
    current_shares = db.Column(db.Integer)
    # Create initializer/constructor
    def _init_(self, user_id, symbol, current_shares):
        self.user_id = user_id
        self.symbol = symbol
        self.current_shares = current_shares
class Bought(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    buyer_id = db.Column(db.Integer)
    time = db.Column(db.String(length=100))
    symbol = db.Column(db.String(length=5))
    shares_bought = db.Column(db.Integer)
    price_bought = db.Column(db.Float)
    # Create initializer/constructor
    def _init_(self, buyer_id, time, symbol, shares_bought, price_bought):
        self.buyer_id = buyer_id
        self.time = time
        self.symbol = symbol
        self.shares_bought = shares_bought
        self.price_bought = price_bought
class Sold(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    seller_id = db.Column(db.Integer)
    time = db.Column(db.String(length=100))
    symbol = db.Column(db.String(length=5))
    shares_sold = db.Column(db.Integer)
    price_sold = db.Column(db.Float)
    # Create initializer/constructor
    def _init_(self, seller_id, time, symbol, shares_sold, price_sold):
        self.seller_id = seller_id
        self.time = time
        self.symbol = symbol
        self.shares_sold = shares_sold
        self.price_sold = price_sold

# Create Schemas (only include data you want to show)
class UsersSchema(ma.Schema):
    class Meta:
        fields = ('username', 'cash')
class PortfolioSchema(ma.Schema):
    class Meta:
        fields = ('symbol', 'current_shares')
class BoughtSchema(ma.Schema):
    class Meta:
        fields = ('time', 'symbol', 'shares_bought', 'price_bought')
class SoldSchema(ma.Schema):
    class Meta:
        fields = ('time', 'symbol', 'shares_sold', 'price_sold')
        
# Initialize Schemas
users_schema = UsersSchema
portfolio_schema = PortfolioSchema(many=True)
bought_schema = BoughtSchema(many=True)
sold_schema = SoldSchema(many=True)

# Make sure API key is set
os.environ.get("API_KEY")

if not os.environ.get("API_KEY"):
    raise RuntimeError("API_KEY not set")

@application.route("/")
def landing():
    return render_template("landing.html")

@application.route("/home")
@login_required
def index():
    # Obtain user id
    user = session["user_id"]
    print("user: ", user)

    # Obtain available cash
    available = (Users.query.filter_by(id = user).first()).cash
    print("available: ", available)

    # Obtain at least one stock symbol that the user possesses
    symbol_list = Portfolio.query.filter_by(user_id = user).all()
    print("symbol list: ", symbol_list)

    # If user has no stocks return minimum information
    if symbol_list == []:
        return render_template("index.html", available = usd(available), grand_total = usd(available),  total = [], shares = [], price = [], symbols = [], symbol_list_length = 0)
    # If user owns stocks return the remaining information
    else:
        # Calculate symbol list length for iteration in index.html
        symbol_list_length = len(symbol_list)
        print("symbol_list_length: ", symbol_list_length)

        # Create empty arrays to store values
        symbols = []
        price = []
        shares = []
        total = []
        # Calculate value of each holding of stock in portfolio
        for i in range(len(symbol_list)):
            symbol_index = symbol_list[i].symbol
            print("symbol_index:", symbol_index)
            symbols.append(symbol_index)
            # Obtain price of stock using iex API
            price_index = float(lookup(symbol_index).get('price'))
            print("price_index:", price_index)
            price.append(price_index)
            # Obtain number of shares that the user possesses to calculate total value
            shares_list = Portfolio.query.filter_by(user_id = user, symbol = symbol_index).all()
            print("shares_list:", shares_list)
            #("SELECT current_shares FROM portfolio WHERE user_id = :id AND symbol = :symbol", id = user, symbol = symbol_index)
            # Iterate over list of dicts
            for i in range(len(shares_list)):
                share_index = shares_list[i].current_shares
                print("share_index:", share_index)
                shares.append(share_index)
            # Calculate total value of stocks
            calc = share_index * price_index
            print("calc:", calc)
            total.append(calc)
        print("symbols:", symbols)
        print("price:", price)
        print("shares:", shares)
        print("total:", total)
        # Calculate grand total value of all assets
        grand_total = sum(total) + available

        # Render page with information
        return render_template("index.html", symbol_list = symbol_list, symbol_list_length = symbol_list_length, shares = shares, price = price, total = total, available = usd(available), grand_total = usd(grand_total))


@application.route("/buy", methods=["GET", "POST"])
@login_required
def buy():
    if request.method == "GET":
        return render_template("buy.html")
    else:
        symbol = request.form.get("symbol").upper()

        # User error handling: stop empty symbol and shares fields, stop invalid symbols, and negative share numbers
        if not symbol:
            return errorPage(title="No Data", info = "Please enter a stock symbol, i.e. AMZN", file = "no-data.svg")
        result = lookup(symbol)
        if result == None:
             return errorPage(title = "Bad Request", info = "Please enter a valid stock symbol", file="animated-400.svg")
        shares = int(request.form.get("shares"))
        if symbol == None:
            return errorPage(title="No Data", info = "Please enter number of shares", file = "no-data.svg")
        if shares < 0:
             return errorPage(title = "Bad Request", info = "Please enter a positive number", file="animated-400.svg")
        if shares == 0:
             return errorPage(title="No Data", info = "Transaction will not proceed", file = "no-data.svg")

        # Obtain user id
        user = session["user_id"]
        print("user:", user)

        # Obtain available cash
        available = (Users.query.filter_by(id = user).first()).cash
        print("available:", available)

        # Use IEX API to get price of stock
        price = lookup(symbol).get('price')
        print("price:", price)

        # Calculate total cost
        total = shares * price
        
        # User error handling: stop user if seeking to buy beyond cash balance
        if available < total:
             return errorPage(title="Forbidden", info = "Insufficient funds to complete transaction", file="animated-403.svg")
        
        # Continue with transaction and calculate remaining cash
        remaining = available - total

        # Obtain year, month, day, hour, minute, second
        now = datetime.now()
        time = now.strftime("%d/%m/%Y %H:%M:%S")

        # Update cash field in Users Table and create entry into Bought Table
        update_cash = Users.query.filter_by(id = user).first()
        update_cash.cash = remaining
        db.session.commit()
        #"UPDATE users SET cash = :remaining WHERE id = :id", remaining = remaining, id = user)

        # Log transaction history
        log_purchase = Bought(user, time, symbol, shares, price)
        db.session.add(log_purchase)
        db.session.commit()
        #("INSERT INTO bought (buyer_id, time, symbol, shares_bought, price_bought) VALUES (:buyer_id, :time, :symbol, :shares_bought, :price_bought)", time = datetime.datetime.now(), symbol = symbol, shares_bought = shares, price_bought = price, buyer_id = user)

        # If buyer never bought this stock before
        portfolio = Portfolio.query.filter(Portfolio.user_id == user, Portfolio.symbol == symbol).first()
        print("portfolio", portfolio)

        #("SELECT symbol FROM portfolio WHERE user_id = :id AND symbol = :symbol", id = user, symbol = symbol)
        if portfolio == None:
            db.session.add(Portfolio(user, symbol, shares))
            db.session.commit()
            #("INSERT INTO portfolio (user_id, symbol, current_shares) VALUES (:user_id, :symbol, :current_shares)", user_id = user, symbol = symbol, current_shares = shares)
        else:
            stock_owned = portfolio.symbol
            print("stock_owned", stock_owned)
            # Obtain current number of shares from portfolio
            current_shares = portfolio.current_shares
            print("current shares", current_shares)
            #("SELECT current_shares FROM portfolio WHERE user_id = :id AND symbol = :symbol", id = user, symbol = symbol)

            # Calculate new amount of shares
            new_shares = shares + current_shares
            print("Total shares now:", new_shares)

            # Update portfolio table with new amount of shares
            portfolio.current_shares = new_shares
            print("Update db with new total:", portfolio.current_shares)
            db.session.commit()
            #("UPDATE portfolio SET current_shares = :new_shares WHERE user_id = :id", new_shares = new_shares, id = user)

        return render_template("bought.html", symbol = symbol, shares = shares, total = usd(total))


@application.route("/history")
@login_required
def history():
    # Obtain user id
    user = session["user_id"]

    # Obtain purchase history
    bought_list = Bought.query.filter_by(buyer_id = user).all()
    print("bought_list:", bought_list)
    #("SELECT time, symbol, shares_bought, price_bought FROM bought WHERE buyer_id = :id", id = user)

    # If user didn't sell stocks, only query bought table, if didn't buy anything, return empty
    if bought_list == []:
        # Will return empty list if user didn't buy anything
        return render_template("history.html", bought_list_length = 0, bought_list = [], sold_list_length = 0, sold_list = [])
        
    # Else query sold table
    else:
        # Obtain sell history
        sold_list = Sold.query.filter_by(seller_id = user).all()
        print("sold_list:", sold_list)
        #("SELECT time, symbol, shares_sold, price_sold FROM sold WHERE seller_id = :id", id = user)

        # Calculate length of bought_list and sold_list
        bought_list_length = len(bought_list)
        sold_list_length = len(sold_list)

        return render_template("history.html", bought_list = bought_list, sold_list = sold_list, bought_list_length = bought_list_length, sold_list_length = sold_list_length)


@application.route("/login", methods=["GET", "POST"])
def login():
    """Log user in"""

    # Forget any user_id
    session.clear()

    # User reached route via POST (as by submitting a form via POST)
    if request.method == "POST":

        # Ensure username was submitted
        if not request.form.get("username"):
            return errorPage(title="No Data", info = "Must provide username", file = "no-data.svg")

        # Ensure password was submitted
        elif not request.form.get("password"):
            return errorPage(title="No Data", info = "Must provide password", file = "no-data.svg")

        # Query database for username
        rows = Users.query.filter_by(username=request.form.get("username")).first()
        #("SELECT * FROM users WHERE username = :username", username=request.form.get("username"))

        # Ensure user exists
        try:
            rows.username

        # NoneType is returned and therefore username does't exist in database
        except AttributeError:
             return errorPage(title="No Data", info = "User doesn't exist", file = "no-data.svg")

        # Finish logging user in
        else:
            # Ensure username and password is correct
            if rows.username != request.form.get("username") or not check_password_hash(rows.hash, request.form.get("password")):
                return errorPage(title = "Unauthorized", info = "invalid username and/or password", file="animated-401.svg")

            # Remember which user has logged in
            session["user_id"] = rows.id

            # Redirect user to home page
            return redirect("/home")

    # User reached route via GET (as by clicking a link or via redirect)
    else:
        return render_template("login.html")


@application.route("/logout")
def logout():
    """Log user out"""

    # Forget any user_id
    session.clear()

    # Redirect user to login form
    return redirect("/")


@application.route("/quote", methods=["GET", "POST"])
@login_required
def quote():
    if request.method == "GET":
        return render_template("quote.html")
    else:
        symbol = request.form.get("symbol")
        data = lookup(symbol)
        # User error handling: stop empty symbol and shares fields, stop invalid symbols, and negative share numbers
        if not symbol:
            return errorPage(title="No Data", info = "Please enter a stock symbol, i.e. AMZN", file = "no-data.svg")
        if data == None:
            return errorPage(title = "Bad Request", info = "Please enter a valid stock symbol", file="animated-400.svg")
        return render_template("quoted.html", data = data)


@application.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "GET":
        return render_template("register.html")
    else:
        # Obtain username inputted
        username = request.form.get("username")

        # User error handling: stop empty username and password fields, stop usernames already taken, stop non-matching passwords
        if not username:
            return errorPage(title="No Data", info = "Please enter a username", file = "no-data.svg")

        existing = Users.query.filter_by(username=username)
        print("EXISTING USER: ", existing)
        #("SELECT * FROM users WHERE username = :username", username=username)
        if existing == username:
            print("EXISTING USER ALREADY!: ", existing)
            return errorPage(title="Forbidden", info = "Username already taken", file="animated-403.svg")
        password = request.form.get("password")
        if not password:
            return errorPage(title="No Data", info = "Please enter a password", file = "no-data.svg")
        confirmation = request.form.get("confirmation")
        if password != confirmation:
            return errorPage(title = "Unauthorized", info = "Passwords do not match", file="animated-401.svg")
        hashed = generate_password_hash(password, method='pbkdf2:sha256', salt_length=8)

        # All users automatically recieve $10,000 to start with
        cash = 10000

        # Add and commit the data into database
        db.session.add(Users(username, hashed, cash))
        db.session.commit()
        #("INSERT INTO users (username, hash) VALUES (:username, :hash)", username=username, hash=hashed)

        # Automatically sign in after creating account
        rows = Users.query.filter_by(username=request.form.get("username")).first()
        session["user_id"] = rows.id

        # Redirect user to home page
        return redirect("/home")


@application.route("/sell", methods=["GET", "POST"])
@login_required
def sell():
    # Obtain user id
    user = session["user_id"]

    if request.method == "GET":
        # Obtain stock symbols that the user possesses
        symbol_list = Portfolio.query.filter_by(user_id = user).all()
        #("SELECT symbol FROM portfolio WHERE user_id = :id", id = user)

        # If user never bought anaything, return empty values
        if symbol_list == []:
            return render_template("sell.html", symbol_list_length = 0)
        # Else display stock symbols in drop-down menu
        else:
            symbol_list_length = len(symbol_list)
            # Render sell page with list of stocks the user owns
            return render_template("sell.html", symbol_list = symbol_list, symbol_list_length = symbol_list_length)
    else:
        # Obtain stock symbol from user
        symbol = request.form.get("symbol")

        # If user doesn't own stock, render error
        if symbol == '':
            return errorPage(title="Forbidden", info = "Must own stock before selling", file="animated-403.svg")

        # Obtain number of shares from user
        shares = int(request.form.get("shares"))

        # Prevent user from submitting form with no number, negative number, or zero
        if not shares:
             return errorPage(title="No Data", info = "Please enter number of shares", file = "no-data.svg")
        if shares < 0:
             return errorPage(title = "Bad Request", info = "Please enter a positive number", file="animated-400.svg")
        if shares == 0:
             return errorPage(title="No Data", info = "Transaction will not proceed", file = "no-data.svg")

        # Obtain data for stock selected
        shares_held_list = Portfolio.query.filter(Portfolio.user_id == user, Portfolio.symbol == symbol).first()
        #("SELECT current_shares FROM portfolio WHERE user_id = :id AND symbol = :symbol", id = user, symbol = symbol)
        print("shares_held_list:", shares_held_list)

        # Obtain number of shares for stock selected
        shares_held = shares_held_list.current_shares
        print("shares_held:", shares_held)

        # Prevent user from selling more than they have
        if shares > shares_held:
            return errorPage(title="Forbidden", info = "Unable to sell more than you have", file="animated-403.svg")

        # Obtain available cash
        available = (Users.query.filter_by(id = user).first()).cash
        #("SELECT cash FROM users WHERE id = :id", id = user)

        # Obtain current price of stock
        price = lookup(symbol).get('price')

        # Calculate new number of shares
        updated_shares = shares_held - shares

        # Remove stocks from user's portfolio by number of shares indicated
        portfolio = Portfolio.query.filter(Portfolio.user_id == user, Portfolio.symbol == symbol).first()
        print("portfolio", portfolio)
        portfolio.current_shares = updated_shares
        print("Update db with new total:", portfolio.current_shares)
        db.session.commit()
        #("UPDATE portfolio SET current_shares = :updated_shares WHERE user_id = :id", updated_shares = updated_shares, id = user)

        # Calculate new amount of available cash
        total = available + (price * shares)

        # Update cash field in Users Table
        update_cash = Users.query.filter_by(id = user).first()
        update_cash.cash = total
        db.session.commit()
        #("UPDATE users SET cash = :total WHERE id = :id", total = total, id = user)

        # Obtain year, month, day, hour, minute, second
        now = datetime.now()
        time = now.strftime("%d/%m/%Y %H:%M:%S")

        # Log transaction history
        log_sale = Sold(user, time, symbol, shares, price)
        db.session.add(log_sale)
        db.session.commit()
        #("INSERT INTO sold (seller_id, time, symbol, shares_sold, price_sold) VALUES (:seller_id, :time, :symbol, :shares_sold, :price_sold)", time = datetime.datetime.now(), symbol = symbol, shares_sold = shares, price_sold = price, seller_id = user)

        # Render success page with infomation about transaction
        return render_template("sold.html", shares = shares, symbol = symbol.upper())


# def errorhandler(e):
#     """Handle error"""
#     if not isinstance(e, HTTPException):
#         e = InternalServerError()
#     return apology(e.name, e.code)


# Listen for errors
# for code in default_exceptions:
#     application.errorhandler(code)(errorhandler)

@application.errorhandler(404)
def page_not_found(e):
    # note that we set the 404 status explicitly
    return render_template('404.html'), 404

# Run Server
# Run the following in the command line: python application.py
if _name_ == '_main_':
    application.run(host='')
