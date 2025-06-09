class GettarrResponse {
    constructor(status, body, data) {
        this.status = status
        this.body = body
    }

    static fromJson(json) {
        return new GettarrResponse(json.status, json.body)
    }
}

const checkInterval = 500 // Interval to check if the content has been downloaded
const display = document.getElementById("statusDisplay")
const spinner = document.createElement("div")
spinner.id = "spinner"
const baseUrl = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ":" + window.location.port : "")
const endpointUrl = window.location.protocol + "//" + window.location.hostname + ":5000"
const urlInput = document.getElementById("urlInput")

// download request function using async/await
async function requestDownload(queryUrl) {
    const response = await fetch(endpointUrl+"/dw", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ inputUrl: queryUrl })
    })
    
    const json = await response.json()
    return GettarrResponse.fromJson(json)
}


async function checkDownloadStatus(fileId) {
    const interval = setInterval(async () => {
        try {
            const res = await fetch(`${endpointUrl}/dw/status/${fileId}`)
            const json = await res.json()
            console.log("Current status: " + json.status+ " body: "+json.body)
            showStatus(json.status, json.body)
            if (json.status === "DOWNLOADING")
                showStatus("DOWNLOADING", "Your file "+json.body+" is being downloaded, please hold")
            else
                clearInterval(interval)
        } catch (error) {
            console.error("Error checking download status: ", error)
            showStatus("ERROR", error)
            clearInterval(interval)
        }
    }, checkInterval) 
}

function showStatus(code, body) {
    clearDisplay()
    display.style.display = "flex"
    switch (code) {
        case "COMPLETED":
            display.classList.add("state-ok")
            display.innerHTML = `<p>Your file has been downloaded successfully:</p>
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
            break
        case "DOWNLOADING":
            display.classList.add("state-checking")
            display.innerText = body
            display.append(spinner)
            break
        default:
            display.classList.add("state-error")
            display.innerText = body
            break
    }
}

function clearDisplay() {
    display.classList.remove("state-error", "state-ok", "state-checking")
    display.innerText =""
    display.style.display = "none"
}


// Event handler for the download form
document.getElementById("downloadForm").addEventListener("submit", async function(e) {
    e.preventDefault()
    const queryUrl = e.target.urlInput.value.trim()
    console.log("Submitting -> " + queryUrl)
    const response = await requestDownload(queryUrl)

    if (response.status === "OK" ) {
        console.log("Download initiated. File ID: " + response.body)
        showStatus("DOWNLOADING", "Your file "+response.body+" is being downloaded, please hold")
        checkDownloadStatus(response.body)
    } else {
        console.error("ERROR: " + response)
        showStatus("ERROR", response.body)
    }
})


