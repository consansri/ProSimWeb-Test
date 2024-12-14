# **ProSimWeb**

A university project developed by **Constantin Birkert** at the **Institute of Communication Networks and Computer Engineering** at the **University of Stuttgart**.

**Started as**\
This repository is dedicated to the development of a Web App Processor Emulator as part of my Bachelor's thesis at the University of Stuttgart, which will be carried out at the Institute of Communication Networks and Computer Systems (IKR). 
The project will begin on April 17, 2023, and is expected to be completed in six months, with a functional front-end web application that can emulate at least the RISC-V processor architecture and is easily expandable to other architectures.

## **Overview**
**ProSimWeb** is a cross-platform processor emulator that combines a **Integrated Development Environment (IDE)** with an **Emulator**. 
Originally created as part of a bachelor's thesis, the project has evolved to enable programming, compiling, and emulating processor architectures.

With support for **desktop** and **web** platforms (based on Kotlin Multiplatform), **ProSimWeb** is a flexible and modern tool for developing and analyzing assembly code and processor architectures.

---

## **Features**

### **1. Integrated Development Environment (IDE)**
The IDE is a core component of **ProSimWeb**, providing comprehensive tools for creating and managing source code.

- **Supported Architectures:**
    - **RISC-V**
    - **6502**
    - **IKR RISC-II**
    - **IKR Mini**
- **Supported File Formats:**
    - **ELF (Executable and Linkable Format)** for architectures with byte addressed memory
    - **MIF (Memory Initialization File)** for architectures without byte addressed memory
- **Compiler:**
    - Custom `LanguageService`, `AsmLang` inspired by the **GNU Assembler**
    - Automatic translation of assembly files into ELF or MIF formats via `AsmCodeGenerator`
- **Code Analysis and Editing:**
    - Advanced syntax highlighting and code analysis using a custom **Program Structure Interface (PSI)**
    - Long-term goal: Support for higher-level languages like **C**, which can be compiled directly into ELF
- **Project System:**
    - Manage multiple projects with working directories, architecture associations, and saved states (e.g., open views, layout configurations)

### **2. Processor Emulator**
The emulator executes compiled code and provides detailed insights into processor states and execution flows.

- **Microarchitecture Design:**
    - Support for architectures with customizable components (e.g., register files, caches)
    - Example: **RISC-V** includes a standard register file and CSR registers
- **Cache Support:**
    - Configurable instruction and data caches (32 KB)
    - Modes: **Direct-Mapped**, **Set-Associative**, **Fully-Associative**
- **Performance Optimization:**
    - Uses native integer types (`Byte`, `Short`, `Int`, `Long`) and **BigInteger** for larger computations (e.g., 128-bit)
- **User Interface:**
    - **Execution View:** Synchronized disassembly of ELF or object files, linked to the program counter
    - **Register View:** Overview of all register states
    - **Memory View** Overview of all memory states
    - Automatically initializes memory based on the selected object file

### **3. Shared Virtual File System**
A cross-platform file system provides seamless file access:
- **Web:** Utilizes **Local Storage**
- **Desktop:** Integrates with the **native file system**

---

## **User Interface**
The UI is built entirely with **Compose Multiplatform**, inspired by modern IDE layouts:
- **Design:**
    - **Border layout** for clear organization of views
    - **Dark and Light Modes** with a custom **theming system**
    - Fonts: **JetBrains Mono** and **Poppins**
- **Key Views:**
    - **Project Management**: Create or delete Projects, select Architecture
    - **IDE View**: Code Assembly Programs for a Processor Architecture
    - **Emulator View:** Includes Execution View, Register View, Memory View emulation and debugging

---

## **Workflow**

1. **Develop Code:** Write assembly files in the IDE
2. **Compile:** Generate ELF or MIF files
3. **Emulate:** Load and execute the compiled files in the emulator
4. **Debug:** Analyze and fix issues
5. **Iterate:** Seamlessly switch between the IDE and emulator for efficient workflows

---

## **Technology Stack**
- **Programming Language:** Kotlin (Multiplatform)
- **Framework:** Compose Multiplatform
- **Build System:** Gradle
- **Platforms:** JVM, WebAssembly, JavaScript

---

## **Installation**

### **Desktop**
1. Install Java
2. Download Desktop build from the [web application](https://content.ikr.uni-stuttgart.de/Content/ProSimWeb/).
3. Execute jar with: java -jar [name] 

### **Web**
1. Visit the [web application](https://content.ikr.uni-stuttgart.de/Content/ProSimWeb/).
2. Use the IDE and emulator directly in your browser.

---
