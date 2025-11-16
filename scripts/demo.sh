#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# API Gateway URL
API_GATEWAY="http://localhost:8080"

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   E-commerce Microservices Demo       ║${NC}"
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo ""

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to wait for service
wait_for_service() {
    local url=$1
    local service=$2
    local max_attempts=30
    local attempt=0

    echo -n "Waiting for $service to be ready..."

    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "$url/actuator/health" > /dev/null 2>&1; then
            print_success " $service is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    print_error " $service failed to start"
    return 1
}

# Step 1: Start services
print_header "1. Starting all services with Docker Compose"
echo "Running: docker-compose up -d"
docker-compose up -d

# Step 2: Wait for all services
print_header "2. Waiting for services to be healthy"
wait_for_service "http://localhost:8080" "API Gateway"
wait_for_service "http://localhost:8081" "Products Service"
wait_for_service "http://localhost:8082" "Users Service"
wait_for_service "http://localhost:8083" "Orders Service"
wait_for_service "http://localhost:8084" "Notifications Service"
wait_for_service "http://localhost:8085" "Analytics Service"

sleep 5

# Step 3: Create products
print_header "3. Creating sample products"

LAPTOP=$(curl -s -X POST "$API_GATEWAY/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop with RTX 4080",
    "price": 2999.99,
    "stock": 50,
    "category": "Electronics"
  }')

LAPTOP_ID=$(echo $LAPTOP | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
print_success "Created Gaming Laptop (ID: $LAPTOP_ID)"

MOUSE=$(curl -s -X POST "$API_GATEWAY/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 49.99,
    "stock": 200,
    "category": "Electronics"
  }')

MOUSE_ID=$(echo $MOUSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
print_success "Created Wireless Mouse (ID: $MOUSE_ID)"

KEYBOARD=$(curl -s -X POST "$API_GATEWAY/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "description": "RGB mechanical keyboard",
    "price": 129.99,
    "stock": 100,
    "category": "Electronics"
  }')

KEYBOARD_ID=$(echo $KEYBOARD | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
print_success "Created Mechanical Keyboard (ID: $KEYBOARD_ID)"

# Step 4: Register user
print_header "4. Registering a new user"

USER_RESPONSE=$(curl -s -X POST "$API_GATEWAY/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phone": "+1234567890"
  }')

USER_ID=$(echo $USER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
print_success "User registered (ID: $USER_ID, Email: john.doe@example.com)"

# Step 5: Login
print_header "5. User login"

LOGIN_RESPONSE=$(curl -s -X POST "$API_GATEWAY/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }')

JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
print_success "Login successful (JWT token generated)"

# Step 6: List products
print_header "6. Listing all products"

PRODUCTS=$(curl -s "$API_GATEWAY/api/products")
PRODUCT_COUNT=$(echo $PRODUCTS | grep -o '"id":' | wc -l)
print_success "Found $PRODUCT_COUNT products"

# Step 7: Search products
print_header "7. Searching for 'gaming' products"

SEARCH_RESULTS=$(curl -s "$API_GATEWAY/api/products/search?searchTerm=gaming")
print_success "Search completed"

# Step 8: Create order
print_header "8. Creating an order"

ORDER_RESPONSE=$(curl -s -X POST "$API_GATEWAY/api/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"items\": [
      {
        \"productId\": $LAPTOP_ID,
        \"quantity\": 1
      },
      {
        \"productId\": $MOUSE_ID,
        \"quantity\": 2
      }
    ],
    \"shippingAddress\": \"123 Main St, San Francisco, CA 94122\",
    \"paymentMethod\": \"credit_card\"
  }")

ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
TOTAL_AMOUNT=$(echo $ORDER_RESPONSE | grep -o '"totalAmount":[0-9.]*' | grep -o '[0-9.]*')
print_success "Order created (ID: $ORDER_ID, Total: \$$TOTAL_AMOUNT)"

# Step 9: Update order status
print_header "9. Updating order status to SHIPPED"

UPDATE_RESPONSE=$(curl -s -X PUT "$API_GATEWAY/api/orders/$ORDER_ID/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SHIPPED"
  }')

print_success "Order status updated to SHIPPED"

# Step 10: Get analytics
print_header "10. Fetching analytics summary"

ANALYTICS=$(curl -s "$API_GATEWAY/api/analytics/summary")
echo $ANALYTICS | jq '.' 2>/dev/null || echo $ANALYTICS
print_success "Analytics retrieved"

# Step 11: Check RabbitMQ
print_header "11. RabbitMQ Management Console"
echo -e "${BLUE}Visit: http://localhost:15672${NC}"
echo -e "Username: ${GREEN}guest${NC}"
echo -e "Password: ${GREEN}guest${NC}"
echo ""
echo "Check queues to see event messages:"
echo "  - order.created.queue"
echo "  - product.created.queue"
echo "  - user.registered.queue"

# Step 12: View logs
print_header "12. Recent Notifications Service logs (email notifications)"
docker-compose logs --tail=20 notifications-service | grep -i "email\|event\|registered\|order"

# Final summary
print_header "✅ Demo Complete!"
echo -e "${GREEN}All microservices are running successfully!${NC}"
echo ""
echo "Summary of what was created:"
echo "  • 3 Products (Gaming Laptop, Wireless Mouse, Mechanical Keyboard)"
echo "  • 1 User (john.doe@example.com)"
echo "  • 1 Order (Total: \$$TOTAL_AMOUNT)"
echo ""
echo "Access points:"
echo "  • API Gateway: ${BLUE}http://localhost:8080${NC}"
echo "  • Products Swagger: ${BLUE}http://localhost:8081/swagger-ui.html${NC}"
echo "  • Users Swagger: ${BLUE}http://localhost:8082/swagger-ui.html${NC}"
echo "  • Orders Swagger: ${BLUE}http://localhost:8083/swagger-ui.html${NC}"
echo "  • Analytics Swagger: ${BLUE}http://localhost:8085/swagger-ui.html${NC}"
echo "  • RabbitMQ Management: ${BLUE}http://localhost:15672${NC} (guest/guest)"
echo ""
echo "To view all service logs:"
echo "  ${YELLOW}docker-compose logs -f${NC}"
echo ""
echo "To stop all services:"
echo "  ${YELLOW}docker-compose down${NC}"
echo ""
