let name = window.prompt("Enter Username");
let password = window.prompt("Enter Password");

if (name === null || password === null) {
    window.location.replace("/");
}

let ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/bot?name=" + name + "&password=" + password);