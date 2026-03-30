const express = require('express');
const cors = require('cors');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');

const app = express();
app.use(cors());
app.use(express.json());

// ─── Post-commit mode toggle ───────────────────────────────────────────────────
// Run with:  POST_COMMIT=true node server.js   → enables intentional bugs
//            node server.js                    → all APIs work normally
const POST_COMMIT = process.env.POST_COMMIT === 'true';
console.log(`Mode: ${POST_COMMIT ? '⚠️  POST_COMMIT (bugs active)' : '✅ NORMAL (all APIs working)'}`);

// ─── Sessions ─────────────────────────────────────────────────────────────────
const activeSessions = {};  // token → { username, name, accountNo }

// ─── Per-user state (each device/user gets their own balance & transactions) ──
const USER_STATE = {};

function getState(username) {
  if (!USER_STATE[username]) {
    USER_STATE[username] = {
      balance: 24350.00,
      walletBalance: 5000.00,
      lowBalancePending: false,
      transactions: [
        ...Array.from({ length: 5 }, (_, i) => ({
          id: uuidv4(), type: 'debit',
          amount: -(Math.floor(Math.random() * 900) + 100),
          description: ['Swiggy Order', 'Amazon Pay', 'Zomato', 'PhonePe', 'BookMyShow'][i],
          upiId: ['swiggy@okaxis', 'amazon@okicici', 'zomato@ybl', 'merchant@ybl', 'bms@okhdfc'][i],
          date: new Date(Date.now() - i * 86400000).toISOString(), status: 'SUCCESS',
        })),
        ...Array.from({ length: 5 }, (_, i) => ({
          id: uuidv4(), type: 'credit',
          amount: Math.floor(Math.random() * 2000) + 500,
          description: ['Salary Credit', 'Refund - Flipkart', 'Google Pay', 'NEFT Credit', 'UPI Received'][i],
          upiId: ['salary@corp', 'flipkart@ybl', 'gpay@okaxis', 'neft@sbi', 'friend@paytm'][i],
          date: new Date(Date.now() - (i + 5) * 86400000).toISOString(), status: 'SUCCESS',
        })),
        ...Array.from({ length: 5 }, (_, i) => ({
          id: uuidv4(), type: 'debit',
          amount: -(Math.floor(Math.random() * 500) + 50),
          description: ['Electricity Bill', 'DTH Recharge', 'Mobile Recharge', 'Water Bill', 'Gas Bill'][i],
          upiId: ['bescom@ybl', 'tatasky@okicici', 'airtel@airtel', 'bwssb@ybl', 'igl@ybl'][i],
          date: new Date(Date.now() - (i + 10) * 86400000).toISOString(), status: 'SUCCESS',
        })),
      ],
    };
  }
  return USER_STATE[username];
}

// ─── Dummy users ──────────────────────────────────────────────────────────────
const USERS = {
  'user@test.com': { password: 'Test@1234',   pin: '1234', name: 'Ravi Kumar',    accountNo: 'XX4521' },
  'admin@test.com': { password: 'Admin@1234', pin: '0000', name: 'Priya Sharma',  accountNo: 'XX9988' },
  'demo':           { password: 'password123', pin: '0000', name: 'Demo User',    accountNo: 'XX0000' },
};

// ─── Auth guard middleware ─────────────────────────────────────────────────────
function authGuard(req, res, next) {
  const token = req.headers['authorization']?.split(' ')[1];
  if (!token || !activeSessions[token]) {
    return res.status(401).json({ success: false, message: 'Unauthorized. Please login again.' });
  }
  req.user = activeSessions[token];
  next();
}

// ══════════════════════════════════════════════════════════════════════════════
// SHARED HANDLERS — single implementation, mounted on multiple paths
// ══════════════════════════════════════════════════════════════════════════════

function handleLogin(req, res) {
  const { username, password } = req.body;
  const user = USERS[username];

  if (!user || user.password !== password) {
    return res.status(401).json({ success: false, message: 'Invalid credentials' });
  }

  const token = uuidv4();
  activeSessions[token] = { username, name: user.name, accountNo: user.accountNo };
  getState(username); // initialise per-user state on first login

  // ⚠️  POST_COMMIT vulnerability: token written to log file (caught by compliance agent)
  if (POST_COMMIT) {
    fs.appendFileSync('session.log', `[${new Date().toISOString()}] token=${token} user=${username}\n`);
  }

  res.json({ success: true, token, user: { name: user.name, accountNo: user.accountNo, username } });
}

function handleGetBalance(req, res) {
  const state = getState(req.user.username);

  // ⚠️  POST_COMMIT bug: renames `balance` → `availableBalance` — breaks Android & tests
  const balanceField = POST_COMMIT ? 'availableBalance' : 'balance';

  res.json({
    success: true,
    [balanceField]: state.balance,
    currency: 'INR',
    accountNo: req.user.accountNo,
    accountHolder: req.user.name,
    lastUpdated: new Date().toISOString(),
  });
}

function handleUpiPay(req, res) {
  const { upiId, amount, remarks } = req.body;
  const state = getState(req.user.username);

  if (!upiId || !amount) {
    return res.status(400).json({ success: false, message: 'UPI ID and amount are required' });
  }
  if (upiId === 'fail@test') {
    return res.status(422).json({
      success: false,
      message: 'Payment failed. Beneficiary account is inactive.',
      errorCode: 'UPI_FAILURE_001',
    });
  }
  if (upiId === 'timeout@test') {
    return setTimeout(() => {
      res.status(408).json({ success: false, message: 'Request timed out', errorCode: 'TIMEOUT' });
    }, 30000);
  }
  if (parseFloat(amount) > state.balance) {
    return res.status(422).json({ success: false, message: 'Insufficient balance', errorCode: 'INSUFFICIENT_FUNDS' });
  }

  // If previous transaction left balance below 5000, top-up to 10000 before this transaction
  if (state.lowBalancePending) {
    const topupAmount = 10000 - state.balance;
    state.balance = 10000;
    state.transactions.unshift({
      id: uuidv4(), type: 'credit', amount: topupAmount,
      description: 'Auto Balance Top-Up',
      date: new Date().toISOString(), status: 'SUCCESS',
      referenceId: `TXN${Date.now()}`,
    });
    state.lowBalancePending = false;
  }

  state.balance -= parseFloat(amount);
  const txn = {
    id: uuidv4(), type: 'debit',
    amount: -parseFloat(amount),
    description: remarks || `UPI Payment to ${upiId}`,
    upiId, date: new Date().toISOString(), status: 'SUCCESS',
    referenceId: `TXN${Date.now()}`,
  };
  state.transactions.unshift(txn);

  // Flag for top-up on next transaction if balance dropped below 5000
  if (state.balance < 5000) {
    state.lowBalancePending = true;
  }

  res.json({ success: true, status: 'SUCCESS', message: 'Payment successful', transaction: txn, newBalance: state.balance });
}

function handleGetTransactions(req, res) {
  const state = getState(req.user.username);
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 5;
  const typeFilter = req.query.type;

  const filtered = typeFilter
    ? state.transactions.filter(t => t.type === typeFilter)
    : state.transactions;

  const total = filtered.length;
  const totalPages = Math.ceil(total / limit);
  const start = (page - 1) * limit;

  res.json({
    success: true,
    transactions: filtered.slice(start, start + limit),
    pagination: { page, limit, total, totalPages, hasNext: page < totalPages },
  });
}

function handleWalletTopup(req, res) {
  const { amount } = req.body;
  const state = getState(req.user.username);

  if (!amount || parseFloat(amount) <= 0) {
    return res.status(400).json({ success: false, message: 'Invalid top-up amount' });
  }
  if (parseFloat(amount) > state.balance) {
    return res.status(422).json({ success: false, message: 'Insufficient bank balance for top-up' });
  }

  state.walletBalance += parseFloat(amount);
  state.balance -= parseFloat(amount);

  res.json({
    success: true,
    message: `Wallet topped up with ₹${amount}`,
    walletBalance: state.walletBalance,
    balance: state.balance,
  });
}

function handleGetNotifications(req, res) {
  const state = getState(req.user.username);
  const notifications = [];

  if (state.balance < 5000) {
    notifications.push({
      id: uuidv4(), type: 'LOW_BALANCE', title: 'Low Balance Alert',
      body: `Your account balance ₹${state.balance.toFixed(2)} is below ₹5,000`,
      deepLink: 'banking://wallet/topup',
      timestamp: new Date().toISOString(), read: false,
    });
  }

  notifications.push({
    id: uuidv4(), type: 'INFO', title: 'Security Tip',
    body: 'Never share your PIN or OTP with anyone.',
    deepLink: 'banking://profile/security',
    timestamp: new Date(Date.now() - 3600000).toISOString(), read: true,
  });

  res.json({ success: true, notifications, unreadCount: notifications.filter(n => !n.read).length });
}

// ══════════════════════════════════════════════════════════════════════════════
// SPEC ROUTES  (original paths from the API spec)
// ══════════════════════════════════════════════════════════════════════════════

app.post('/auth/login',           handleLogin);
app.get('/account/balance',       authGuard, handleGetBalance);
app.post('/payment/upi',          authGuard, handleUpiPay);
app.get('/transactions',          authGuard, handleGetTransactions);
app.post('/wallet/topup',         authGuard, handleWalletTopup);
app.get('/notifications',         authGuard, handleGetNotifications);

// ══════════════════════════════════════════════════════════════════════════════
// ANDROID APP ALIAS ROUTES  (paths used by the Android app & Postman tests)
// ══════════════════════════════════════════════════════════════════════════════

app.get('/banking/balance',       authGuard, handleGetBalance);
app.post('/banking/upi/pay',      authGuard, handleUpiPay);
app.get('/banking/transactions',  authGuard, handleGetTransactions);
app.post('/banking/topup',        authGuard, handleWalletTopup);
app.get('/banking/notifications', authGuard, handleGetNotifications);

// ══════════════════════════════════════════════════════════════════════════════
// EXTRA ENDPOINTS
// ══════════════════════════════════════════════════════════════════════════════

// POST /auth/register
app.post('/auth/register', (req, res) => {
  const { username, password, name } = req.body;

  if (!username || !password || !name) {
    return res.status(400).json({ success: false, message: 'username, password and name are required' });
  }
  if (USERS[username]) {
    return res.status(409).json({ success: false, message: 'Account already exists with this username' });
  }

  const accountNo = 'XX' + Math.floor(1000 + Math.random() * 9000);
  USERS[username] = { password, pin: '0000', name, accountNo };

  const token = uuidv4();
  activeSessions[token] = { username, name, accountNo };
  getState(username); // initialise balance & transactions for new account

  res.status(201).json({ success: true, token, user: { name, accountNo, username } });
});

app.post('/auth/verify-pin', authGuard, (req, res) => {
  const user = USERS[req.user.username];
  if (user.pin !== req.body.pin) {
    return res.status(401).json({ success: false, message: 'Invalid PIN' });
  }
  res.json({ success: true, message: 'PIN verified' });
});

app.post('/auth/logout', authGuard, (req, res) => {
  const token = req.headers['authorization']?.split(' ')[1];
  delete activeSessions[token];
  res.json({ success: true, message: 'Logged out successfully' });
});

app.get('/wallet/balance', authGuard, (req, res) => {
  const state = getState(req.user.username);
  res.json({ success: true, walletBalance: state.walletBalance, currency: 'INR' });
});

app.get('/profile', authGuard, (req, res) => {
  const user = USERS[req.user.username];
  res.json({
    success: true,
    profile: {
      name: user.name, username: req.user.username, accountNo: user.accountNo,
      linkedAccounts: [
        { bank: 'SBI',  accountNo: 'XXXX1234', isPrimary: true },
        { bank: 'HDFC', accountNo: 'XXXX5678', isPrimary: false },
      ],
    },
  });
});

// ── Start server — listen on 0.0.0.0 so real devices on same WiFi can connect ─
const PORT = process.env.PORT || 8080;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Banking Mock API running on http://0.0.0.0:${PORT}`);
  console.log(`\nAvailable users:`);
  console.log(`  demo            / password123`);
  console.log(`  user@test.com   / Test@1234`);
  console.log(`  admin@test.com  / Admin@1234`);
  console.log(`\nSpec APIs:`);
  console.log(`  POST /auth/login`);
  console.log(`  GET  /account/balance`);
  console.log(`  POST /payment/upi`);
  console.log(`  GET  /transactions`);
  console.log(`  POST /wallet/topup`);
  console.log(`  GET  /notifications`);
});
