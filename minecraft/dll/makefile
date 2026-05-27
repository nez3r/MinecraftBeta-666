# Makefile for compiling error404.dll
# Requires MinGW-w64 or MSVC

# Compiler
CXX = g++

# Output DLL name
TARGET = error404.dll

# Source files
SOURCES = main.cpp

# Compilation flags
CXXFLAGS = -std=c++11 -O2 -Wall

# Linking flags
LDFLAGS = -shared -static-libgcc -static-libstdc++

# Windows libraries
LIBS = -lgdi32 -luser32 -lshell32 -lkernel32 -lmsimg32

# Default rule
all: $(TARGET)

# Compile DLL
$(TARGET): $(SOURCES)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) -o $(TARGET) $(SOURCES) $(LIBS)
	@echo "==================================="
	@echo "DLL compiled successfully!"
	@echo "Output: $(TARGET)"
	@echo "==================================="

# Clean
clean:
	del /Q $(TARGET) 2>nul || rm -f $(TARGET)

# Install to libraries/natives/
install: $(TARGET)
	@if not exist "..\libraries\natives\" mkdir "..\libraries\natives\"
	copy $(TARGET) ..\libraries\natives\$(TARGET)
	@echo "==================================="
	@echo "DLL installed to libraries/natives/"
	@echo "==================================="

# Test compilation
test:
	@echo "Testing compilation..."
	$(CXX) --version
	@echo "MinGW is ready!"

.PHONY: all clean install test
