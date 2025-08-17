#ifndef CSV_MANAGER_H
#define CSV_MANAGER_H

#include "csv2/reader.hpp"
#include <string>
#include <vector>
#include <memory>

class CSVManager {
private:
    std::unique_ptr<csv2::Reader<csv2::delimiter<','>,
    csv2::quote_character<'"'>,
    csv2::first_row_is_header<true>,
    csv2::trim_policy::trim_whitespace>> reader;
    bool fileLoaded;
    std::vector<std::string> headers;
    size_t totalRows;

public:
    CSVManager();
    ~CSVManager();

    // Singleton pattern
    static CSVManager& getInstance();

    // Core functionality
    bool loadFile(const std::string& filePath);
    void clearData();

    // Data access
    const std::vector<std::string>& getHeaders() const;
    std::vector<std::string> getRow(size_t rowIndex) const;
    size_t getRowCount() const;
    size_t getColumnCount() const;
    bool isFileLoaded() const;

    // Utility
    std::string getCellValue(size_t rowIndex, size_t columnIndex) const;
};

#endif // CSV_MANAGER_H