# 2020 Vision Code

Vision code for the 2020 robot. Eventually, this will be deployed to a raspberry pi on the robot and will communicate with the roborio via NetworkTables.

This still require modification to work on the FRCVision raspi images.

## Building
This project used gradle for the build system. Building can be done with `./gradlew build`. Running the app an be done with `./gradlew run`. The app will require the opencv library to be installed (gradle handles the java bindings).
