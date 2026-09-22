# NEURAX — Order Processing System with Live Operations Dashboard

### Acentra Health HackForge — Build to Care 2026
### Problem Statement 2 — HARD

> **Concurrent by Design. Safe by Default. Observable in Real Time.**

---

## 👥 Team NEURAX

| Member | Role |
|---|---|
| **Jose Nishanth M** | Team Leader |
| Nithish K | Team Member |
| Naveen N | Team Member |
| Prathish M | Team Member |
| Chaitanya C H | Team Member |

---

# 📌 Problem

Order processing systems must handle multiple orders simultaneously while ensuring that limited inventory is never oversold.

The major challenges are:

- Processing multiple orders concurrently
- Preventing inventory overselling
- Preventing negative inventory
- Handling out-of-stock orders
- Recovering from transient failures
- Implementing bounded retries
- Moving permanently failed orders to a Dead-Letter Queue
- Providing live visibility into order processing
- Persisting orders and inventory

---

# 💡 Our Solution

NEURAX is a concurrency-safe order processing platform built with **Spring Boot, PostgreSQL, and React**.

The system separates:

1. **Order acceptance**
2. **Concurrent processing**
3. **Inventory protection**
4. **Failure handling**
5. **Retry processing**
6. **Dead-Letter Queue management**
7. **Live operational monitoring**

The most important design decision is that **inventory correctness is enforced at the database level**, rather than relying only on Java application logic.

---

# 🏗️ Architecture

```text
                         ┌───────────────────────┐
                         │    React Dashboard    │
                         │  Live Operations UI   │
                         └───────────┬───────────┘
                                     │
                                  REST API
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │    Spring Boot API    │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │     Order Service     │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │    Thread Pool        │
                         │ 10 Workers / 500 Queue│
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │   Order Processor     │
                         │ Retry + Failure Logic │
                         └───────────┬───────────┘
                                     │
                         ┌───────────┴───────────┐
                         │                       │
                         ▼                       ▼
                ┌─────────────────┐     ┌─────────────────┐
                │   PostgreSQL    │     │ Dead-Letter     │
                │                 │     │ Queue            │
                │ Products        │     │ failed_orders    │
                │ Orders          │     └─────────────────┘
                └─────────────────┘
