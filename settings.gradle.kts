rootProject.name = "sudoku-10000"

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.50.2"
}

include(
    ":demo",
    ":library"
)
