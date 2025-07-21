\# E-Commerce Backend API



This is a comprehensive backend API for an e-commerce platform, built with Spring Boot. It includes features for user authentication, product management, shopping cart functionality, and order processing.



\## 🚀 Features



\* \*\*User Authentication\*\*: Secure user registration and login using JWT (JSON Web Tokens).

\* \*\*Role-Based Access Control\*\*: Differentiates between regular users (`ROLE\_USER`) and administrators (`ROLE\_ADMIN`), with specific API endpoints protected based on user roles.

\* \*\*Product \& Category Management\*\*: Full CRUD (Create, Read, Update, Delete) functionality for products and categories (Admin only).

\* \*\*Shopping Cart\*\*: Authenticated users can add, update, view, and remove items from their shopping cart.

\* \*\*Order Management\*\*: Users can place orders from their cart. Admins can view all orders and update their status.



\## 🛠️ Technologies Used



\* \*\*Java 17\*\*

\* \*\*Spring Boot 3.5.3\*\*

\* \*\*Spring Web\*\*: For building RESTful APIs.

\* \*\*Spring Data JPA\*\*: For data persistence.

\* \*\*Spring Security\*\*: For authentication and authorization.

\* \*\*MySQL\*\*: As the relational database.

\* \*\*Lombok\*\*: To reduce boilerplate code.

\* \*\*Maven\*\*: For project dependency management.

\* \*\*JJWT\*\*: For creating and validating JSON Web Tokens.



\## ⚙️ Setup and Installation



To run this project locally, you will need to have Java 17 and Maven installed.



1\.  \*\*Clone the repository:\*\*

&nbsp;   ```bash

&nbsp;   git clone \[https://github.com/itachi21113/ecom-backend.git](https://github.com/itachi21113/ecom-backend.git)

&nbsp;   ```

2\.  \*\*Navigate to the project directory:\*\*

&nbsp;   ```bash

&nbsp;   cd ecom-backend

&nbsp;   ```

3\.  \*\*Database Configuration:\*\*

&nbsp;   \* Make sure you have a MySQL database created (e.g., `ecom\_db`).

&nbsp;   \* Update the `src/main/resources/application.properties` file with your database URL, username, and password.



4\.  \*\*Run the application:\*\*

&nbsp;   ```bash

&nbsp;   ./mvnw spring-boot:run

&nbsp;   ```



The API will be available at `http://localhost:8080`.




