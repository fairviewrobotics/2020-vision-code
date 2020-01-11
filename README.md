# 2020 Vision Code

Vision code for the 2020 robot. Eventually, this will be deployed to a raspberry pi on the robot and will communicate with the roborio via NetworkTables.

This still require modification to work on the FRCVision raspi images.

## Environment
This environment setup assumes you're in Ubuntu, or something similar, but the same basic process should work on other distros, or in Mac OS or Windows.

### Install OpenJDK 11 (or higher)

Having the Java runtime environment installed is necessary, but not sufficient; you must have the full JDK installed. Any recent version should work; I tested with 11.

```
sudo apt-get install -y openjdk-11-jdk
```

Make sure your `JAVA_HOME` env variable is set:

```
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/
```

(substitute the correct path for your java install)

On Ubuntu 19, also set your `ANT_HOME` env variable:

```
export ANT_HOME=/usr/share/ant
```

In some systems, `/bin` may be a symlink to `/usr/bin`, and this breaks the default Ubuntu `ant` package. To fix this, remove the old `ant` symlink and add a new one with the correct path:

```
sudo rm /usr/bin/ant
sudo ln -s -T /usr/share/ant/bin/ant /usr/bin/ant
```

You can add that line to `$HOME/.bashrc` so you won't have to reset it for every new terminal (don't forget to restart your terminal if you change `.bashrc`).

### Install OpenCV Dependencies

Update packages:

```
sudo apt-get update
sudo apt-get upgrade -y
```

Remove any previous installs of x264

```
sudo apt-get remove --purge -y x264 libx264-dev
```
 
Install build dependencies:

```
sudo apt-get install -y \
  build-essential \
  checkinstall \
  cmake \
  pkg-config \
  yasm \
  git \
  gfortran
```

Install library dependencies:

```
sudo apt-get install -y \
  libjpeg8-dev \
  libavcodec-dev \
  libavformat-dev \
  libswscale-dev \
  libdc1394-22-dev \
  libxine2-dev \
  libv4l-dev \
  qt5-default \
  libgtk2.0-dev \
  libtbb-dev \
  libatlas-base-dev \
  libfaac-dev \
  libmp3lame-dev \
  libtheora-dev \
  libvorbis-dev \
  libxvidcore-dev \
  libopencore-amrnb-dev \
  libopencore-amrwb-dev \
  x264 \
  v4l-utils
```

A bunch of library dependencies seem to depend on the specific ubuntu version you're using. I've done my best below, but this list is incomplete. You'll have to pay attention to the output of `apt-get install` and make sure everything gets installed as expected, and if an install fails you'll need to search for an alternative.

If you are using Ubuntu 18.04:

```
sudo apt-get install -y \
  libpng-dev \
  libgstreamer1.0-dev \
  libgstreamer-plugins-base1.0-dev \
```

(TODO: No candidate found for libjasper-dev on Ubuntu 18.04...) 

If you are using an older version of Ubuntu:

```
sudo apt-get install -y \
  libpng12-dev \
  libjasper-dev \
  libgstreamer0.10-dev \
  libgstreamer-plugins-base0.10-dev \
```

If you are using Ubuntu 16.04 or Ubuntu 18.04:

```
sudo apt-get install -y libtiff5-dev
```

If you are using Ubuntu 14.04:

```
sudo apt-get install -y libtiff4-dev
```

### Install OpenCV 4.1.2

Download and unzip the source files for OpenCV 4.1.2:

```
cd /tmp
wget https://github.com/opencv/opencv/archive/4.1.2.zip
unzip opencv-4.1.2.zip
cd opencv-4.1.2
```

(All releases can be found here: https://opencv.org/releases/ )

Run CMake to set up the OpenCV build:

```
mkdir build
cd build
cmake .. -DCMAKE_BUILD_TYPE=RELEASE \
         -DCMAKE_INSTALL_PREFIX=/usr/local \
         -DINSTALL_C_EXAMPLES=ON \
         -DINSTALL_PYTHON_EXAMPLES=ON \
         -DWITH_TBB=ON \
         -DWITH_V4L=ON \
         -DWITH_QT=ON \
         -DWITH_OPENGL=ON \
         -DBUILD_EXAMPLES=ON \
         -DBUILD_JAVA=ON
```

Build and install OpenCV:

```
# find out number of CPU cores in your machine
nproc
# substitute 4 by output of nproc
make -j4
sudo make install
sudo cp lib/libopencv_java412.so /usr/lib/
sudo sh -c 'echo "/usr/local/lib" >> /etc/ld.so.conf.d/opencv.conf'
sudo ldconfig
```

## Building
This project uses gradle for the build system. Building can be done with `./gradlew build`.

## Running
Running the app an be done with `./gradlew run`. The app will require the opencv library to be installed (gradle handles the java bindings).
