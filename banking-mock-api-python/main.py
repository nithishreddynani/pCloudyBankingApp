from fastapi import FastAPI, HTTPException, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import uuid, os, math
from datetime import datetime, timezone, timedelta

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

POST_COMMIT = os.getenv("POST_COMMIT", "false").lower() == "true"
print(f"Mode: {'POST_COMMIT (bugs active)' if POST_COMMIT else 'NORMAL (all APIs working)'}")

# ── In-memory store ────────────────────────────────────────────────────────────
active_sessions = {}  # token → { username, name, accountNo }
user_state      = {}  # username → { balance, walletBalance, transactions }

USERS = {
    "user@test.com":  {"password": "Test@1234",   "pin": "1234", "name": "Ravi Kumar",   "accountNo": "XX4521"},
    "admin@test.com": {"password": "Admin@1234",  "pin": "0000", "name": "Priya Sharma", "accountNo": "XX9988"},
    "demo":           {"password": "password123", "pin": "0000", "name": "Demo User",    "accountNo": "XX0000"},
}

# ── Per-user state initialiser ─────────────────────────────────────────────────
def get_state(username: str):
    if username not in user_state:
        now = datetime.now(timezone.utc)
        import random
        debit_desc  = ["Swiggy Order", "Amazon Pay", "Zomato", "PhonePe", "BookMyShow"]
        debit_upi   = ["swiggy@okaxis", "amazon@okicici", "zomato@ybl", "merchant@ybl", "bms@okhdfc"]
        credit_desc = ["Salary Credit", "Refund - Flipkart", "Google Pay", "NEFT Credit", "UPI Received"]
        credit_upi  = ["salary@corp", "flipkart@ybl", "gpay@okaxis", "neft@sbi", "friend@paytm"]
        bill_desc   = ["Electricity Bill", "DTH Recharge", "Mobile Recharge", "Water Bill", "Gas Bill"]
        bill_upi    = ["bescom@ybl", "tatasky@okicici", "airtel@airtel", "bwssb@ybl", "igl@ybl"]

        txns = (
            [{"id": str(uuid.uuid4()), "type": "debit",
              "amount": -(random.randint(100, 999)),
              "description": debit_desc[i], "upiId": debit_upi[i],
              "date": (now - timedelta(days=i)).isoformat(), "status": "SUCCESS"} for i in range(5)]
            +
            [{"id": str(uuid.uuid4()), "type": "credit",
              "amount": random.randint(500, 2499),
              "description": credit_desc[i], "upiId": credit_upi[i],
              "date": (now - timedelta(days=i+5)).isoformat(), "status": "SUCCESS"} for i in range(5)]
            +
            [{"id": str(uuid.uuid4()), "type": "debit",
              "amount": -(random.randint(50, 549)),
              "description": bill_desc[i], "upiId": bill_upi[i],
              "date": (now - timedelta(days=i+10)).isoformat(), "status": "SUCCESS"} for i in range(5)]
        )
        user_state[username] = {"balance": 24350.0, "walletBalance": 5000.0, "transactions": txns}
    return user_state[username]

# ── Auth helper ────────────────────────────────────────────────────────────────
def get_current_user(request: Request):
    auth = request.headers.get("authorization", "")
    token = auth.split(" ")[1] if " " in auth else ""
    if not token or token not in active_sessions:
        raise HTTPException(status_code=401, detail="Unauthorized. Please login again.")
    return active_sessions[token]

# ── Request models ─────────────────────────────────────────────────────────────
class LoginRequest(BaseModel):
    username: str
    password: str

class RegisterRequest(BaseModel):
    username: str
    password: str
    name: str

class UpiPayRequest(BaseModel):
    upiId: str
    amount: float
    remarks: Optional[str] = None

class TopupRequest(BaseModel):
    amount: float

class PinRequest(BaseModel):
    pin: str

# ══════════════════════════════════════════════════════════════════════════════
# AUTH
# ══════════════════════════════════════════════════════════════════════════════

@app.post("/auth/login")
def login(body: LoginRequest):
    user = USERS.get(body.username)
    if not user or user["password"] != body.password:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    token = str(uuid.uuid4())
    active_sessions[token] = {"username": body.username, "name": user["name"], "accountNo": user["accountNo"]}
    get_state(body.username)

    if POST_COMMIT:
        with open("session.log", "a") as f:
            f.write(f"[{datetime.now(timezone.utc).isoformat()}] token={token} user={body.username}\n")

    return {"success": True, "token": token,
            "user": {"name": user["name"], "accountNo": user["accountNo"], "username": body.username}}

@app.post("/auth/register")
def register(body: RegisterRequest):
    if not body.username or not body.password or not body.name:
        raise HTTPException(status_code=400, detail="username, password and name are required")
    if body.username in USERS:
        raise HTTPException(status_code=409, detail="Account already exists with this username")

    account_no = "XX" + str(1000 + int(uuid.uuid4().int % 9000))
    USERS[body.username] = {"password": body.password, "pin": "0000", "name": body.name, "accountNo": account_no}

    token = str(uuid.uuid4())
    active_sessions[token] = {"username": body.username, "name": body.name, "accountNo": account_no}
    get_state(body.username)

    return {"success": True, "token": token,
            "user": {"name": body.name, "accountNo": account_no, "username": body.username}}

@app.post("/auth/verify-pin")
def verify_pin(body: PinRequest, user=Depends(get_current_user)):
    u = USERS[user["username"]]
    if u["pin"] != body.pin:
        raise HTTPException(status_code=401, detail="Invalid PIN")
    return {"success": True, "message": "PIN verified"}

@app.post("/auth/logout")
def logout(request: Request, user=Depends(get_current_user)):
    auth  = request.headers.get("authorization", "")
    token = auth.split(" ")[1] if " " in auth else ""
    active_sessions.pop(token, None)
    return {"success": True, "message": "Logged out successfully"}

# ══════════════════════════════════════════════════════════════════════════════
# BALANCE  (spec + android alias)
# ══════════════════════════════════════════════════════════════════════════════

def _balance_response(user):
    state = get_state(user["username"])
    key   = "availableBalance" if POST_COMMIT else "balance"
    return {"success": True, key: state["balance"], "currency": "INR",
            "accountNo": user["accountNo"], "accountHolder": user["name"],
            "lastUpdated": datetime.now(timezone.utc).isoformat()}

@app.get("/account/balance")
def account_balance(user=Depends(get_current_user)):
    return _balance_response(user)

@app.get("/banking/balance")
def banking_balance(user=Depends(get_current_user)):
    return _balance_response(user)

# ══════════════════════════════════════════════════════════════════════════════
# UPI PAYMENT  (spec + android alias)
# ══════════════════════════════════════════════════════════════════════════════

def _upi_pay(body: UpiPayRequest, user):
    state = get_state(user["username"])
    if body.upiId == "fail@test":
        raise HTTPException(status_code=422,
            detail={"success": False, "message": "Payment failed. Beneficiary account is inactive.", "errorCode": "UPI_FAILURE_001"})
    if body.amount > state["balance"]:
        raise HTTPException(status_code=422,
            detail={"success": False, "message": "Insufficient balance", "errorCode": "INSUFFICIENT_FUNDS"})

    state["balance"] -= body.amount
    txn = {"id": str(uuid.uuid4()), "type": "debit", "amount": -body.amount,
           "description": body.remarks or f"UPI Payment to {body.upiId}",
           "upiId": body.upiId, "date": datetime.now(timezone.utc).isoformat(),
           "status": "SUCCESS", "referenceId": f"TXN{int(datetime.now().timestamp()*1000)}"}
    state["transactions"].insert(0, txn)
    return {"success": True, "status": "SUCCESS", "message": "Payment successful",
            "transaction": txn, "newBalance": state["balance"]}

@app.post("/payment/upi")
def payment_upi(body: UpiPayRequest, user=Depends(get_current_user)):
    return _upi_pay(body, user)

@app.post("/banking/upi/pay")
def banking_upi_pay(body: UpiPayRequest, user=Depends(get_current_user)):
    return _upi_pay(body, user)

# ══════════════════════════════════════════════════════════════════════════════
# TRANSACTIONS  (spec + android alias)
# ══════════════════════════════════════════════════════════════════════════════

def _transactions(user, page: int = 1, limit: int = 5, type: Optional[str] = None):
    state    = get_state(user["username"])
    filtered = [t for t in state["transactions"] if not type or t["type"] == type]
    total    = len(filtered)
    start    = (page - 1) * limit
    return {"success": True, "transactions": filtered[start:start+limit],
            "pagination": {"page": page, "limit": limit, "total": total,
                           "totalPages": math.ceil(total / limit),
                           "hasNext": page < math.ceil(total / limit)}}

@app.get("/transactions")
def get_transactions(page: int = 1, limit: int = 5, type: Optional[str] = None, user=Depends(get_current_user)):
    return _transactions(user, page, limit, type)

@app.get("/banking/transactions")
def get_banking_transactions(page: int = 1, limit: int = 5, type: Optional[str] = None, user=Depends(get_current_user)):
    return _transactions(user, page, limit, type)

# ══════════════════════════════════════════════════════════════════════════════
# WALLET TOPUP  (spec + android alias)
# ══════════════════════════════════════════════════════════════════════════════

def _topup(body: TopupRequest, user):
    state = get_state(user["username"])
    if body.amount <= 0:
        raise HTTPException(status_code=400, detail="Invalid top-up amount")
    if body.amount > state["balance"]:
        raise HTTPException(status_code=422, detail="Insufficient bank balance for top-up")
    state["walletBalance"] += body.amount
    state["balance"]       -= body.amount
    return {"success": True, "message": f"Wallet topped up with ₹{body.amount}",
            "walletBalance": state["walletBalance"], "balance": state["balance"]}

@app.post("/wallet/topup")
def wallet_topup(body: TopupRequest, user=Depends(get_current_user)):
    return _topup(body, user)

@app.post("/banking/topup")
def banking_topup(body: TopupRequest, user=Depends(get_current_user)):
    return _topup(body, user)

# ══════════════════════════════════════════════════════════════════════════════
# NOTIFICATIONS  (spec + android alias)
# ══════════════════════════════════════════════════════════════════════════════

def _notifications(user):
    state = get_state(user["username"])
    now   = datetime.now(timezone.utc)
    notifs = []
    if state["balance"] < 5000:
        notifs.append({"id": str(uuid.uuid4()), "type": "LOW_BALANCE",
                        "title": "Low Balance Alert",
                        "body": f"Your account balance ₹{state['balance']:.2f} is below ₹5,000",
                        "deepLink": "banking://wallet/topup",
                        "timestamp": now.isoformat(), "read": False})
    notifs.append({"id": str(uuid.uuid4()), "type": "INFO", "title": "Security Tip",
                   "body": "Never share your PIN or OTP with anyone.",
                   "deepLink": "banking://profile/security",
                   "timestamp": (now - timedelta(hours=1)).isoformat(), "read": True})
    return {"success": True, "notifications": notifs,
            "unreadCount": sum(1 for n in notifs if not n["read"])}

@app.get("/notifications")
def notifications(user=Depends(get_current_user)):
    return _notifications(user)

@app.get("/banking/notifications")
def banking_notifications(user=Depends(get_current_user)):
    return _notifications(user)

# ══════════════════════════════════════════════════════════════════════════════
# WALLET BALANCE & PROFILE
# ══════════════════════════════════════════════════════════════════════════════

@app.get("/wallet/balance")
def wallet_balance(user=Depends(get_current_user)):
    state = get_state(user["username"])
    return {"success": True, "walletBalance": state["walletBalance"], "currency": "INR"}

@app.get("/profile")
def profile(user=Depends(get_current_user)):
    u = USERS[user["username"]]
    return {"success": True, "profile": {
        "name": u["name"], "username": user["username"], "accountNo": u["accountNo"],
        "linkedAccounts": [
            {"bank": "SBI",  "accountNo": "XXXX1234", "isPrimary": True},
            {"bank": "HDFC", "accountNo": "XXXX5678", "isPrimary": False},
        ]}}

# ── Run ────────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8080))
    print(f"\nBanking Mock API (Python) running on http://0.0.0.0:{port}")
    print("\nAvailable users:")
    print("  demo            / password123")
    print("  user@test.com   / Test@1234")
    print("  admin@test.com  / Admin@1234")
    uvicorn.run(app, host="0.0.0.0", port=port)
