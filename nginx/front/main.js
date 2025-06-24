class GettarrResponse {
    constructor(status, body) {
        this.status = status
        this.body = body
    }

    static fromJson(json) {
        return new GettarrResponse(json.status, json.body)
    }
}

class GettarrInfo {
    constructor(title, thumbnail, duration) {
        this.title = title
        this.thumbnail = thumbnail
        this.duration = duration
    }

    static fromJson(json) {
        return new GettarrInfo(json.title, json.thumbnail, json.duration)
    }
}

const baseUrl = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ":" + window.location.port : "")
const endpointUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/api"


const checkInterval = 500 // Interval to check if the content has been downloaded
const display = document.getElementById("statusDisplay")
const displayBody = document.getElementById("statusDisplayBody")
const videoInfoDisplay = document.getElementById("videoInfo")
const urlInput = document.getElementById("urlInput")
const spinner = document.createElement("div");
spinner.id = "spinner";
spinner.classList.add("spinner");

let debounceTimer = null
const debounceInterval = 1000 // Time (in ms) to wait after user input stops to check for video info

// download request function using async/await
async function requestDownload(queryUrl) {
    const response = await fetch(endpointUrl + "/dw", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ inputUrl: queryUrl, vidQuality: videoQualityValues[vSlider.value], audioQuality: audioQualityValues[aSlider.value]})
    })

    const json = await response.json()
    return GettarrResponse.fromJson(json)
}

async function checkDownloadStatus(fileId) {
    const interval = setInterval(async () => {
        try {
            const res = await fetch(`${endpointUrl}/dw/status/${fileId}`)
            const json = await res.json()
            console.log("Current status: " + json.status + " body: " + json.body)
            showStatus(json.status, json.body)
            if (json.status === "DOWNLOADING")
                showStatus("DOWNLOADING", "Your file " + json.body + " is being downloaded, please hold")
            else
                clearInterval(interval)
        } catch (error) {
            console.error("Error checking download status: ", error)
            showStatus("ERROR", error)
            clearInterval(interval)
        }
    }, checkInterval)
}

async function requestInfo(queryUrl) {
    console.log("request info for: " + queryUrl)
    const response = await fetch(endpointUrl + "/info", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ inputUrl: queryUrl })
    })

    const json = await response.json()
    return GettarrInfo.fromJson(json)
}


const videoQualityValues = ["144p","240p","360p","480p","720p","1080p","1440p","2160p"];
const audioQualityValues = ["64kbps","128kbps","192kbps","256kbps","320kbps"];

const vSlider = document.getElementById("videoQualitySlider");
const vLabel  = document.getElementById("videoQualityLabel");
vSlider.addEventListener("input", () => {
vLabel.textContent = videoQualityValues[vSlider.value];
});

const aSlider = document.getElementById("audioQualitySlider");
const aLabel  = document.getElementById("audioQualityLabel");
aSlider.addEventListener("input", () => {
aLabel.textContent = audioQualityValues[aSlider.value];
});

async function displayVideoInfo(videoData) {
    videoInfoDisplay.innerHTML = "";
    videoInfoDisplay.classList.add("video-card");

    // Create a title element
    const titleEl = document.createElement("h3");
    titleEl.classList.add("video-title");
    titleEl.textContent = videoData.title;

    // Create an image element for the thumbnail
    const img = document.createElement("img");
    img.classList.add("video-thumbnail");
    img.src = videoData.thumbnail;
    img.alt = videoData.title;
    if (videoData.duration) {
        const thumbnailContainer = document.createElement("div");
        thumbnailContainer.classList.add("video-thumbnail-container");
        const durationEl = document.createElement("span");
        durationEl.classList.add("video-duration");
        durationEl.textContent = formatTime(videoData.duration);
        thumbnailContainer.appendChild(img);
        thumbnailContainer.appendChild(durationEl);
        videoInfoDisplay.appendChild(thumbnailContainer);
    } else {
        videoInfoDisplay.appendChild(img);
    }

    // Append children to the card
    videoInfoDisplay.appendChild(titleEl);
    videoInfoDisplay.style.display = "flex";
}

function showStatus(code, body) {
    clearDisplay()
    display.style.display = "flex"
    switch (code) {
        case "COMPLETED":
            display.classList.add("state-ok")
            displayBody.innerHTML = `<p>Your file has been downloaded successfully:</p>
  <div class="btn-container">
    <a href="${baseUrl}/media/${body}" class="btn">
        <img src="/img/preview.svg" alt="Preview" class="icon"> Preview
    </a>
    <a href="${baseUrl}/media/${body}?download=1" class="btn">
        <img src="/img/download.svg" alt="Download" class="icon">Direct Download
    </a>
  </div>`
            // display.innerHTML += `<a href="${baseUrl}/media/${body}?download=1">Direct Download</a>`
            urlInput.value = ""
            display.removeChild(spinner);
            break
        case "DOWNLOADING":
            display.classList.add("state-checking")
            displayBody.innerText = body
            if (!document.getElementById("spinner")) {
                display.appendChild(spinner);
            }
            break
        default:
            display.classList.add("state-error")
            displayBody.innerText = body
            display.removeChild(spinner);
            break
    }
}

function clearDisplay() {
    display.classList.remove("state-error", "state-ok", "state-checking");
    // Clear only the message part so that the spinner remains intact.
    displayBody.innerHTML = "";
    display.style.display = "none";
}

function formatTime(seconds) {
    let minutes = Math.floor(seconds / 60);
    let sec = seconds % 60;
    return `${minutes}:${sec.toString().padStart(2, '0')}`;
}

// Event handler for the download form
document.getElementById("downloadForm").addEventListener("submit", async function (e) {
    e.preventDefault()
    const queryUrl = e.target.urlInput.value.trim()
    console.log("Submitting -> " + queryUrl)
    const response = await requestDownload(queryUrl)

    if (response.status === "OK") {
        console.log("Download initiated. File ID: " + response.body)
        showStatus("DOWNLOADING", "Your file " + response.body + " is being downloaded, please hold")
        checkDownloadStatus(response.body)
    } else {
        console.error("ERROR: " + response)
        showStatus("ERROR", response.body)
    }
})

document.getElementById("urlInput").addEventListener('input', () => {
    if (debounceTimer) clearTimeout(debounceTimer);

    debounceTimer = setTimeout(async function () {
        if (URL.parse(urlInput.value)) {
            const response = await requestInfo(urlInput.value);
            console.log("Got response:", response);
            if (response && response.title && response.thumbnail) {
                displayVideoInfo(response);
            } else {
                console.error("Incomplete response:", response);
            }
        }
    }, debounceInterval);
});