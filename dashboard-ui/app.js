document.addEventListener("DOMContentLoaded", () => {
    // API Endpoint
    const API_URL = "http://localhost:8080/api/runs";

    // Get DOM elements
    const tableBody = document.getElementById("runs-table-body");

    // Summary card elements
    const totalRunsEl = document.getElementById("total-runs");
    const totalCompletedEl = document.getElementById("total-completed");
    const totalFailedEl = document.getElementById("total-failed");
    const totalRunningEl = document.getElementById("total-running");

    // Main function to fetch and render data
    const loadData = () => {
        // Set table to loading state
        tableBody.innerHTML = '<tr><td colspan="7">Loading...</td></tr>';

        fetch(API_URL)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok: " + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                populateSummary(data);
                populateTable(data);
            })
            .catch(error => {
                console.error("Error fetching test runs:", error);
                tableBody.innerHTML = `<tr><td colspan="7">Error loading data: ${error.message}</td></tr>`;
                // Clear summary on error too
                populateSummary([]);
            });
    };

    // Function to populate the summary cards
    const populateSummary = (data) => {
        let completed = 0;
        let failed = 0;
        let running = 0;

        data.forEach(run => {
            switch(run.status) {
                case "COMPLETED":
                    completed++;
                    break;
                case "FAILED":
                    failed++;
                    break;
                case "RUNNING":
                case "SCHEDULED":
                    running++;
                    break;
            }
        });

        totalRunsEl.textContent = data.length;
        totalCompletedEl.textContent = completed;
        totalFailedEl.textContent = failed;
        totalRunningEl.textContent = running;
    };

    // Function to populate the runs table
    const populateTable = (data) => {
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7">No test runs found.</td></tr>';
            return;
        }

        // Clear loading row
        tableBody.innerHTML = "";

        // Sort data to show newest runs first
        data.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));

        // Loop through each test run and add it to the table
        data.forEach(run => {
            const row = document.createElement("tr");

            // Format dates to be readable
            const startTime = new Date(run.startTime).toLocaleString();
            const endTime = run.endTime ? new Date(run.endTime).toLocaleString() : "N/A";

            // Creates a link for the "Report" column
            let reportCell = `<span class="report-link-na">N/A</span>`;
            if (run.reportUrl) {
                // This assumes the API returns a relative path like "reports/report-name.html"
                reportCell = `<a href="${run.reportUrl}" target="_blank" class="report-link">View Report</a>`;
            }

            row.innerHTML = `
                <td>${run.id}</td>
                <td>${run.environment}</td>
                <td><span class="status status-${run.status.toLowerCase()}">${run.status}</span></td>
                <td>${run.failedTestCount !== null ? run.failedTestCount : 0}</td>
                <td>${startTime}</td>
                <td>${endTime}</td>
                <td>${reportCell}</td>
            `;
            tableBody.appendChild(row);
        });
    };

    // Initial data load
    loadData();

    // Optional: Refresh data every 10 seconds
    // setInterval(loadData, 10000);
});