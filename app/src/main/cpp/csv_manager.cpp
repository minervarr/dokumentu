#include "csv_manager.h"
#include <android/log.h>

#define LOG_TAG "CSVManager"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

CSVManager::CSVManager() : fileLoaded(false), totalRows(0) {
    reader = std::make_unique<csv2::Reader<csv2::delimiter<','>,
            csv2::quote_character<'"'>,
            csv2::first_row_is_header<true>,
            csv2::trim_policy::trim_whitespace>>();
}

CSVManager::~CSVManager() {
    clearData();
}

CSVManager& CSVManager::getInstance() {
    static CSVManager instance;
    return instance;
}

bool CSVManager::loadFile(const std::string& filePath) {
    LOGD("Loading CSV file: %s", filePath.c_str());

    clearData();

    if (!reader->mmap(filePath)) {
        LOGE("Failed to memory-map CSV file: %s", filePath.c_str());
        return false;
    }

    try {
        // Extract headers
        headers.clear();
        const auto headerRow = reader->header();
        for (const auto& cell : headerRow) {
            std::string value;
            cell.read_value(value);
            headers.push_back(value);
        }

        // Count total rows
        totalRows = reader->rows();
        fileLoaded = true;

        LOGD("CSV loaded successfully: %zu rows, %zu columns", totalRows, headers.size());
        return true;

    } catch (const std::exception& e) {
        LOGE("Exception while loading CSV: %s", e.what());
        clearData();
        return false;
    }
}

void CSVManager::clearData() {
    fileLoaded = false;
    headers.clear();
    totalRows = 0;
    // Note: reader is reused, not recreated
}

const std::vector<std::string>& CSVManager::getHeaders() const {
    return headers;
}

std::vector<std::string> CSVManager::getRow(size_t rowIndex) const {
    std::vector<std::string> rowData;

    if (!fileLoaded || rowIndex >= totalRows) {
        LOGE("Invalid row access: rowIndex=%zu, totalRows=%zu, fileLoaded=%d",
             rowIndex, totalRows, fileLoaded);
        return rowData;
    }

    try {
        size_t currentIndex = 0;
        for (const auto& row : *reader) {
            if (currentIndex == rowIndex) {
                for (const auto& cell : row) {
                    std::string value;
                    cell.read_value(value);
                    rowData.push_back(value);
                }
                break;
            }
            currentIndex++;
        }

    } catch (const std::exception& e) {
        LOGE("Exception while reading row %zu: %s", rowIndex, e.what());
        rowData.clear();
    }

    return rowData;
}

size_t CSVManager::getRowCount() const {
    return fileLoaded ? totalRows : 0;
}

size_t CSVManager::getColumnCount() const {
    return fileLoaded ? headers.size() : 0;
}

bool CSVManager::isFileLoaded() const {
    return fileLoaded;
}

std::string CSVManager::getCellValue(size_t rowIndex, size_t columnIndex) const {
    if (!fileLoaded || rowIndex >= totalRows || columnIndex >= headers.size()) {
        return "";
    }

    auto rowData = getRow(rowIndex);
    if (columnIndex < rowData.size()) {
        return rowData[columnIndex];
    }

    return "";
}