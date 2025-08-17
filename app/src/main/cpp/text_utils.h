#ifndef TEXT_UTILS_H
#define TEXT_UTILS_H

#include <string>
#include <string_view>
#include <vector>
#include <algorithm>
#include <cctype>

namespace csv_utils {

/**
 * Smart text utilities for CSV processing using C++17 features
 */
    class TextUtils {
    public:
        /**
         * Intelligently truncate header text for display
         * Uses C++17 string_view for efficient string operations
         */
        static std::string getSmartTruncatedHeader(std::string_view header, size_t maxLength = 12) {
            if (header.length() <= maxLength) {
                return std::string(header);
            }

            // Find natural break points (underscore, space, camelCase)
            auto breakPoint = findOptimalBreakPoint(header, maxLength);

            if (breakPoint != std::string_view::npos && breakPoint < maxLength - 1) {
                return std::string(header.substr(0, breakPoint + 1));
            }

            // Fallback to simple truncation
            return std::string(header.substr(0, maxLength));
        }

        /**
         * Calculate optimal column width based on content analysis
         */
        static size_t calculateOptimalColumnWidth(const std::vector<std::string>& columnData,
                                                  std::string_view header) {
            constexpr size_t MIN_WIDTH = 80;  // dp
            constexpr size_t MAX_WIDTH = 300; // dp
            constexpr size_t CHAR_WIDTH = 8;  // approximate dp per character

            // Start with header length
            size_t maxLength = header.length();

            // Analyze sample data (first 10 rows for performance)
            size_t sampleSize = std::min(columnData.size(), size_t(10));
            for (size_t i = 0; i < sampleSize; ++i) {
                maxLength = std::max(maxLength, columnData[i].length());
            }

            // Convert to dp and clamp
            size_t width = maxLength * CHAR_WIDTH;
            return std::clamp(width, MIN_WIDTH, MAX_WIDTH);
        }

        /**
         * Clean and format cell content for display
         */
        static std::string formatCellContent(std::string_view content) {
            std::string result(content);

            // Trim whitespace
            trimWhitespace(result);

            // Replace problematic characters
            std::replace(result.begin(), result.end(), '\r', ' ');
            std::replace(result.begin(), result.end(), '\n', ' ');
            std::replace(result.begin(), result.end(), '\t', ' ');

            // Collapse multiple spaces
            auto newEnd = std::unique(result.begin(), result.end(),
                                      [](char a, char b) { return a == ' ' && b == ' '; });
            result.erase(newEnd, result.end());

            return result;
        }

        /**
         * Validate and sanitize header names
         */
        static std::string sanitizeHeaderName(std::string_view header) {
            std::string result(header);
            trimWhitespace(result);

            if (result.empty()) {
                return "Column";
            }

            // Replace invalid characters
            std::replace_if(result.begin(), result.end(),
                            [](char c) { return !std::isalnum(c) && c != '_' && c != ' '; }, '_');

            return result;
        }

    private:
        /**
         * Find optimal break point for text truncation
         */
        static size_t findOptimalBreakPoint(std::string_view text, size_t maxLength) {
            if (text.length() <= maxLength) {
                return std::string_view::npos;
            }

            // Look for underscore within reasonable range
            auto underscorePos = text.find_last_of('_', maxLength - 1);
            if (underscorePos != std::string_view::npos && underscorePos > maxLength / 2) {
                return underscorePos;
            }

            // Look for space
            auto spacePos = text.find_last_of(' ', maxLength - 1);
            if (spacePos != std::string_view::npos && spacePos > maxLength / 2) {
                return spacePos;
            }

            // Look for camelCase transition
            for (size_t i = maxLength / 2; i < maxLength - 1 && i < text.length() - 1; ++i) {
                if (std::islower(text[i]) && std::isupper(text[i + 1])) {
                    return i;
                }
            }

            return std::string_view::npos;
        }

        /**
         * Efficient whitespace trimming using C++17 string_view
         */
        static void trimWhitespace(std::string& str) {
            // Trim from start
            str.erase(str.begin(), std::find_if(str.begin(), str.end(),
                                                [](unsigned char ch) { return !std::isspace(ch); }));

            // Trim from end
            str.erase(std::find_if(str.rbegin(), str.rend(),
                                   [](unsigned char ch) { return !std::isspace(ch); }).base(), str.end());
        }
    };

/**
 * Column metadata for enhanced table display
 */
    struct ColumnMetadata {
        std::string originalHeader;
        std::string displayHeader;
        size_t optimalWidth;
        bool isNumeric;
        bool hasLongContent;

        ColumnMetadata(std::string_view header, const std::vector<std::string>& data)
                : originalHeader(header)
                , displayHeader(TextUtils::getSmartTruncatedHeader(header))
                , optimalWidth(TextUtils::calculateOptimalColumnWidth(data, header))
                , isNumeric(analyzeColumnType(data))
                , hasLongContent(hasLongContentInColumn(data)) {}

    private:
        static bool analyzeColumnType(const std::vector<std::string>& data) {
            size_t numericCount = 0;
            size_t sampleSize = std::min(data.size(), size_t(5));

            for (size_t i = 0; i < sampleSize; ++i) {
                if (isNumericValue(data[i])) {
                    numericCount++;
                }
            }

            return sampleSize > 0 && (numericCount * 2 > sampleSize); // >50% numeric
        }

        static bool isNumericValue(const std::string& value) {
            if (value.empty()) return false;

            auto it = value.begin();
            if (*it == '-' || *it == '+') ++it; // Allow sign

            bool hasDigit = false;
            bool hasDot = false;

            while (it != value.end()) {
                if (std::isdigit(*it)) {
                    hasDigit = true;
                } else if (*it == '.' && !hasDot) {
                    hasDot = true;
                } else if (*it == ',' && hasDigit) {
                    // Allow thousand separators
                } else {
                    return false;
                }
                ++it;
            }

            return hasDigit;
        }

        static bool hasLongContentInColumn(const std::vector<std::string>& data) {
            constexpr size_t LONG_CONTENT_THRESHOLD = 50;

            for (const auto& item : data) {
                if (item.length() > LONG_CONTENT_THRESHOLD) {
                    return true;
                }
            }
            return false;
        }
    };

} // namespace csv_utils

#endif // TEXT_UTILS_H