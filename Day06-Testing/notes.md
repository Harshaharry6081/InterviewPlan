# 📘 Day 6 – Testing: JUnit & Mockito (May 7th)

> **Goal:** Write production-quality tests that prove your code works.

---

## ✅ Checklist

- [ ] **JUnit 5 annotations and assertions**
  → `@Test`, `@BeforeEach`, `@AfterEach`. Use `assertEquals`, `assertThrows` for validation.
- [ ] **Mockito (mocking, stubbing, verification)**
  → Use `@Mock` for dependencies, `when(...).thenReturn(...)` to stub, and `verify(...)` to check interactions.
- [ ] **Integration testing with @SpringBootTest**
  → Loads the full application context. Use `TestRestTemplate` or `MockMvc` to test API endpoints.
- [ ] **Testcontainers (real DB in tests)**
  → Spins up a Docker container (Postgres/Redis) for tests, ensuring they run against a real environment, not just H2.
- [ ] **Test coverage mindset**
  → Aim for 80%+ coverage, but focus on testing business logic and edge cases, not just getters/setters.

---

## 1. JUnit 5 Basics

```java
@ExtendWith(MockitoExtension.class)  // Activates Mockito
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks  // Creates OrderService and injects the mocks above
    private OrderService orderService;

    // ==================== Setup ====================
    @BeforeEach
    void setUp() {
        // Runs before EACH test method
    }

    @AfterEach
    void tearDown() {
        // Runs after EACH test method
    }

    @BeforeAll
    static void setUpOnce() {
        // Runs ONCE before all tests in this class
    }

    // ==================== Tests ====================

    @Test
    @DisplayName("Should create order successfully when all items are in stock")
    void createOrder_success() {
        // GIVEN (Arrange)
        CreateOrderRequest request = new CreateOrderRequest(List.of(
            new OrderItem("PRODUCT-1", 2)
        ));
        Order expectedOrder = Order.builder().id(1L).status("PENDING").build();
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // WHEN (Act)
        OrderResponse response = orderService.createOrder(request);

        // THEN (Assert)
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());

        // Verify the mock was actually called
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).notify(anyString());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void getOrder_notFound_throwsException() {
        // GIVEN
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> orderService.getOrder(99L)
        );
        assertEquals("Order not found: 99", ex.getMessage());
    }

    @Test
    @DisplayName("Should never call notification service if order creation fails")
    void createOrder_failure_noNotification() {
        // GIVEN
        when(orderRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate"));

        // WHEN
        assertThrows(Exception.class, () -> orderService.createOrder(new CreateOrderRequest()));

        // THEN: verify notification was NEVER sent
        verify(notificationService, never()).notify(anyString());
    }
}
```

---

## 2. Mockito — Key Methods

```java
// Stubbing (define what mock returns)
when(repo.findById(1L)).thenReturn(Optional.of(order));
when(repo.save(any())).thenReturn(savedOrder);
when(service.call()).thenThrow(new RuntimeException("Error"));

// Argument Matchers
when(repo.findByStatus(anyString())).thenReturn(list);
when(repo.findById(eq(5L))).thenReturn(Optional.of(order));

// Do-nothing for void methods
doNothing().when(emailService).send(anyString(), anyString());
doThrow(new RuntimeException()).when(emailService).send(anyString(), anyString());

// Verification
verify(repo).findById(1L);                      // Called exactly once
verify(repo, times(3)).save(any());              // Called 3 times
verify(repo, never()).delete(any());             // Never called
verify(repo, atLeastOnce()).findAll();            // Called at least once

// Argument Captors - Capture what was passed to mock
@Captor
ArgumentCaptor<Order> orderCaptor;

verify(repo).save(orderCaptor.capture());
Order savedOrder = orderCaptor.getValue();
assertEquals("PENDING", savedOrder.getStatus()); // Assert the captured arg
```

---

## 3. Integration Testing with @SpringBootTest

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll(); // Clean state before each test
    }

    @Test
    void createOrder_returnsCreated() {
        CreateOrderRequest request = new CreateOrderRequest(/* ... */);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }
}
```

---

## 4. Testcontainers — Real Databases in Tests

```java
// pom.xml dependency:
// <dependency>
//   <groupId>org.testcontainers</groupId>
//   <artifactId>postgresql</artifactId>
//   <scope>test</scope>
// </dependency>

@SpringBootTest
@Testcontainers  // Activates Testcontainers
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource  // Injects container properties into Spring context
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFindOrder() {
        Order order = new Order("PENDING");
        Order saved = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("PENDING", found.get().getStatus());
    }
}
```

---

## 5. Testing Pyramid & Philosophy

```
        /\
       /  \      ← E2E Tests (few, slow, expensive)
      /----\
     /      \    ← Integration Tests (moderate)
    /--------\
   /          \  ← Unit Tests (many, fast, cheap)
  /____________\
```

**Test Rules:**
- A good unit test should: Be Fast, Independent, Repeatable, Self-validating
- **One assertion per test** (ideally)
- Test behavior, not implementation
- If a test is hard to write, your code is probably hard to use → refactor!

---

## 6. Key Interview Q&A

| Question | Answer |
|---|---|
| What is a mock vs a stub vs a spy? | Stub: returns hardcoded values. Mock: verifies interactions. Spy: partial mock of real object |
| `@Mock` vs `@MockBean`? | `@Mock` = pure Mockito (no Spring). `@MockBean` = Mockito mock registered in Spring context |
| How to test private methods? | Usually you shouldn't. Test through the public API. Refactor if needed |
| What is code coverage? | % of code lines executed during tests. 80%+ is a good target |
| TDD Red-Green-Refactor? | Red: write failing test. Green: write minimum code to pass. Refactor: clean up |
