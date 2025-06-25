# Gettarr

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Gettarr is a self-hosted, open-source media downloader that provides a configurable, ad-free way to download and manage media content. With Gettarr, you can download your favorite media files and share them directly, ensuring that the content remains accessible even if it’s later removed, moved, or updated on social media platforms.

## Overview

The primary goal of Gettarr is to simplify media consumption:
- **Media Ownership:** Download your favorite media files directly instead of depending on external platforms.
- **Easy Sharing:** Share files with friends without requiring them to access social media.
- **Content Preservation:** Keep a personal archive of the media content you love.

Gettarr is built on [yt-dlp](https://github.com/yt-dlp/yt-dlp), enabling downloads from a wide range of websites.

## Disclaimer

Gettarr is provided as-is for personal use only. It is not intended to facilitate activities that violate any website's terms of service or any applicable legislation. **Use at your own risk.**  

## Future Improvements

- **Enhanced Configuration:** More granular options for download management.
- **Additional Site Support:** Integration of gallery-dl for extended website compatibility.
- **Detailed Documentation:** Expanded sections on installation, configuration, and troubleshooting.

## Installation 
To run Gettarr in a Linux host, you only need Docker and Docker Compose installed.

*If it’s your first time using Docker or Compose, check out the [Docker installation guide](https://docs.docker.com/get-started/get-docker/) and the [Compose quick start](https://docs.docker.com/compose/gettingstarted/).*

### Clone the repository

The first step is to clone the repo with the project:

```
git clone https://github.com/TangibleRaccoon/gettarr.git
cd gettarr
```

### Adding your SSL certificate
To enable HTTPS in Gettarr, you’ll need to provide your own SSL certificate and key files. Place them in the following locations:

* ./nginx/proxy/cert.pem – your SSL certificate file
* ./nginx/proxy/key.pem – the corresponding private key file

If you're using a certificate authority like Let's Encrypt, you can copy or symlink the .pem files from your cert directory.

Note: Make sure both files are valid and readable by Docker. For testing or local use, you can generate a self-signed certificate with:
```
openssl req -x509 -newkey rsa:4096 -keyout ./nginx/proxy/key.pem -out ./nginx/proxy/cert.pem -days 365 -nodes
```

Once the files are in place, the `gettarr-proxy` container will automatically use them to serve content securely over HTTPS at `https://localhost:42000`.

### Build and start the app
These commands will build the Docker image and start all necessary services:

```
docker compose build
docker compose up
```
 *Please be mindful that building the app and downloading all the necessary docker images will take some time, depending on your machine, and will take a longer time on the first start*

By default, the application will be accessible at `https://localhost:42000` unless configured otherwise.

### Run the Gettarr API only

The steps to run the gettarr API only without the standard front-end are as follows:

#### Build the image 
```
docker build -f DOCKERFILE_SPRING -t gettarr:latest .
```
After this step you can create gettarr containers as any other regular docker image, for example this would be the command used to start a gettarr container, expose the port 5000, use /tmp/data and /tmp/media folders as the volumes for downloaded and registered data, give the container the name "gettarr-api" and keep it running in the background

```
docker run --name gettarr-api -v /tmp/media:/tmp/media -v /tmp/data:/data -p 5000:5000 -d gettarr:latest
```

### Run without SSL

If you want to run the app directly without using SSL certificates (for example only for a local network)
you can specify docker compose to use the "docker-compose-no-ssl.yaml" file to start the project:
```
docker compose build
docker compose -f docker-compose-no-ssl.yaml up
```
In this case the application can be accessed at `http://localhost:42000`

## License
 This project is licensed under the GNU General Public License v3.0.  
See [LICENSE.txt](./LICENSE.txt) for more details.

This project is a work in progress (WIP) and licensed under the GNU General Public License, Version 3 (GPLv3) 
**Please note:**

- This license does not extend to third-party components which may be included in this project.
- `yt-dlp` is licensed under the **Unlicense**.
- `gallery-dl` is licensed under **GNU GPL 2.0**.
- `Spring Boot` is licensed under **Apache License 2.0**.

You must comply with the terms of these licenses when using or redistributing this software.

### Use this project at your own risk.