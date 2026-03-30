# pCloudy Banking App — API Documentation

**Base URL:** `https://demo-banking-server-production.up.railway.app`

---

## Default Users

| Username | Password | PIN |
|---|---|---|
| `demo` | `password123` | `0000` |
| `user@test.com` | `Test@1234` | `1234` |
| `admin@test.com` | `Admin@1234` | `0000` |

> **Note:** Protected APIs require `Authorization: Bearer <token>` header.
> Get the token from Login or Register response.

---

---

## 1. Login

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/auth/login`
**Auth Required:** No

### Request Body
```json
{
  "username": "demo",
  "password": "password123"
}
```

### Success Response `200`
```json
{
  "success": true,
  "token": "6f7c954b-6efd-419d-8c67-8a06f0f331b4",
  "user": {
    "name": "Demo User",
    "accountNo": "XX0000",
    "username": "demo"
  }
}
```

### Failure Response `401`
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/auth/login
Headers : Content-Type: application/json
Body    : raw → JSON
          { "username": "demo", "password": "password123" }
```

### curl
```bash
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}'
```

---

---

## 2. Register

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/auth/register`
**Auth Required:** No

### Request Body
```json
{
  "username": "john",
  "password": "john@123",
  "name": "John"
}
```

### Success Response `201`
```json
{
  "success": true,
  "token": "abc123-...",
  "user": {
    "name": "John",
    "accountNo": "XX4823",
    "username": "john"
  }
}
```

### Failure Response `409` — User already exists
```json
{
  "success": false,
  "message": "Account already exists with this username"
}
```

### Failure Response `400` — Missing fields
```json
{
  "success": false,
  "message": "username, password and name are required"
}
```

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/auth/register
Headers : Content-Type: application/json
Body    : raw → JSON
          { "username": "john", "password": "john@123", "name": "John" }
```

### curl
```bash
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"john@123","name":"John"}'
```

---

---

## 3. Logout

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/auth/logout`
**Auth Required:** Yes

### Success Response `200`
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/auth/logout
Headers : Authorization: Bearer <token>
Body    : none
```

### curl
```bash
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## 4. Verify PIN

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/auth/verify-pin`
**Auth Required:** Yes

### Request Body
```json
{
  "pin": "0000"
}
```

### Success Response `200`
```json
{
  "success": true,
  "message": "PIN verified"
}
```

### Failure Response `401`
```json
{
  "success": false,
  "message": "Invalid PIN"
}
```

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/auth/verify-pin
Headers : Authorization: Bearer <token>
          Content-Type: application/json
Body    : raw → JSON
          { "pin": "0000" }
```

### curl
```bash
# Correct PIN
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/verify-pin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"pin":"0000"}'

# Wrong PIN
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/verify-pin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"pin":"9999"}'
```

---

---

## 5. Get Balance

**Method:** `GET`
**URL:** `https://demo-banking-server-production.up.railway.app/banking/balance`
**Alias:** `/account/balance`
**Auth Required:** Yes

### Success Response `200`
```json
{
  "success": true,
  "balance": 24350,
  "currency": "INR",
  "accountNo": "XX0000",
  "accountHolder": "Demo User",
  "lastUpdated": "2026-03-18T10:00:00.000Z"
}
```

### Postman
```
Method  : GET
URL     : https://demo-banking-server-production.up.railway.app/banking/balance
Headers : Authorization: Bearer <token>
Body    : none
```

### curl
```bash
# Android path
curl -s https://demo-banking-server-production.up.railway.app/banking/balance \
  -H "Authorization: Bearer YOUR_TOKEN"

# Spec path (same response)
curl -s https://demo-banking-server-production.up.railway.app/account/balance \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## 6. UPI Payment

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/banking/upi/pay`
**Alias:** `/payment/upi`
**Auth Required:** Yes

### Request Body
```json
{
  "upiId": "friend@upi",
  "amount": 100,
  "remarks": "Dinner"
}
```

### Success Response `200`
```json
{
  "success": true,
  "status": "SUCCESS",
  "message": "Payment successful",
  "transaction": {
    "id": "uuid",
    "type": "debit",
    "amount": -100,
    "description": "UPI Payment to friend@upi",
    "upiId": "friend@upi",
    "date": "2026-03-18T10:00:00.000Z",
    "status": "SUCCESS",
    "referenceId": "TXN1234567890"
  },
  "newBalance": 24250
}
```

> **Auto Top-Up Behaviour**
> If the previous transaction caused the balance to fall below ₹5,000, the server automatically credits the account to bring the balance back to ₹10,000 **before** processing the next payment.
> - An `"Auto Balance Top-Up"` credit transaction is inserted at the top of the transaction history.
> - `newBalance` in the response reflects the balance **after** the auto top-up and the current debit are both applied.
>
> Example:
> - Balance = ₹4,500 (below ₹5,000 after a previous payment)
> - Next payment of ₹200 → balance topped up to ₹10,000 first → deducted ₹200 → `newBalance` = ₹9,800

### Failure Response `422` — Inactive account
```json
{
  "success": false,
  "message": "Payment failed. Beneficiary account is inactive.",
  "errorCode": "UPI_FAILURE_001"
}
```

### Failure Response `422` — Insufficient balance
```json
{
  "success": false,
  "message": "Insufficient balance",
  "errorCode": "INSUFFICIENT_FUNDS"
}
```

### Test Cases
| upiId | amount | Expected |
|---|---|---|
| `friend@upi` | `100` | ✅ Success |
| `fail@test` | any | ❌ 422 Inactive account |
| `timeout@test` | any | ⏱ 408 Timeout (30s) |
| any | `999999` | ❌ 422 Insufficient balance |
| any (when prev balance < ₹5,000) | any | ✅ Success + auto top-up to ₹10,000 applied first |

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/banking/upi/pay
Headers : Authorization: Bearer <token>
          Content-Type: application/json
Body    : raw → JSON
          { "upiId": "friend@upi", "amount": 100, "remarks": "Dinner" }
```

### curl
```bash
# Success
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/upi/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"upiId":"friend@upi","amount":100,"remarks":"Dinner"}'

# Failure — inactive account
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/upi/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"upiId":"fail@test","amount":100}'

# Failure — insufficient balance
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/upi/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"upiId":"friend@upi","amount":999999}'

# Spec path
curl -s -X POST https://demo-banking-server-production.up.railway.app/payment/upi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"upiId":"friend@upi","amount":100}'
```

---

---

## 7. Transactions

**Method:** `GET`
**URL:** `https://demo-banking-server-production.up.railway.app/banking/transactions`
**Alias:** `/transactions`
**Auth Required:** Yes

### Query Parameters
| Param | Type | Default | Description |
|---|---|---|---|
| `page` | number | `1` | Page number |
| `limit` | number | `5` | Items per page |
| `type` | string | all | `debit` or `credit` |

### Success Response `200`
```json
{
  "success": true,
  "transactions": [
    {
      "id": "uuid",
      "type": "debit",
      "amount": -100,
      "description": "UPI Payment to friend@upi",
      "upiId": "friend@upi",
      "date": "2026-03-18T10:00:00.000Z",
      "status": "SUCCESS"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 5,
    "total": 15,
    "totalPages": 3,
    "hasNext": true
  }
}
```

### Postman
```
Method  : GET
URL     : https://demo-banking-server-production.up.railway.app/banking/transactions
Headers : Authorization: Bearer <token>
Params  : page=1, limit=5, type=debit   (optional)
Body    : none
```

### curl
```bash
# Page 1 (default)
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?page=1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Page 2
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?page=2" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Debits only
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?type=debit" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Credits only
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?type=credit" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Custom limit
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Spec path
curl -s "https://demo-banking-server-production.up.railway.app/transactions?page=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## 8. Wallet Balance

**Method:** `GET`
**URL:** `https://demo-banking-server-production.up.railway.app/wallet/balance`
**Auth Required:** Yes

### Success Response `200`
```json
{
  "success": true,
  "walletBalance": 5000,
  "currency": "INR"
}
```

### Postman
```
Method  : GET
URL     : https://demo-banking-server-production.up.railway.app/wallet/balance
Headers : Authorization: Bearer <token>
Body    : none
```

### curl
```bash
curl -s https://demo-banking-server-production.up.railway.app/wallet/balance \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## 9. Wallet Topup

**Method:** `POST`
**URL:** `https://demo-banking-server-production.up.railway.app/banking/topup`
**Alias:** `/wallet/topup`
**Auth Required:** Yes

### Request Body
```json
{
  "amount": 500
}
```

### Success Response `200`
```json
{
  "success": true,
  "message": "Wallet topped up with ₹500",
  "walletBalance": 5500,
  "balance": 23850
}
```

### Failure Response `422` — Insufficient bank balance
```json
{
  "success": false,
  "message": "Insufficient bank balance for top-up"
}
```

### Postman
```
Method  : POST
URL     : https://demo-banking-server-production.up.railway.app/banking/topup
Headers : Authorization: Bearer <token>
          Content-Type: application/json
Body    : raw → JSON
          { "amount": 500 }
```

### curl
```bash
# Success
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/topup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount":500}'

# Failure — insufficient balance
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/topup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount":999999}'

# Spec path
curl -s -X POST https://demo-banking-server-production.up.railway.app/wallet/topup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount":1000}'
```

---

---

## 10. Notifications

**Method:** `GET`
**URL:** `https://demo-banking-server-production.up.railway.app/banking/notifications`
**Alias:** `/notifications`
**Auth Required:** Yes

### Success Response `200`
```json
{
  "success": true,
  "notifications": [
    {
      "id": "uuid",
      "type": "LOW_BALANCE",
      "title": "Low Balance Alert",
      "body": "Your account balance ₹4350.00 is below ₹5,000",
      "deepLink": "banking://wallet/topup",
      "timestamp": "2026-03-18T10:00:00.000Z",
      "read": false
    },
    {
      "id": "uuid",
      "type": "INFO",
      "title": "Security Tip",
      "body": "Never share your PIN or OTP with anyone.",
      "deepLink": "banking://profile/security",
      "timestamp": "2026-03-18T09:00:00.000Z",
      "read": true
    }
  ],
  "unreadCount": 1
}
```

> **Note:** `LOW_BALANCE` alert appears only when balance is below ₹5,000

### Postman
```
Method  : GET
URL     : https://demo-banking-server-production.up.railway.app/banking/notifications
Headers : Authorization: Bearer <token>
Body    : none
```

### curl
```bash
# Android path
curl -s https://demo-banking-server-production.up.railway.app/banking/notifications \
  -H "Authorization: Bearer YOUR_TOKEN"

# Spec path
curl -s https://demo-banking-server-production.up.railway.app/notifications \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## 11. Profile

**Method:** `GET`
**URL:** `https://demo-banking-server-production.up.railway.app/profile`
**Auth Required:** Yes

### Success Response `200`
```json
{
  "success": true,
  "profile": {
    "name": "Demo User",
    "username": "demo",
    "accountNo": "XX0000",
    "linkedAccounts": [
      { "bank": "SBI",  "accountNo": "XXXX1234", "isPrimary": true },
      { "bank": "HDFC", "accountNo": "XXXX5678", "isPrimary": false }
    ]
  }
}
```

### Postman
```
Method  : GET
URL     : https://demo-banking-server-production.up.railway.app/profile
Headers : Authorization: Bearer <token>
Body    : none
```

### curl
```bash
curl -s https://demo-banking-server-production.up.railway.app/profile \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

---

## Quick Test — Login + All APIs in one shot

```bash
# Step 1 — Login and save token
export TOKEN=$(curl -s -X POST \
  https://demo-banking-server-production.up.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"

# Step 2 — Run all APIs
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN"
curl -s "https://demo-banking-server-production.up.railway.app/banking/transactions?page=1" -H "Authorization: Bearer $TOKEN"
curl -s https://demo-banking-server-production.up.railway.app/wallet/balance -H "Authorization: Bearer $TOKEN"
curl -s https://demo-banking-server-production.up.railway.app/banking/notifications -H "Authorization: Bearer $TOKEN"
curl -s https://demo-banking-server-production.up.railway.app/profile -H "Authorization: Bearer $TOKEN"

# Step 3 — UPI Payment
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/upi/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"upiId":"friend@upi","amount":100}'

# Step 4 — Wallet Topup
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/topup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":500}'

# Step 5 — Logout
curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# Step 6 — Verify token invalid after logout (should return 401)
curl -s https://demo-banking-server-production.up.railway.app/banking/balance \
  -H "Authorization: Bearer $TOKEN"
```

---

---

## Multi-Device Test

```bash
# Login 3 users (3 different devices)
export TOKEN1=$(curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

export TOKEN2=$(curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@test.com","password":"Test@1234"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

export TOKEN3=$(curl -s -X POST https://demo-banking-server-production.up.railway.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@test.com","password":"Admin@1234"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Device1 (demo):           $TOKEN1"
echo "Device2 (user@test.com):  $TOKEN2"
echo "Device3 (admin@test.com): $TOKEN3"

# Check all 3 balances — each user has own balance
echo "--- Balances ---"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN1"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN2"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN3"

# Device1 pays — only Device1 balance drops
curl -s -X POST https://demo-banking-server-production.up.railway.app/banking/upi/pay \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{"upiId":"friend@upi","amount":5000}'

# Verify Device2 and Device3 are unaffected
echo "--- After Device1 payment ---"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN1"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN2"
curl -s https://demo-banking-server-production.up.railway.app/banking/balance -H "Authorization: Bearer $TOKEN3"
```

---

---

## HTTP Status Codes

| Code | Meaning |
|---|---|
| `200` | Success |
| `201` | Created (register) |
| `400` | Bad request — missing fields |
| `401` | Unauthorized — wrong credentials or invalid/expired token |
| `408` | Timeout — `timeout@test` UPI ID |
| `409` | Conflict — username already exists |
| `422` | Payment failed / Insufficient balance |

---

## All Endpoints Summary

| # | Method | Endpoint | Auth | Description |
|---|---|---|---|---|
| 1 | POST | `/auth/login` | No | Login |
| 2 | POST | `/auth/register` | No | Register new account |
| 3 | POST | `/auth/logout` | Yes | Logout |
| 4 | POST | `/auth/verify-pin` | Yes | Verify PIN |
| 5 | GET | `/banking/balance` | Yes | Get account balance |
| 6 | GET | `/account/balance` | Yes | Get balance (spec alias) |
| 7 | POST | `/banking/upi/pay` | Yes | UPI payment |
| 8 | POST | `/payment/upi` | Yes | UPI payment (spec alias) |
| 9 | GET | `/banking/transactions` | Yes | Transaction history |
| 10 | GET | `/transactions` | Yes | Transactions (spec alias) |
| 11 | GET | `/wallet/balance` | Yes | Wallet balance |
| 12 | POST | `/banking/topup` | Yes | Wallet topup |
| 13 | POST | `/wallet/topup` | Yes | Wallet topup (spec alias) |
| 14 | GET | `/banking/notifications` | Yes | Notifications |
| 15 | GET | `/notifications` | Yes | Notifications (spec alias) |
| 16 | GET | `/profile` | Yes | User profile |
