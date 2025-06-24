# factory-pattern
When to Use Factory Pattern
Use it when:

You have multiple related classes that implement the same interface
You want to hide the complex object creation logic
You need to create objects based on some condition or input
You want to make your code more flexible and maintainable

Real-world scenarios:

Creating different types of database connections (MySQL, PostgreSQL, Oracle)
Creating different payment processors (PayPal, Stripe, Square)
Creating different file parsers (JSON, XML, CSV)
Creating different notification services (Email, SMS, Push)

Benefits of Factory Pattern

Loose Coupling: Client code doesn't depend on concrete classes
Easy Extension: Adding new shapes/types doesn't require changing existing code
Centralized Creation Logic: All object creation logic is in one place
Configuration-Based: Can create objects based on configuration files
Testing: Easy to mock and test
