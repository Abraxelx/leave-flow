# LeaveFlow

A modern, simple and beautiful desktop application for managing employee leaves, official holidays, and leave reports. Built with JavaFX, SQLite, and iText.

## 🚀 Features
- **Employee Management:** Add, list, and delete employees
- **Leave Management:** Add, filter, and export leave records
- **Official Holidays:** Fetch Turkish holidays from Google Calendar API or enter manually
- **PDF Export:** Export leave reports with full Turkish and Unicode support (Noto Sans, Roboto fonts)
- **Quick Add Leave:** Fast leave entry with automatic calculation (weekends & holidays excluded)
- **Statistics:** See total, average, and per-employee leave stats
- **Modern UI:** Tab-based, minimal, responsive, and emoji-powered interface

## 🖥️ Technologies
- **Java 22**
- **JavaFX** (UI)
- **SQLite** (local database)
- **iText** (PDF export)
- **Google Fonts** (Noto Sans, Roboto)
- **GitHub Actions** (optional, for CI/CD)

## ⚡ Quick Start
```bash
# Clone the repo
 git clone https://github.com/yourusername/leave-flow.git
 cd leave-flow

# Build the project
mvn clean compile

# Run the app
mvn exec:java -Dexec.mainClass="com.halilsahin.leaveflow.Main"
```

## 📦 Project Structure
- `src/main/java/` — Java source code
- `src/main/resources/` — FXML views, fonts, configs
- `leaveflow.db` — SQLite database
- `fonts/` — PDF fonts (Noto Sans, Roboto)

## 📝 Configuration
- **API Key:** Set your Google Calendar API key in `src/main/resources/config.properties` for holiday fetching.
- **Fonts:** Noto Sans and Roboto are included for full Turkish and Unicode support in PDFs.

## 📄 License
This project is licensed under the MIT License.

---

**LeaveFlow** — Simple, beautiful, and powerful leave management for your team!
