# Java Swing File Organizer with Drag-and-Drop and Undo

A simple desktop application in Java Swing to organize files in a selected folder into categorized subfolders, featuring:

- **Drag-and-drop** support (drop a folder anywhere on the app window)
- **Browse button** to select folders
- Categorizes files into folders like **Images, Documents, Music, Videos, Others**
- **Undo feature** to restore files back to their original locations
- **Progress bar** showing progress during organization

---

## Features

- Automatically moves files based on their extensions
- Supports common file types for images, documents, music, and videos
- Creates category folders if they do not exist
- Undo last organization operation using a saved log file
- User-friendly GUI built with Java Swing
- Drag and drop folders anywhere on the window to quickly select a folder

---

## Screenshots

<img width="734" height="255" alt="image" src="https://github.com/user-attachments/assets/ecbe5d7a-c13c-4d4d-bfff-547f5bf1742c" />

<img width="1893" height="1000" alt="image" src="https://github.com/user-attachments/assets/8030fb5a-8df1-458c-a8d7-69ccab8b5424" />


---


## How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or above installed
- `javac` and `java` commands available in your system PATH

### Compile
```bash
javac FileOrganizerGUI.java
