group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
