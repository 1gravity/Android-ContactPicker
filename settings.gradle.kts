rootProject.name = "Android-ContactPicker"

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.50.2"
}

include(
    ":demo",
    ":library"
)
