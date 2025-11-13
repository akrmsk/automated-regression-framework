document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("runs-table-body");

    // Fetch data from the API.
    // We use localhost:8080 because this script runs in the user's browser,
    // not inside the Docker container.
    fetch("http://localhost:8080/api/runs")
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok: " + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            if (data.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="7">No test runs found.</td></tr>';
                return;
            }

            // Clear loading row
            tableBody.innerHTML = "";

            // Loop through each test run and add it to the table
            data.forEach(run => {
                const row = document.createElement("tr");

                // Format dates to be readable
                const startTime = new Date(run.startTime).toLocaleString();
                const endTime = run.endTime ? new Date(run.endTime).toLocaleString() : "N/A";

                row.innerHTML = `
                    <td>${run.runId}</td>
                    <td>${run.environment}</td>
                    <td class="status-${run.status.toLowerCase()}">${run.status}</td>
                    <td>${run.passedTestCount}</td>
                    <td>${run.failedTestCount}</td>
                    <td>${startTime}</td>
                    <td>${endTime}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error("Error fetching test runs:", error);
            tableBody.innerHTML = `<tr><td colspan="7">Error loading data: ${error.message}</td></tr>`;
        });
});