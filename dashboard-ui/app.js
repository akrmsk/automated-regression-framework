document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("runs-table-body");
    const API_URL = "http://localhost:8080/api/runs";

    // Creates a clickable link if the report URL exists
    const createReportLink = (run) => {
        if (run.reportUrl) {
            // This links to http://localhost:8081/reports/report-....html
            return `<a href="${run.reportUrl}" target="_blank">${run.id}</a>`;
        }
        return run.id; // Just return the ID if no report
    };

    // Fetch data from the API
    fetch(API_URL)
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok: " + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            if (data.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6">No test runs found.</td></tr>';
                return;
            }

            // Clear loading row
            tableBody.innerHTML = "";

            // --- NEW: Sort data to show newest runs first ---
            data.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));

            // Loop through each test run and add it to the table
            data.forEach(run => {
                const row = document.createElement("tr");

                // Format dates to be readable
                const startTime = new Date(run.startTime).toLocaleString();
                const endTime = run.endTime ? new Date(run.endTime).toLocaleString() : "N/A";
                const idCell = createReportLink(run);

                // --- MODIFIED: Added <span> for status badge ---
                row.innerHTML = `
                    <td>${idCell}</td>
                    <td>${run.environment}</td>
                    <td><span class="status status-${run.status.toLowerCase()}">${run.status}</span></td>
                    <td>${run.failedTestCount}</td>
                    <td>${startTime}</td>
                    <td>${endTime}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error("Error fetching test runs:", error);
            tableBody.innerHTML = `<tr><td colspan="6">Error loading data: ${error.message}</td></tr>`;
        });
});