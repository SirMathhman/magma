#include <iostream>
#include <fstream>
#include <string>

int main() {
    // Source and destination file paths
    std::string sourcePath = "./main.mgs";
    std::string destinationPath = "./main.cpp";

    // Open the source file for reading
    std::ifstream sourceFile(sourcePath);
    if (!sourceFile) {
        std::cerr << "Error: Could not open source file " << sourcePath << " for reading." << std::endl;
        return 1;
    }

    // Open the destination file for writing
    std::ofstream destinationFile(destinationPath);
    if (!destinationFile) {
        std::cerr << "Error: Could not open destination file " << destinationPath << " for writing." << std::endl;
        return 1;
    }

    // Copy content line by line
    std::string line;
    while (std::getline(sourceFile, line)) {
        destinationFile << line << std::endl;
    }

    // Close the files
    sourceFile.close();
    destinationFile.close();

    std::cout << "Content copied from " << sourcePath << " to " << destinationPath << " successfully." << std::endl;

    return 0;
}
