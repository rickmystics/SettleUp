<div align="center">
</div>

# SettleUp 💸

> **One shared ledger. Zero awkward money conversations.**

[cite_start]SettleUp is a mobile-first social money ledger designed for friends, flatmates, and small groups to track informal debts, split bills, and settle dues seamlessly—without the friction of buried WhatsApp chats or abandoned spreadsheets[cite: 5, 14]. [cite_start]Positioned at the intersection of personal finance and social interaction, it offers a lightweight, elegant solution to see real-time balances and nudge friends without the judgment[cite: 6, 16].

---

## 🚀 Key Features

### 1. Unified Dashboard & Lightning-Fast Ledger
* [cite_start]**Real-Time Financial Pulse:** See total to receive, total owed, and net figures at a glance[cite: 37].
* [cite_start]**Quick-Action Entries:** Log *I Lent*, *I Borrowed*, *Split Expenses*, or *Settle Payments* in 2 taps or less[cite: 26, 28].
* [cite_start]**Color-Coded Accountability:** Instantly view who owes who with intuitive status indicators[cite: 39].

### 2. Smart Settlement Engine (Graph Reduction)
Why make 5 transactions when 1 will do? [cite_start]SettleUp uses a custom debt-simplification graph algorithm to reduce $N$ payment transactions down to the absolute theoretical minimum[cite: 55].
* [cite_start]*Example:* $A \rightarrow B$ ₹500, $B \rightarrow C$ ₹500, and $C \rightarrow A$ ₹500 simplifies automatically to **zero net transfers**[cite: 56].

### 3. Persistent Groups
[cite_start]Perfect for flatmates managing shared utilities, travel buddies booking trips, or couples tracking joint expenses[cite: 23, 47]. [cite_start]Supports equal, exact, or percentage-based splitting[cite: 50].

### 4. Tone-Aware Reminder System 💬
[cite_start]Frictionless, respectful nudges to avoid the awkwardness of debt collection[cite: 61]. [cite_start]Seamlessly export templates via WhatsApp deep-linking, SMS, or copy-to-clipboard[cite: 63]:
* [cite_start]**Friendly:** *"Hey! Just a quick reminder 😄 ₹300 is still pending."* [cite: 62]
* [cite_start]**Professional:** *"Reminder: ₹300 pending from our dinner on 12 Jun."* [cite: 62]
* [cite_start]**Playful:** *"My wallet misses its ₹300 🥲"* [cite: 62]

---

## 🛠️ Architecture & Core Data Model

[cite_start]The application architecture relies on a bidirectional friend mapping and a strict tracking ledger[cite: 76]. [cite_start]Below is the foundational entity setup[cite: 76]:

* [cite_start]**Users:** Phone-number authenticated profiles linked to UPI IDs for settlement mapping[cite: 76].
* [cite_start]**Transactions:** Explicit states tracing `lent`, `borrowed`, `split`, and `settlement` variations across categories (`Food`, `Rent`, `Travel`, etc.)[cite: 33, 76].
* [cite_start]**Groups & Group Expenses:** Relational schema supporting persistent member matrices and split vectors[cite: 76].

### Non-Functional Guardrails
* [cite_start]**Performance:** Dashboard load speed under 1 second; transaction saves under 500ms[cite: 70].
* **Security:** AES-256 encryption at rest; [cite_start]TLS 1.3 encryption in transit[cite: 70].
* [cite_start]**Reliability:** Built with offline-first tracking support to cache local ledger logs until reconnected[cite: 70].

---

## 🗺️ Roadmap & Ecosystem

* [cite_start]**v1.0 (MVP):** Phone OTP login, core 4-way transaction logging, friend ledgers, and basic WhatsApp/copy reminders[cite: 80, 81, 82, 84, 86].
* [cite_start]**v2.0 (Growth):** Full groups implementation, Smart Settlement Engine, analytics, and native UPI payment integration[cite: 90, 91, 92, 93].
* [cite_start]**v3.0 (Scale):** AI-powered reminder optimization, subscription tracking, and multi-currency group frameworks[cite: 98, 100, 102].
