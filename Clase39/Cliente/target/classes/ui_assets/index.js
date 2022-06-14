async function fetchBooks() {
    console.log("Ya entre");
    const data = await fetch('http://34.121.124.189:8080/searchBooks?phrase=FRASE');
    const data1 = new URLSearchParams(data.search);
    console.log(data1);
    console.log(response);
}

fetchBooks();