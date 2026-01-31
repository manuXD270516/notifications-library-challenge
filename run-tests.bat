@echo off
REM Testing Script for Windows - Notifications Library

echo =================================================
echo   Notifications Library - Testing Suite
echo =================================================
echo.

set TESTS_PASSED=0
set TESTS_FAILED=0

echo Checking prerequisites...
echo =================================================

REM Check Java
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [32m[OK] Java found[0m
    java -version 2>&1 | findstr "version"
) else (
    echo [33m[WARNING] Java not found - Docker can be used instead[0m
)

REM Check Maven
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [32m[OK] Maven found[0m
    mvn -version | findstr "Apache Maven"
) else (
    echo [33m[WARNING] Maven not found - Docker can be used instead[0m
)

REM Check Docker
where docker >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [32m[OK] Docker found[0m
    docker --version
) else (
    echo [31m[ERROR] Docker not found[0m
)

echo.
echo =================================================
echo   Starting Test Scenarios
echo =================================================
echo.

REM Scenario 1: Maven Build
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo SCENARIO 1: Maven Build
    echo =================================================
    
    echo Running: mvn clean compile...
    mvn clean compile -s settings.xml -q
    if %ERRORLEVEL% EQU 0 (
        echo [32m[PASS] Maven compile[0m
        set /a TESTS_PASSED+=1
    ) else (
        echo [31m[FAIL] Maven compile[0m
        set /a TESTS_FAILED+=1
    )
    
    echo Running: mvn clean package...
    mvn clean package -DskipTests -s settings.xml -q
    if %ERRORLEVEL% EQU 0 (
        echo [32m[PASS] Maven package[0m
        set /a TESTS_PASSED+=1
    ) else (
        echo [31m[FAIL] Maven package[0m
        set /a TESTS_FAILED+=1
    )
    
    if exist target\notifications-library-1.0.0.jar (
        echo [32m[PASS] JAR exists[0m
        set /a TESTS_PASSED+=1
        dir target\notifications-library-1.0.0.jar | findstr "notifications-library"
    ) else (
        echo [31m[FAIL] JAR not found[0m
        set /a TESTS_FAILED+=1
    )
    
    echo.
) else (
    echo [33mSkipping Maven build tests - Maven not available[0m
    echo.
)

REM Scenario 2: Unit Tests
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo SCENARIO 2: Unit Tests
    echo =================================================
    
    echo Running: mvn test...
    mvn test -s settings.xml
    if %ERRORLEVEL% EQU 0 (
        echo [32m[PASS] All unit tests[0m
        set /a TESTS_PASSED+=1
    ) else (
        echo [31m[FAIL] Some unit tests failed[0m
        set /a TESTS_FAILED+=1
    )
    
    echo.
) else (
    echo [33mSkipping unit tests - Maven not available[0m
    echo.
)

REM Scenario 3: Docker Build
where docker >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo SCENARIO 3: Docker Build
    echo =================================================
    
    echo Running: docker-compose build...
    docker-compose build
    if %ERRORLEVEL% EQU 0 (
        echo [32m[PASS] Docker build[0m
        set /a TESTS_PASSED+=1
    ) else (
        echo [31m[FAIL] Docker build[0m
        set /a TESTS_FAILED+=1
    )
    
    docker images notifications-library:1.0.0 >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo [32m[PASS] Docker image exists[0m
        set /a TESTS_PASSED+=1
        docker images notifications-library:1.0.0
    ) else (
        echo [31m[FAIL] Docker image not found[0m
        set /a TESTS_FAILED+=1
    )
    
    echo.
) else (
    echo [33mSkipping Docker tests - Docker not available[0m
    echo.
)

REM Scenario 4: Code Structure
echo SCENARIO 4: Code Structure
echo =================================================

if exist "src\main\java\com\novacomp\notifications" (
    echo [32m[PASS] Source directory exists[0m
    set /a TESTS_PASSED+=1
) else (
    echo [31m[FAIL] Source directory missing[0m
    set /a TESTS_FAILED+=1
)

if exist "src\test\java\com\novacomp\notifications" (
    echo [32m[PASS] Test directory exists[0m
    set /a TESTS_PASSED+=1
) else (
    echo [31m[FAIL] Test directory missing[0m
    set /a TESTS_FAILED+=1
)

if exist README.md (
    echo [32m[PASS] README.md exists[0m
    set /a TESTS_PASSED+=1
) else (
    echo [31m[FAIL] README.md missing[0m
    set /a TESTS_FAILED+=1
)

echo.

REM Final Summary
echo =================================================
echo   Test Summary
echo =================================================
echo Tests Passed: %TESTS_PASSED%
echo Tests Failed: %TESTS_FAILED%
echo.

if %TESTS_FAILED% EQU 0 (
    echo [32m All tests passed! [0m
    exit /b 0
) else (
    echo [31m Some tests failed. Please review. [0m
    exit /b 1
)
