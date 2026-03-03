# Online Auction Platform

A full-featured online auction platform built with Spring Boot, Thymeleaf, and H2 database. This application allows users to create, browse, and bid on auction items in real-time.

## Features

- 🔨 **Auction Management** - Create, edit, and manage auction items with start/end times
- 💰 **Bidding System** - Real-time bidding with automatic auction end scheduling
- 👤 **User Authentication** - Secure registration and login with Spring Security
- 💳 **Payment Integration** - Stripe payment gateway integration
- 📧 **Email Notifications** - Automated email alerts for bidding and auction updates
- 💬 **Real-time Chat** - WebSocket-based chat for bidder communication
- 👑 **Admin Dashboard** - Manage users, items, and monitor auctions
- 🎨 **Modern UI** - Clean, responsive Thymeleaf templates

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.1.0
- **Database:** Spring Data JPA, H2 Database
- **Security:** Spring Security
- **Frontend:** Thymeleaf Templates, HTML5, CSS3
- **Real-time:** WebSocket
- **Payments:** Stripe API

## Project Structure

```
src/
├── main/
│   ├── java/com/github/anuawchar/onlineauction/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/     # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── service/         # Business logic
│   │   ├── utils/           # Utility classes
│   │   └── validator/      # Custom validators
│   └── resources/
│       ├── static/          # CSS, images
│       └── templates/       # Thymeleaf templates
└── test/                    # Test classes
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/anuja-awchar/online-auction-platform.git
```

2. Navigate to the project directory:
```bash
cd online-auction-platform
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. Open your browser and navigate to:
```
http://localhost:8080
```

### Default Credentials

The application comes with pre-configured test accounts:

| Role  | Email             | Password |
|-------|-------------------|----------|
| Admin | admin@example.com | admin123 |
| User  | user@example.com  | user123  |

## Configuration

### Database Configuration

The application uses H2 in-memory database by default. To configure a different database, update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./auctiondb
spring.datasource.username=sa
spring.datasource.password=
```

### Email Configuration

Configure SMTP for email notifications:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-password
```

### Stripe Configuration

Add your Stripe API keys:

```properties
stripe.api.key=sk_test_your_stripe_key
stripe.publishable.key=pk_test_your_stripe_key
```

## Key Components

### Auction Item Entity
- Title, description, and starting price
- Image upload support
- Start and end time validation
- Automatic status updates

### Bidding System
- Real-time bid tracking
- Automatic auction end at scheduled time
- Bid history per item
- Outbid notifications

### User Management
- Registration and login
- Profile management
- Role-based access control (Admin, User)
- Bid history tracking

## Screenshots

The platform includes:
- Home page with featured auctions
- Item detail pages with bidding
- User profile and bid history
- Admin dashboard for management
- Real-time chat interface

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Author

- **Anuja Awchar** - [anuja-awchar](https://github.com/anuja-awchar)

---

⭐ Star this repository if you found it helpful!

