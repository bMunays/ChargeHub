# ChargeHub 🔋

ChargeHub is a custom, offline-first Android application designed to manage high-volume mobile phone charging stations at large events, off-grid gatherings, and churches.
I started working on this app at a friend's request.

## The Problem
Managing dozens of phones checking in and out for a charging service using paper logbooks creates massive queues, misplaced devices, and disputes over phone damage or charging durations.

## The Solution
ChargeHub uses a physical slot-and-token system paired with a rapid-entry SQLite backend to achieve ~20-second check-ins. It relies on the token number as the primary ID, uses the device camera to instantly log physical condition upon entry, and automatically calculates billing based on charge time.

## Core Features
* **Grid Dashboard:** Visual representation of 30 physical charging slots.
* **Rapid Check-In:** Maps a phone to a physical slot and captures an instantaneous top-down photo to prove device condition.
* **Automated Billing:** Calculates fees based on exact timestamps.
* **Offline First:** Fully functional without internet access using an SQLite database.
* **Revenue Tracking:** Basic statistical dashboard for daily gross revenue and active sessions.

## Tech Stack
* **Language:** Java
* **UI:** XML Views / Material Design
* **Database:** SQLite (SQLiteOpenHelper)
* **Architecture:** MVC / Standard Android Activity Lifecycle

## Development Workflow
This project follows a strict feature-branch workflow. All active development occurs on isolated `feature/*` branches and is merged into `main` only upon completion and testing.
