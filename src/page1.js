fetch('system.php')
    .then(response => response.json())
    .then(data => {
        // Display data on the page, make fields editable
    })
    .catch(error => console.error('Error:', error));
