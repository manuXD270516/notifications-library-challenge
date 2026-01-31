#!/bin/bash

# Testing Script - Notifications Library
# This script runs all test scenarios

echo "================================================="
echo "  Notifications Library - Testing Suite"
echo "================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to run test
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    echo -e "${YELLOW}Running: ${test_name}${NC}"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✅ PASSED: ${test_name}${NC}\n"
        ((TESTS_PASSED++))
        return 0
    else
        echo -e "${RED}❌ FAILED: ${test_name}${NC}\n"
        ((TESTS_FAILED++))
        return 1
    fi
}

# Check prerequisites
echo "Checking prerequisites..."
echo "================================================="

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✅ Java found: ${JAVA_VERSION}${NC}"
else
    echo -e "${YELLOW}⚠️  Java not found (Docker can be used instead)${NC}"
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -1)
    echo -e "${GREEN}✅ Maven found: ${MVN_VERSION}${NC}"
else
    echo -e "${YELLOW}⚠️  Maven not found (Docker can be used instead)${NC}"
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✅ Docker found: ${DOCKER_VERSION}${NC}"
else
    echo -e "${RED}❌ Docker not found${NC}"
fi

echo ""
echo "================================================="
echo "  Starting Test Scenarios"
echo "================================================="
echo ""

# Scenario 1: Maven Build
if command -v mvn &> /dev/null && command -v javac &> /dev/null; then
    echo "SCENARIO 1: Maven Build"
    echo "================================================="
    
    run_test "Clean compile" "mvn clean compile -s settings.xml -q"
    run_test "Package JAR" "mvn clean package -DskipTests -s settings.xml -q"
    run_test "Verify JAR exists" "test -f target/notifications-library-1.0.0.jar"
    
    if [ -f target/notifications-library-1.0.0.jar ]; then
        JAR_SIZE=$(ls -lh target/notifications-library-1.0.0.jar | awk '{print $5}')
        echo -e "  JAR Size: ${JAR_SIZE}"
    fi
    
    echo ""
else
    echo -e "${YELLOW}Skipping Maven build tests (JDK not available)${NC}\n"
fi

# Scenario 2: Unit Tests
if command -v mvn &> /dev/null && command -v javac &> /dev/null; then
    echo "SCENARIO 2: Unit Tests"
    echo "================================================="
    
    run_test "All unit tests" "mvn test -s settings.xml -q"
    
    if [ -d target/surefire-reports ]; then
        echo "Test reports available in: target/surefire-reports/"
        TEST_COUNT=$(grep -r "Tests run:" target/surefire-reports/*.txt | head -1 | awk '{print $3}' | tr -d ',')
        echo "  Total tests: ${TEST_COUNT}"
    fi
    
    echo ""
else
    echo -e "${YELLOW}Skipping unit tests (JDK not available)${NC}\n"
fi

# Scenario 3: Docker Build
if command -v docker &> /dev/null; then
    echo "SCENARIO 3: Docker Build"
    echo "================================================="
    
    run_test "Docker build" "docker-compose build"
    run_test "Docker image exists" "docker images notifications-library:1.0.0 -q | grep -q ."
    
    if docker images notifications-library:1.0.0 -q | grep -q .; then
        IMAGE_SIZE=$(docker images notifications-library:1.0.0 --format "{{.Size}}")
        echo -e "  Image Size: ${IMAGE_SIZE}"
    fi
    
    echo ""
else
    echo -e "${RED}Skipping Docker tests (Docker not available)${NC}\n"
fi

# Scenario 4: Docker Run
if command -v docker &> /dev/null && docker images notifications-library:1.0.0 -q | grep -q .; then
    echo "SCENARIO 4: Docker Execution"
    echo "================================================="
    
    run_test "Container runs" "docker-compose up --exit-code-from notifications-library"
    run_test "JAR in container" "docker run --rm notifications-library:1.0.0 sh -c 'test -f /app/notifications-library.jar'"
    run_test "Examples in container" "docker run --rm notifications-library:1.0.0 sh -c 'test -f /app/examples/NotificationExamples.java'"
    
    echo ""
else
    echo -e "${YELLOW}Skipping Docker execution tests${NC}\n"
fi

# Scenario 5: Code Quality Checks
echo "SCENARIO 5: Code Quality"
echo "================================================="

run_test "Source files exist" "test -d src/main/java/com/novacomp/notifications"
run_test "Test files exist" "test -d src/test/java/com/novacomp/notifications"
run_test "Documentation exists" "test -f README.md && test -f ARCHITECTURE.md"

# Count files
JAVA_FILES=$(find src/main/java -name "*.java" | wc -l)
TEST_FILES=$(find src/test/java -name "*.java" 2>/dev/null | wc -l)
DOC_FILES=$(ls *.md 2>/dev/null | wc -l)

echo "  Java files: ${JAVA_FILES}"
echo "  Test files: ${TEST_FILES}"
echo "  Documentation files: ${DOC_FILES}"
echo ""

# Scenario 6: Git History
echo "SCENARIO 6: Git History"
echo "================================================="

run_test "Git repository exists" "test -d .git"
run_test "Commits exist" "git log --oneline | grep -q ."

COMMIT_COUNT=$(git log --oneline | wc -l)
echo "  Total commits: ${COMMIT_COUNT}"

echo "Recent commits:"
git log --oneline -5 | sed 's/^/  /'
echo ""

# Final Summary
echo "================================================="
echo "  Test Summary"
echo "================================================="
echo -e "${GREEN}Tests Passed: ${TESTS_PASSED}${NC}"
echo -e "${RED}Tests Failed: ${TESTS_FAILED}${NC}"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$((TESTS_PASSED * 100 / TOTAL_TESTS))
    echo "Success Rate: ${SUCCESS_RATE}%"
fi

echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review.${NC}"
    exit 1
fi
