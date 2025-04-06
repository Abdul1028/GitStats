# 🧠 Project Name: **GitStats** – *Your GitHub Story & Stats Visualized*

Analyze, visualize, and compare GitHub profiles — even without login. Generate timelines, get fun insights, compare with friends, and even auto-generate a custom README.md for your profile.

---

## 💻 Tech Stack

| Layer         | Tech Used                        |
|---------------|----------------------------------|
| Backend       | Spring Boot (REST + OAuth + GitHub API) |
| Frontend      | React.js / Vue.js + TailwindCSS |
| Auth          | GitHub OAuth (Spring Security)  |
| DB (optional) | PostgreSQL / MongoDB            |
| Charts/Graphs | Chart.js / D3.js / Recharts     |
| Export Tools  | Puppeteer / html2canvas         |

---

## 🔓 Public Mode (GitHub username input)

### ✅ Features:

- 🔹 **Basic GitHub Info**
  - Avatar, name, bio, join date, followers/following
- 📆 **Repository Analysis**
  - Most starred / forked repos
  - Languages used (pie/bar chart)
  - Repo creation timeline
- 🔥 **Activity Timeline**
  - Approximate contribution events using public `/events`
  - Animated timeline of activity
- 🧙 **Fun Profile Insights**
  - Most used language
  - Most common keywords in repo names
  - Oldest repo vs latest repo
  - Day/time you commit most (approximated)
- 📸 **Developer Card Generator**
  - Downloadable image with top stats
- 👵 **Gists Dashboard**
  - Public gists, file types, created/updated dates

---

## 🔐 Logged-In Mode (GitHub OAuth)

### 🛢️ Extra Authenticated Features:

- Access to private repos
- Access to real contribution graph (via GraphQL)
- Access to user email, full events history, notifications

### ✅ Extra Features:

- 📊 **Advanced Contribution Analysis**
  - Real daily heatmap
  - Longest streak, busiest month, top hour
- 📊 **Cross-Year Comparison**
  - Activity trends across years
- 🔀 **Auto Timeline Sync**
  - Optionally sync your stats weekly (via GitHub Actions)

---

## ✍️ 📄 README Generator (NEW FEATURE)

Help users create an engaging `README.md` for their GitHub profile based on their stats

### Features:

- Pre-fill templates using user data (top languages, pinned projects, contribution stats)
- Offer themes:
  - Minimal
  - Fun (emoji-filled)
  - Dark hacker mode
- Let users choose:
  - Projects to highlight
  - Socials to include
  - Emoji/stat widgets (e.g., GitHub streak counter, visitor badge)
- Optional AI feature:
  - Generate a custom intro bio using GPT (e.g., "I'm a backend dev who loves Spring Boot and pizza 🍕")
- One-click: "Generate & Copy to Clipboard" / "Create README.md in Repo"

---

## 🤝 🕺 Friends Comparison Mode (NEW FEATURE)

Compare GitHub stats between two or more users side by side

### Features:

- Input multiple GitHub usernames
- Side-by-side comparison table:
  - Repos, stars, forks
  - Most used language
  - Activity timeline sync
  - First vs latest repo
  - PRs / Issues / Gists
- Declare fun titles:
  - "Most Starred"
  - "Most Active Night Owl"
  - "Longest GitHub Streak"
- Optionally: Generate a leaderboard (e.g., among classmates)

---

## 🌐 Pages Overview

| Page              | Purpose                                        |
|-------------------|------------------------------------------------|
| **Home Page**     | Search by username, login button              |
| **Dashboard**     | Profile overview, graphs, stats, insights     |
| **Timeline Page** | Visual timeline of repos, commits, events     |
| **Friends Compare** | Input two GitHub usernames, compare stats    |
| **README Generator** | Customize and generate personalized README.md |
| **Settings**      | Logged-in user preferences, auto sync         |

---

## 📦 GitHub APIs Used

### Public:

- `/users/{username}` – profile
- `/users/{username}/repos` – repos
- `/users/{username}/events/public` – activity
- `/users/{username}/gists` – gists

### Authenticated:

- `/user` – profile (private)
- `/user/repos` – private repos
- `/user/events` – private activity
- `/user/starred`
- `/notifications`
- `GraphQL API` – for real contribution heatmap, PR/issue data

