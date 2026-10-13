# 🏍️ MPP - Motociclism Racing Management

University project for the **Methods and Programming Paradigms** course.
A complete cross-platform application for managing motorcycle racing events, implemented in Java and C#.

---

## 📋 Description

The application manages the following entities: **Race (Cursa)**, **Participant**, **Registration (Inscriere)**, **User**.
Database: **SQLite** (`motociclism.db`).

---

## 🏗️ Project Structure

### Java (IntelliJ + Gradle multi-module)
```
mpp-proiect-java-dodudaniel/
├── model/          # Entities: Cursa, Participant, Inscriere, User
├── repository/     # Database access (JDBC + Hibernate)
├── services/       # Business logic
├── networking/     # TCP Server (port 55555)
├── server/         # Server startup
└── rest/           # REST API Spring Boot (port 8090)
```

### C# (Rider - Windows Forms)
```
mpp-proiect-csharp-dodudaniel/
├── Client/         # Windows Forms client
└── RestClientTest/ # REST API test program
```

### React (Vite)
```
MPPLab7React/cursa-web/
├── src/
│   ├── components/
│   │   ├── CursaList.jsx
│   │   ├── CursaItem.jsx
│   │   ├── CursaFilter.jsx
│   │   └── CursaForm.jsx
│   ├── services/
│   │   └── cursaService.js
│   └── App.jsx
```

---

## 🔬 Implemented Labs

### Lab5 — Cross-platform Distributed Applications
- Java TCP server on port **55555** (TCP + JSON)
- C# Windows Forms client communicating with the Java server
- Protocol: JSON over TCP using Jackson ObjectMapper
- Operations: authentication, race listing, participant registration, team modification

### Lab6 — ORM with Hibernate
- Hibernate 6.4.4 with SQLiteDialect
- Mapped entities: **Cursa** and **Participant**
- New repositories: `CursaHibernateRepository`, `ParticipantHibernateRepository`
- SQL logged to console via `show_sql=true`

### Lab7 — REST Services
- REST API built with **Spring Boot 3.2.5** on port **8090**
- Endpoints for the **Cursa** entity:

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/curse` | Get all races |
| GET | `/api/curse/{id}` | Get race by ID |
| GET | `/api/curse/filter?capacitate=X` | Filter by engine capacity |
| POST | `/api/curse` | Create new race |
| PUT | `/api/curse/{id}` | Update race |
| DELETE | `/api/curse/{id}` | Delete race |

- Java test program using `RestClient` + `LoggingInterceptor`
- C# test program using `HttpClient` + `LoggingHandler`

### Lab8 — React Web Client
- React + Vite web application running on port **5173**
- Separate components: CursaList, CursaItem, CursaFilter, CursaForm
- Operations: view, filter, add, edit, delete

### Lab9 — WebSockets
- Real-time notifications using **STOMP over WebSocket**
- All connected clients receive notifications on add/update/delete
- Server: `WebSocketConfig.java` + `SimpMessagingTemplate` in controller
- Client: `@stomp/stompjs` + `sockjs-client`

---

## 🚀 How to Run

### Prerequisites
- Java 17
- .NET 8
- Node.js v24+
- IntelliJ IDEA
- Rider
- SQLite (database included in project)

### 1. Start TCP Server (Lab5)
```bash
# In IntelliJ, Run the StartServer class from the server module
# Server starts on port 55555
```

### 2. Start C# Client (Lab5)
```bash
# In Rider, Run the Client project
# Windows Forms UI will open
```

### 3. Start REST API (Lab7, Lab8, Lab9)
```bash
# In IntelliJ, Run the RestApp class from the rest module
# Server starts on port 8090
```

### 4. Start React Client (Lab8, Lab9)
```bash
cd MPPLab7React/cursa-web
npm install
npm run dev
# App runs at http://localhost:5173
```

### 5. Java Test Program (Lab7)
```bash
# In IntelliJ, Run the RestClientTest class from the rest module
# REST server must be running first
```

### 6. C# Test Program (Lab7)
```bash
# In Rider, Run the RestClientTest project
# REST server must be running first
```

---

## 🛠️ Technologies Used

| Technology | Version | Usage |
|------------|---------|-------|
| Java | 17 | Server, REST API |
| Spring Boot | 3.2.5 | REST API, WebSockets |
| Hibernate | 6.4.4 | ORM |
| SQLite | 3.45.3 | Database |
| Jackson | 2.17 | JSON Serialization |
| C# / .NET | 8.0 | Windows Forms Client, REST Test |
| React | 18 | Web Client |
| Vite | 5 | React Build Tool |
| STOMP / SockJS | - | WebSockets |

---

## 👤 Author

**Dodu Daniel**
University — Faculty of Mathematics and Computer Science
Course: Methods and Programming Paradigms
